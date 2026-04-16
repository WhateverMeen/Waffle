import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;

public class Client{
    private final String SERVER_HOST = "localhost";
    private final int SERVER_PORT = 4567;


    //State variables
    private String username;
    private HashMap<Integer, ChannelContainer> channels; //Holds information regarding chats
    private boolean microphone_enabled;
    private boolean camera_enabled;
    private PublicKey client_public_key;
    private PrivateKey client_private_key;
    private PublicKey server_public_key;

    private Stack<String[]> NotificationStack;

    //Server communication values
    private Socket socket;
    private BufferedReader client_in;
    private BufferedWriter client_out;


    public Client() throws Exception{
        //Initialise state variables
        channels = new HashMap<Integer, ChannelContainer>();
        microphone_enabled = false;
        camera_enabled = false;

        //Create encryption keys
        KeyPair keys = EncryptionManager.get_keys();
        client_public_key = keys.getPublic();
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
        client_out.write("HELO " + Base64.getEncoder().encodeToString(client_public_key.getEncoded()) + "\n");//Encode the public key into bytes
        client_out.flush();
        String[] msg = client_in.readLine().split(" ");
        if (msg[0].equals("HELO") && msg.length == 2){
            server_public_key = EncryptionManager.public_key_from_string(msg[1]); //Extract servers public key from the message received
        }
    }

    public void quit(){
        try{
            socket.close();
            client_in.close();
            client_out.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    //private void request_messages();

    private void request_channels() throws Exception{
        String to_send = "GET CHANNELS";
        client_out.write(EncryptionManager.encrypt_message(to_send, server_public_key));
        client_out.write('\n');
        client.flush();
        
        String[] msg = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key).split(" ");
        if (!msg[0].equals("NONE"){
            //Make sure user is in some channels
            for (int i = 0; i < msg.length; i++){
                //Iterate over all channels returned and request necessary data for them
                to_send = "GET CHANNEL_DATA " + String.valueOf(i);
                client_out.write(EncryptionManager.encrypt_message(to_send, server_public_key));
                client_out.write('\n');
                client_out.flush();

                String[] data = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key).split(" ");
                String[] users = new String[msg.length - 1];
                for (int j = 1; j < data.length; j++){
                    //Add a user to users list if it is not the client
                    if (!data[j].equals(username)){
                        users.add(data[j]);
                    }
                }
                channels.put(msg[i], new ChannelContainer(data[0], users);
            }
        }
    }
    
    //GUI CALL FUNCTION
    public boolean register_account(String username, String password, String email){
        String to_send = "REG " + username + " " + password + " " + email;
        client_out.write(EncryptionManager.encrypt_message(to_send, server_public_key));
        client_out.write('\n');
        client_out.flush();

        String[] msg = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key).split(" ");
        if (msg[0].equals("REG") && msg.length == 2){
            //Server sent back an appropriate response
            if (msg[1].equals("OK"){
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
                request_channels();
                return true;
            } else {
                //Authentication failed
                return false;
            }
        }
        //Server ended up sending an incorrect message thus authentication failed. It should be unreachable
        return false;
    }

    //public boolean reset_password(String old_password, String new_password);
    
    //GUI CALL FUNCTION
    public boolean send_message(String message, int channel_id){
        String to_send = "PUT " + String.valueOf(channel_id) + " " + message;
        client_out.write(EncryptionManager.encrypt_message(to_send, server_public_key));
        client_out.write('\n');
        client_out.flush();

        String[] msg = EncryptionManager.decrypt_message(to_send, client_private_key));
        if (msg[0].equals("PUT") && msg.length == 2){
            //Server replied with correct message
            if (msg[1].equals("OK")){
                //Sending the message succeded
                channels.get(channel_id).addMessage(message); //Add the message to the channelContainer
                return true;
            } else {
                //Sending a message failed
                return false;
            }
        }
        return false;
    }

    //public send_photo();
    //public boolean delete_message(Date date, int channel_id);
    //public boolean edit_message(Date date, int channel_id);
    
    //GUI FUNCTION CALL
    public int create_channel(String group_name){
        //Returns the channel_id, returns -1 if creating the group failed
        String to_send = "MAKE CHANNEL " + group_name;
        client_out.write(EncryptionManager.encrypt_message(to_send, server_public_key);
        client_out.write('\n');
        client_out.flush();

        String[] msg = EncryptionManager.decrypt_message(to_send, client_private_key));
        if (msg[0].equals("MAKE") && msg.length == 2){
            //Server sent back correct message
            return Integer.parseInt(msg[1]);
        } else {
            return -1;
        }
        
    }
    
    
    //public boolean leave_group(int channel_id);
    //public boolean  join_group(int channel_id);
    //public void delete_group(int channel_id);
    //public boolean start_call(int channel_id);
    //public void leave_call();
    //public void join_call(channel_id);


    public void unmute_microphone(){
        microphone_enabled = true;
    }
    public void mute_microphone(){
        microphone_enabled = false;
    }
    public void enable_camera(){
        camera_enabled = true;
    }
    public void disable_camera(){
        camera_enabled = false;
    }
}
