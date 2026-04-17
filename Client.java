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
        client_out.write("HELO " + Base64.getEncoder().encodeToString(keys.getPublic().getEncoded()) + "\n");//Encode the public key into bytes
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
        client_out.write("\n");
        client_out.flush();

        String in = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key);
        ArrayList<Integer> channel_ids = new ArrayList<Integer>();
        //Iterate over all returns
        while (!in.equals("LSDONE") && !in.equals("NONE")){
            channel_ids.add(Integer.parseInt(in));
            in = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key);
        }

        //Request the data regarding each channel
        for (int i = 0; i < channel_ids.size(); i++){
            client_out.write(EncryptionManager.encrypt_message("GET CHANNEL_DATA" + channel_ids.get(i), server_public_key));
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
    }
    
    //GUI CALL FUNCTION
    public boolean register_account(String username, String password, String email) throws Exception{
        String to_send = "REG " + username + " " + password + " " + email;
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
    
    //GUI CALL FUNCTION
    public boolean send_message(String message, int channel_id) throws Exception{
        String[] msg_lines = message.split("\n");
        
        client_out.write(EncryptionManager.encrypt_message("PUT " + channel_id, server_public_key));
        client_out.write("\n");
        client_out.flush();

        for (int i = 0; i < msg_lines.length; i++){
            client_out.write(EncryptionManager.encrypt_message(msg_lines[i], server_public_key));
            client_out.write("\n");
            client_out.flush();
        }

        client_out.write(EncryptionManager.encrypt_message("MSDONE", server_public_key));
        client_out.write("\n");
        client_out.flush();

        //Check if the put was okay
        String[] in = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key).split(" ");
        if (in[0].equals("PUT") && in.length == 2){
            //Server sent correct response
            if (in[1].equals("OK")){
                //Add the message to the channel data
                channels.get(channel_id).addMessage(message);;
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
        String to_send = "MAKE CHANNEL " + channel_name;
        client_out.write(EncryptionManager.encrypt_message(to_send, server_public_key));
        client_out.write('\n');
        client_out.flush();

        String[] msg = EncryptionManager.decrypt_message(to_send, client_private_key).split(" ");
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
    

     // SEND LEAVE COMMAND -> CHECK SERVER -> IF LEAVE OK -> RETURN TRUE channell ( hasmap , see paramet , ) -> Remove from Channels 
    
     public boolean leave_channel(int channel_id) throws Exception{
        client_out.write(EncryptionManager.encrypt_message("LEAVE SERVER" + channel_id, server_public_key));
        client_out.write('\n');
        client_out.flush();

        String[] msg = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key).split(" ");
        if (msg[0].equals("LEAVE SERVER") && msg.length == 2){
            //Server sent correct response
            if (msg[1].equals("OK")){
                // Remove Channel Container from channels hashmap
                channells.remove(channel_id)
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

     }


     // JUST WORK ON CLIENT MESSAGES [ IN -> OUT ] ; client in () , client out () ;
    
    // CLIENT OUT ; ENNCRYPT EVERYTHING ; FLUSH --- Message OUT 
    //                                          ----  MESSAGE IN ;
    
    

    
    //GUI CALL FUNCTION
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
                String in = EncryptionManager.decrypt_message(channel_name, client_private_key);
                //Read in all users that are a part of the channel
                while (!in.equals("LSDONE")){
                    if (!in.equals(username)){
                        users.add(in);
                    }
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

    //  SORT OUT LATER ;  IGNORE FOR NOW 
    //public boolean start_call(int channel_id);

    // REFERENCE 
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
