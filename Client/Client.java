import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Set;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;

import java.awt.image.BufferedImage;

public class Client{
    private final String SERVER_HOST = "localhost";
    private final int SERVER_PORT = 4567;
    public final int CALL_NOTIFY_PORT = 6767;
    
    private boolean call_incoming;
    private boolean in_call;
    private int call_in_channel;

    //State variables
    private String username;
    private HashMap<Integer, ChannelContainer> channels; //Holds information regarding chats
    private boolean microphone_enabled;
    private boolean camera_enabled;
    private PrivateKey client_private_key;
    private PublicKey server_public_key;

    //Server communication values
    private Socket socket;
    private BufferedReader client_in;
    private BufferedWriter client_out;
    private CallHandler callHandler;

    public Client() throws Exception{
        //Initialise state variables
        channels = new HashMap<Integer, ChannelContainer>();
        microphone_enabled = true;
        camera_enabled = true;

        //Create encryption keys
        KeyPair keys = EncryptionManager.get_keys();
        client_private_key = keys.getPrivate();
        //Establish connection with server and set up the reader and writer
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
        } catch (IOException e){
          throw  new Exception("Couldn`t connect to server");
        }
        client_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        client_out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        
        //Server Handshake
        System.out.println("Attempting to write hello");
        String to_send = "HELO " + Base64.getEncoder().encodeToString(keys.getPublic().getEncoded());
        System.out.println(to_send);
        client_out.write(to_send + "\n");//Encode the public key into bytes
        client_out.flush();
        
        client_out.flush();
        String[] msg = client_in.readLine().split(" ");
        if (msg[0].equals("HELO") && msg.length == 2){
            server_public_key = EncryptionManager.public_key_from_string(msg[1]); //Extract servers public key from the message received
        }

