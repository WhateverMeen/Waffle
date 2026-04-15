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
    private HashMap<Integer, ChannelContainer> channels; //Holds information regarding chats
    private ArrayList<String> friends;
    private ArrayList<String> friend_requests;
    private boolean microphone_enabled;
    private boolean camera_enabled;
    private Socket socket;
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
        friends = new ArrayList<String>();
        friend_requests = new ArrayList<String>();
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

        }
    }
    
    //private void request_messages();
    private void request_friends() throws Exception{
        String to_send = "GET FRIENDS";
        client_out.write(EncryptionManager.encrypt_message(to_send, server_public_key));
        client_out.write('\n');
        client_out.flush();

        String[] msg = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key).split(" ");
        if (!msg[0].equals("NONE")){
            for (int i = 0; i < msg.length; i++){
                //If there are friends requests
                friends.add(msg[i]);
            }
        }
    }
    private void request_friend_requests() throws Exception{
        String to_send = "GET FRIENDS_REQ";
        client_out.write(EncryptionManager.encrypt_message(to_send, server_public_key));
        client_out.write('\n');
        client_out.flush();

        String[] msg = EncryptionManager.decrypt_message(client_in.readLine(), client_private_key).split(" ");
        if (!msg[0].equals("NONE")){
            //If there are friends requests
            for (int i = 0; i < msg.length; i++){
                friend_requests.add(msg[i]);
            }
        }
    }
    private void request_channels() throws Exception{
        //TODO CANNOT BE IMPLEMENTED UNTIL CHANNEL CONTAINER IS DONE
    }

    private void request_all_current_data() throws Exception{
        //Function to request all channels the user is in, their friends and friends request upon successful login
        request_friends();
        request_friend_requests();
        request_channels();
    }
    //public boolean register_account(String username, String password, String email){}
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
                request_all_current_data();
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
    //public boolean send_message(String message, int channel_id);
    //public send_photo();
    //public boolean delete_message(Date date, int channel_id);
    //public boolean edit_message(Date date, int channel_id);
    //public boolean create_group(String group_name);
    //public boolean leave_group(int channel_id);
    //public boolean  join_group(int channel_id);
    //public void delete_group(int channel_id);
    //public boolean start_call(int channel_id);
    //public void leave_call();
    //public void join_call(channel_id);
    //public boolean send_friend_request(String username);
    //public boolean accept_friend_request(String username);
    //public void deny_friend_request(String username);
    //public void remove_friend(String username);
    //public void block_user(String username);

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
