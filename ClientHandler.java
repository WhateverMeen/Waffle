import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable{
    private Socket socket;
    private PublicKey server_public_key;
    private PrivateKey server_private_key;
    private PublicKey client_public_key;

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

            while (true){
                
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private String[] get_friends(String username);
    private String[] get_channels(String username);
    private void start_call(int channel_id);
    private String[] get_messages(int channel_id, stack_top);
    private boolean put_message(int channel_id, String username, String message);
    //private boolean send_friend_request(String username, String friend_username);
    //private boolean accept_friend_request(String username, String friend_username);
    //private void deny_friend_request(String username, String friend_username);
    //private String[] get_friends_requests(String username);
    //Private String[] get_friend_list(String username);
    private String[] get_channels(String username);
    private int create_channel(String channel_name, String username);
    private void delete_channel(int channel_id);
    private boolean authenticate_user(String username, String password);
     
}