        Socket call_socket = new Socket(SERVER_HOST, CALL_NOTIFY_PORT);
        callHandler = new CallHandler(this, call_socket, server_public_key, client_private_key);
        callHandler.start();
    }

    public void quit(){
        try{
            if (callHandler != null) {
                callHandler.end();
            }
            socket.close();
            client_in.close();
            client_out.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    public boolean get_call_incoming(){
        return call_incoming;
    }
    
    public boolean get_in_call(){
        return in_call;
    }

    public int get_call_channel() {
        return call_in_channel;
    }

    public void notify_call_incoming(int channel_id) {
        call_in_channel = channel_id;
        call_incoming = true;
    }

    public void notify_call_ended() {
        call_in_channel = -1;
        call_incoming = false;
    }

    public String[] get_users_in_call(){
        return new String[0];
    }

    public Integer[] get_channel_ids() {
        //Returns all channel ids the client stores
        Set<Integer> ids = channels.keySet();
        return ids.toArray(new Integer[ids.size()]);
    }

    public String get_channel_name(int channel_id) {
        //gui function call, returns the name of the channel with the channel_id
        return channels.get(channel_id).get_name();
    }

    public Message[] get_messages(int channel_id) {
        return channels.get(channel_id).get_messages();
    }

    public String get_username() {
        return this.username;
    }

    public void start_call(int channel_id) throws Exception {
        client_out.write(EncryptionManager.encrypt_message("CALL " + channel_id, server_public_key));
        client_out.write('\n');
        client_out.flush();
    }

    public void join_call(int channel_id) throws Exception {
        client_out.write(EncryptionManager.encrypt_message("CONNECT " + channel_id, server_public_key));
        client_out.write('\n');
        client_out.flush();

        String in;
        ArrayList<String> usernames = new ArrayList<String>();
        ArrayList<String> ips = new ArrayList<String>();

        while (!((in = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key)).equals("LSDONE"))){
            String[] args = in.split(" ");
            usernames.add(args[0]);
            ips.add(args[1]);
        }
        callHandler.connect_on_join_call(usernames.toArray(new String[usernames.size()]), ips.toArray(new String[ips.size()]));
        in_call = true;
    }

    public void leave_call(int channel_id) throws Exception {
        client_out.write(EncryptionManager.encrypt_message("DISCONNECT " + channel_id, server_public_key));
        client_out.write('\n');
        client_out.flush();
    }

    public void request_messages(int channel_id) throws Exception {
        System.out.println("Requesting messages");
        client_out.write(EncryptionManager.encrypt_message("Get MSSG " + channel_id, server_public_key));
        client_out.write('\n');
        client_out.flush();

        System.out.println("Sent request for messages");

        String in = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key);
        System.out.println("First line received: " + in);
        while (!in.equals("NONE") && !in.equals("LSDONE")){
            System.out.println("Reading message id: " + in);
            int id = Integer.parseInt(in);
            in = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key);
            System.out.println("Username: " + in);
            String message_username = in;
            in = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key);
            System.out.println("Datetime" + in);
            LocalDateTime datetime = LocalDateTime.parse(in, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            // Iterate over all message lines until message done
            String message = "";
            while (!((in = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key)).equals("MSDONE"))){
                message += in + "\n";
                System.out.println("Message line: " + in);
            }
            System.out.println("Got MSDONE, reading next...");
            channels.get(channel_id).addMessage(id, new Message(message.trim(), message_username, datetime));
            in = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key);
        }
    }   

    public void request_channels() throws Exception{
        String to_send = "GET CHANNELS";
        client_out.write(EncryptionManager.encrypt_message(to_send, server_public_key));
        client_out.write("\n");
        client_out.flush();

        ArrayList<Integer> channel_ids = new ArrayList<Integer>();
        String in = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key);
        
        while (!in.equals("NONE") && !in.equals("LSDONE")){
            System.out.println(in);
            channel_ids.add(Integer.parseInt(in));
            in = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key);

        }
        System.out.println("MSDONE read");
                                          
        //Request the data regarding each channel
        for (int i = 0; i < channel_ids.size(); i++){
            client_out.write(EncryptionManager.encrypt_message("GET CHANNEL_DATA " + channel_ids.get(i), server_public_key));
            client_out.write("\n");
            client_out.flush();

            String channel_name = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key);
            ArrayList<String> users = new ArrayList<String>();
            in = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key);
            
            //Read all users that are in a channel
            while (!in.equals("LSDONE")){
                if (!in.equals("username")){
                    users.add(in);
                    in = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key);
                }
            }
            
            channels.put(channel_ids.get(i), new ChannelContainer(channel_name, users.toArray(new String[users.size()])));
        }
        System.out.println("Request channels finished");
    }
    
    //GUI CALL FUNCTION
    public boolean register_account(String username, String password) throws Exception{
        String to_send = "REG " + username + " " + password;
        client_out.write(EncryptionManager.encrypt_message(to_send, server_public_key));
        client_out.write('\n');
        client_out.flush();

        String[] msg = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key).split(" ");
        if (msg[0].equals("REG") && msg.length == 2){
            //Server sent back an appropriate response
            if (msg[1].equals("OK")){
                return true; //If login okay
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //GUI CALL FUNCTION
    public boolean login(String username, String password) throws Exception{
        //Attempt to log the user in, returns false if it failed, true if successful. It also
        String to_send = "AUTH " + username + " " + password;
        client_out.write(EncryptionManager.encrypt_message(to_send, server_public_key));
        client_out.write('\n');
        client_out.flush();
        
        String[] msg = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key).split(" ");
        if (msg[0].equals("AUTH") && msg.length == 2){
            //Server sent back expected response
            if (msg[1].equals("OK")){
                //Authentication succeeded
                this.username = username;
                return true;
            } else {
                //Authentication failed
                return false;
            }
        }
        //Server ended up sending an incorrect message thus authentication failed. It should be unreachable
        return false;
    }
    
    //GUI CALL FUNCTION
    public boolean send_message(String message, int channel_id) throws Exception{
        String[] msg_lines = message.split("\n");
        
        client_out.write(EncryptionManager.encrypt_message("PUT " + channel_id, server_public_key));
        client_out.write("\n");
        client_out.flush();
        System.out.println("Writing message lines");
        for (int i = 0; i < msg_lines.length; i++){
            client_out.write(EncryptionManager.encrypt_message(msg_lines[i], server_public_key));
            client_out.write("\n");
            client_out.flush();
        }
        System.out.println("Writing MSDONE");
        client_out.write(EncryptionManager.encrypt_message("MSDONE", server_public_key));
        client_out.write("\n");
        client_out.flush();

        //Check if the put was okay
        String[] in = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key).split(" ");
        if (in[0].equals("PUT") && in.length == 2){
            //Server sent correct response
            if (!(in[1].equals("-1"))){
                //Add the message to the channel data
                channels.get(channel_id).addMessage(Integer.parseInt(in[1]), new Message(message, username, LocalDateTime.now()));
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //GUI FUNCTION CALL
    public int create_channel(String channel_name) throws Exception{
        //Returns the channel_id, returns -1 if creating the group failed
        String to_send = "MAKE " + channel_name;
        client_out.write(EncryptionManager.encrypt_message(to_send, server_public_key));
        client_out.write('\n');
        client_out.flush();

        String[] msg = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key).split(" ");
        System.out.println(msg[0] + msg[1]);
        if (msg[0].equals("MAKE") && msg.length == 2){
            //Server sent back correct message
            if (Integer.parseInt(msg[1]) != -1){
                //Add the channel to channel list if made successfully
                channels.put(Integer.parseInt(msg[1]), new ChannelContainer(channel_name, username));
            }
            return Integer.parseInt(msg[1]);

        } else {
            return -1;
        }
        
    }
    
    public boolean leave_channel(int channel_id) throws Exception{
        client_out.write(EncryptionManager.encrypt_message("LEAVE " + channel_id, server_public_key));
        client_out.write('\n');
        client_out.flush();

        String[] msg = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key).split(" ");
        if (msg[0].equals("LEAVE") && msg.length == 2){
            //Server sent correct response
            if (msg[1].equals("OK")){
                // Remove Channel Container from channels hashmap
                channels.remove(channel_id);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    public boolean join_channel(int channel_id) throws Exception{
        client_out.write(EncryptionManager.encrypt_message("JOIN " + channel_id, server_public_key));
        client_out.write('\n');
        client_out.flush();

        String[] msg = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key).split(" ");
        if (msg[0].equals("JOIN") && msg.length == 2){
            //Server sent correct response
            if (msg[1].equals("OK")){
                //Request channel data and add it to the channels hashmap
                client_out.write(EncryptionManager.encrypt_message("GET CHANNEL_DATA " + channel_id, server_public_key));
                client_out.write("\n");
                client_out.flush();

                String channel_name = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key);
                ArrayList<String> users = new ArrayList<String>();
                String in = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key);
                //Read in all users that are a part of the channel
                while (!in.equals("LSDONE")){
                    if (!in.equals(username)){
                        users.add(in);
                    }
                    in = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key);
                }
                channels.put(channel_id, new ChannelContainer(channel_name, users.toArray(new String[users.size()])));
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void unmute_microphone(){
        microphone_enabled = true;
    }
    public void mute_microphone(){
        microphone_enabled = false;
    }
}
