import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable{
    private Socket socket;
    private PublicKey server_public_key;
    private PrivateKey server_private_key;
    private PublicKey client_public_key;
    private boolean running;


    public ClientHandler(Socket clientSocket){
        socket = clientSocket;
    }

    public void run(){
        try {
            //Generate the keys for encryption
            KeyPair kp = EncryptionManager.get_keys();
            server_public_key = keys.getPublic();
            server_private_key = keys.getPrivate();
            
            //Make input and output streams
            Bufferedreader server_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter server_out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream());
            
            //Do the server handshake
            String[] msg = server_in.readLine();
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
                msg = EncryptionManager.decrypt_message(server_in.readLine(), server_private_key);
                interpret_command(msg.split(" "));
            }
            socket.close();
            server_in.close();
            server_out.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private void interpret_commands(String[] command){
        if (msg[0].equals("AUTH")){
            if (msg.length == 3){
                authenticate_user(msg[1], msg[2]);
            } else {
                server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
            }
        } else if (msg[0].equals("REG")){
            if (msg.length == 4){
                register_user(msg[1], msg[2], msg[3]);
            } else {
                server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
            }
        } else if (msg[0].equals("GET")){
            if (msg[1].equals("MSG")){
                
            } else if (msg[1].equals("CHANNELS")){
                if (msg.length == 2){
                    get_channels();
                } else {
                    server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
                }
            } else if (msg[1].equals("CHANNEL_DATA")){
                if (msg.length == 3){
                    get_channel_data(Integer.parseInt(msg[2]));
                } else {
                    server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
                }
            } else {
                server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
            }
        } else if (msg[0].equals("PUT")){
            if (msg.length == 3){
                put_message(Integer.parseInt(msg[1]), msg[2]);
            } else {
                server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
            }
        } else if (msg[0].equals("MAKE")){
            if (msg.length == 2){
                create_channel(msg[1]);
            } else {
                server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
            }
        } else if (msg[0].equals("JOIN")){
            if (msg.length == 2){
                join_channel(Integer.parseInt(msg[1]);
            } else {
                server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
            }
        } else if (msg[0].equals("LEAVE")){
            if (msg.length == 2){
                leave_channel(Integer.parseInt(msg[1]));
            } else {
                server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
            }
        } else if (msg[0].equals("END")){
            running = false;
        } else {
            server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
        }
    }

    private void register_user(String username, String password, String email);
    private String[] get_channels(String username);
    private String[] get_channel_data(int channel_id);
    private void start_call(int channel_id);
    private String[] get_messages(int channel_id, stack_top);
    private boolean put_message(int channel_id, String username, String message);
    private String[] get_channels(String username);
    private boolean join_channel(int channel_id);
    private boolean leave_channel(int channel_id);
    private int create_channel(String channel_name);
    private void delete_channel(int channel_id);
    private boolean authenticate_user(String username, String password);
     
}
