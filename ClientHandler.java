import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;
import java.security.PrivateKey;

public class ClientHandler implements Runnable{
    private Socket socket;
    private PublicKey server_public_key;
    private PrivateKey server_private_key;
    private PublicKey client_public_key;

    private boolean running;
    private boolean authorised;
    private String username;

    private BufferedReader server_in;
    private BufferedWriter server_out;

    public ClientHandler(Socket clientSocket){
        socket = clientSocket;
        authorised = false;
    }

    public void run(){
        try {
            //Generate the keys for encryption
            KeyPair kp = EncryptionManager.get_keys();
            server_public_key = kp.getPublic();
            server_private_key = kp.getPrivate();
            
            //Make input and output streams
            server_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            server_out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            
            //Do the server handshake
            String[] msg = server_in.readLine().split(" ");
            if (msg[0].equals("HELO") && msg.length == 2){
                //Client sent the correct message
                client_public_key = EncryptionManager.public_key_from_string(msg[1]);
                server_out.write("HELO " + Base64.getEncoder().encodeToString(server_public_key.getEncoded()) + "\n");
                server_out.flush();
            } else {
                //Client sent an incorrect request
                server_out.write("INVALID REQ\n");
                server_out.flush();
            }
            
            running = true;
            while (running){
                msg = EncryptionManager.decrypt_message(server_in.readLine(), server_private_key).split(" ");
                interpret_command(msg);
            }
            socket.close();
            server_in.close();
            server_out.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private void interpret_command(String[] command){
        if (command[0].equals("AUTH")){
            //User is attempting to log in
            if (command.length == 3){
                authenticate_user(command[1], command[2]);
            } else {
                server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
            }
        } else if (command[0].equals("REG")){
            //User is attempting to register a new command
            if (command.length == 4){
                register_user(command[1], command[2], command[3]);
            } else {
                server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
            }
        } else if (command[0].equals("GET")){
            //User wants to request data
            //Make sure the user has logged in
            if (authorised){
                if (command[1].equals("MSG")){
                    //Retrieve messages for client
                    if (command.length == 2){
                        get_messages(Integer.parseInt(command[1]));
                    } else {
                        server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
                    }
                } else if (command[1].equals("CHANNELS")){
                    //Retrieve channels for client
                    if (command.length == 2){
                        get_channels();
                    } else {
                        server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
                    }
                } else if (command[1].equals("CHANNEL_DATA")){
                    if (command.length == 3){
                        //Retrieve channels for client
                        get_channel_data(Integer.parseInt(command[2]));
                    } else {
                        server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
                    }
                } else {
                    server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
                }
            } else {
                server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
            }
        } else if (command[0].equals("PUT")){
            //User wants to send a message
            if (authorised){
                if (command.length == 3){
                    String temp;
                    String userMessage = "";
                    while (!(temp = server_in.readLine()).equals("MSDONE")){
                        userMessage += temp + '\n';
                    }
                    put_message(Integer.parseInt(command[1]), userMessage)
                } else {
                    server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
                }
            } else {
                server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
            }
        } else if (command[0].equals("MAKE")){
            //User wants to create a channel
            //Make sure the user is authorised
            if (authorised){
                if (command.length == 2){
                    create_channel(command[1]);
                } else {
                    server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
                }
            } else {
                server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
            }
        } else if (command[0].equals("JOIN")){
            //User wants to join a channel
            //Check if user authorised
            if (authorised){
                if (command.length == 2){
                    join_channel(Integer.parseInt(command[1]));
                } else {
                    server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
                }
            } else {
                server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
            }
        } else if (command[0].equals("LEAVE")){
            //User would like to leave a channel
            if (authorised){
                if (command.length == 2){
                    leave_channel(Integer.parseInt(command[1]));
                } else {
                    server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
                }
            } else {
                server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
            }
        } else if (command[0].equals("CALL")){
            //User wants to make a call
            if (authorised){
                if (command.length == 2){
                    start_call(Integer.parseInt(command[1]));
                } else {
                    server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
                }
            } else {
                server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
            }
        } else if (command[0].equals("END")){
            running = false;
        } else {
            server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
        }
    }

    private void register_user(String username, String password, String email){

    }
    private String[] get_channels(){

    }
    private String[] get_channel_data(int channel_id){

    }
    private void start_call(int channel_id){

    }
    private String[] get_messages(int channel_id){

    }
    private boolean put_message(int channel_id, String message){

    }
    private boolean join_channel(int channel_id){

    }
    private boolean leave_channel(int channel_id){

    }
    private int create_channel(String channel_name){

    }
    private void delete_channel(int channel_id){

    }
    private boolean authenticate_user(String username, String password){

    }
}
