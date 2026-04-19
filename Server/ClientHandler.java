import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;

import java.util.Base64;

import java.sql.ResultSet;

public class ClientHandler extends Thread{
    private Server mainServer;
    private int unauthorised_id;

    private Socket socket;
    private PublicKey server_public_key;
    private PrivateKey server_private_key;
    private PublicKey client_public_key;

    public boolean running;
    private boolean authorised;
    private String username;
    private int user_id;

    private BufferedReader server_in;
    private BufferedWriter server_out;

    public ClientHandler(Socket client_socket, Server server, int id){
        socket = client_socket;
        mainServer = server;
        authorised = false;
        unauthorised_id = id;
    }

    public void kill_self(){
        if (authorised){
            mainServer.kill_client(user_id, authorised);
        } else {
            mainServer.kill_client(unauthorised_id, authorised);
        }
        try{
            socket.close();
        } catch (Exception e){
            System.out.println("Failed to close connection when killing thread");
        }
        running = false;
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
            //WATING FOR HELLO
            String[] msg = server_in.readLine().split(" ");
            while (msg == null){
                //CLient disconnected, exit early
                System.out.println("Client disconnected");
                kill_self();
                return;
            } 
            
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
                String read = server_in.readLine();
                if (read == null){
                    //Client disconnected
                    System.out.println("Client Disconnected");
                    kill_self();
                    return;
                    
                }
                String recieved = EncryptionManager.decrypt_message(read, server_private_key);
                msg =  recieved.split(" ");
                System.out.println(recieved);
                interpret_command(msg);
            }
            socket.close();
            server_in.close();
            server_out.close();
            kill_self();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void interpret_command(String[] command){      
        try {
            if (command[0].equals("AUTH")){
                //User is attempting to log in
                if (command.length == 3){
                    authenticate_user(command[1], command[2]);
                } else {
                    try {
                        server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (command[0].equals("REG")){
                //User is attempting to register a new command
                if (command.length == 3){
                    register_user(command[1], command[2]);
                } else {
                    server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
                }
            } else if (command[0].equals("GET")){
                //User wants to request data
                //Make sure the user has logged in
                if (authorised){
                    if (command[1].equals("MSG")){
                        //Retrieve messages for client
                        if (command.length == 3){
                            get_messages(Integer.parseInt(command[2]));
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
                    if (command.length == 2){
                        String temp;
                        String userMessage = "";
                        while (!(temp = EncryptionManager.decrypt_message(server_in.readLine(), server_private_key)).equals("MSDONE")){
                            System.out.println(temp);
                            userMessage += temp + '\n';
                        }
                        System.out.println(temp);
                        userMessage.substring(0, userMessage.length() - 1); //Remove the last \n
                        put_message(Integer.parseInt(command[1]), userMessage);
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
                        try {
                            server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                } else {
                    try{
                        server_out.write(EncryptionManager.encrypt_message("INVALID REQUST", client_public_key) + '\n');
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (command[0].equals("CALL")){
                //User wants to make a call
                if (authorised){
                    if (command.length == 2){
                        //start_call(Integer.parseInt(command[1]));
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
        } catch (Exception e){
            e.printStackTrace();
            kill_self();
        }
    }


    private void authenticate_user(String username, String password){
        try {
            ResultSet rs = SQLManager.execute_query("SELECT user_id, username, password FROM Users WHERE username = '" + username + "'", false);
            if (rs.next()){
                //User with such username exists
                if (rs.getString(2).equals(username) && rs.getString(3).equals(password)){
                    //Set clients status to authorised
                    authorised = true;
                    this.username = username;
                    System.out.println("User_id of client: " + user_id);
                    //Let client know authentication succeded
                    server_out.write(EncryptionManager.encrypt_message("AUTH OK", client_public_key));
                    server_out.write('\n');
                    server_out.flush();
                    mainServer.authorise_client(user_id, unauthorised_id);
                } else {
                    server_out.write(EncryptionManager.encrypt_message("AUTH BAD", client_public_key));
                    server_out.write('\n');
                    server_out.flush();               
                }
            } else {
                //No user with such username exists
                server_out.write(EncryptionManager.encrypt_message("AUTH BAD", client_public_key));
                server_out.write('\n');
                server_out.flush();
            }
        } catch (Exception e){
            kill_self();
            e.printStackTrace();
        }
    }

    private void register_user(String username, String password){
        System.out.println("Attempting to register user");
        try {
            ResultSet rs = SQLManager.execute_query("SELECT username FROM users", false);
            if (rs != null){
                if (rs.next()){
                    boolean user_exists = false;
                    //Iterate through all rows in the result to see if username already exists
                    do {
                        if (rs.getString(1).equals(username)){
                            user_exists = true;
                            break; //Leave loop early if user is in the list
                        }
                    } while (rs.next()); //Repeat as long as there is a next row

                    if (!user_exists){
                        //User does not exist hence send REG OK to client 
                        //Find the highest user_id
                        rs = SQLManager.execute_query("SELECT user_id FROM Users ORDER BY user_id DESC", false);
                        int user_id_to_assign;
                        rs.next();
                        user_id_to_assign = rs.getInt(1) + 1;
                        //Add user to db
                        SQLManager.execute_query("INSERT INTO users (user_id, username, password) VALUES (" + user_id_to_assign + ", '" + username + "', '" + password + "')", true);
                        //Send ok to user
                        server_out.write(EncryptionManager.encrypt_message("REG OK", client_public_key));
                        server_out.write('\n');
                        server_out.flush();
                    } else {
                        server_out.write(EncryptionManager.encrypt_message("REG BAD", client_public_key));
                        server_out.write('\n');
                        server_out.flush();
                    }
                } else {
                    SQLManager.execute_query("INSERT INTO users (user_id, username, password) VALUES (" + 0 + ", '" + username + "', '" + password + "')", true);
                        server_out.write(EncryptionManager.encrypt_message("REG OK", client_public_key));
                        server_out.write('\n');
                        server_out.flush();
                }
            } else {
                server_out.write(EncryptionManager.encrypt_message("INVALID REQUEST", client_public_key));
                server_out.write('\n');
                server_out.flush();
            }
        } catch (Exception e){
            kill_self();
            e.printStackTrace();
        }
    }

    private void get_channels(){
        try {
            ResultSet rs = SQLManager.execute_query("SELECT uic.channel_id FROM Users u JOIN Users_in_channel uic ON u.user_id = uic.user_id WHERE u.username = '" + username + "'", false);
            if (rs.next()){ 
                //if table not empty loop through all channels and send them to client
                do {
                    int channel = rs.getInt(1);
                    server_out.write(EncryptionManager.encrypt_message(String.valueOf(channel), client_public_key));
                    server_out.write('\n');
                    server_out.flush();
                } while (rs.next());
                //Let the client know youre done
                server_out.write(EncryptionManager.encrypt_message("LSDONE", client_public_key));
                server_out.write('\n');
                server_out.flush();
            } else {
                //No channels found, let the client know
                server_out.write(EncryptionManager.encrypt_message("NONE", client_public_key));
                server_out.write('\n');
                server_out.flush();
            } 
        }catch (Exception e) {
                e.printStackTrace();
        }
    }

    private void get_channel_data(int channel_id){
        try {
            System.out.println("Retrieving channel data for channel: " + channel_id);
            ResultSet rs = SQLManager.execute_query("SELECT name FROM Channels WHERE channel_id = " + channel_id, false);
            if (rs.next()){
                //channel exists
                System.out.println(rs.getString(1));
                server_out.write(EncryptionManager.encrypt_message(rs.getString(1), client_public_key));
                server_out.write('\n');
                server_out.flush();

                rs = SQLManager.execute_query("SELECT u.username FROM Users u JOIN Users_in_channel uic ON u.user_id = uic.user_id WHERE uic.channel_id = " + channel_id, false);
                rs.next(); //Get to the first row, should always work as channel exists
                //Return all users to client
                do {
                    System.out.println(rs.getString(1));
                    server_out.write(EncryptionManager.encrypt_message(rs.getString(1), client_public_key));
                    server_out.write('\n');
                    server_out.flush();
                } while (rs.next());
                
                //Let client know youre done
                server_out.write(EncryptionManager.encrypt_message("LSDONE", client_public_key));
                server_out.write('\n');
                server_out.flush();
            } else {
                //Channel does not exist
                server_out.write(EncryptionManager.encrypt_message("NONE", client_public_key));
                server_out.write('\n');
                server_out.flush();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    /*TODO
    private void start_call(int channel_id){

    }
    */

    private String[] get_messages(int channel_id){
        return null;
    }

    private void put_message(int channel_id, String message){
        try {
            //Retrieve highest message_id
            ResultSet rs = SQLManager.execute_query("SELECT message_id FROM Messages ORDER BY message_id DESC", false);
            int msg_id;
            System.out.println("Attempting put msg");
            if (!rs.next()){
                //No messages exist
                msg_id = 0;
            } else {
                msg_id = rs.getInt(1) + 1;
            }
            LocalDateTime now = LocalDateTime.now();
            String formatted = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            SQLManager.execute_query("INSERT INTO Messages (message_id, channel_id, user_id, date, message) VALUES (" + msg_id + ", " + channel_id + ", " + user_id + ", '" + formatted + "', '" + message + "')", true);
            server_out.write(EncryptionManager.encrypt_message("PUT OK", client_public_key));
            server_out.write('\n');
            server_out.flush();
        } catch (Exception e){
            try {
                server_out.write(EncryptionManager.encrypt_message("PUT BAD", client_public_key));
                server_out.write('\n');
                server_out.flush();
            } catch (Exception ex){
                e.printStackTrace();
                kill_self();
            }
        }
        
    }

    private boolean join_channel(int channel_id){
        try {
            ResultSet rs = SQLManager.execute_query("SELECT channel_id FROM Channels WHERE channel_id = " + channel_id, false);
            if (rs.next()){
                //Channel exists
                //Make sure user is not already in the channel
                rs = SQLManager.execute_query("SELECT user_id FROM Users_in_channel WHERE user_id = " + user_id  + " AND channel_id = " + channel_id, false);
                if (!rs.next()){
                    //User not in channel
                    SQLManager.execute_query("INSERT INTO Users_in_channel (user_id, channel_id) VALUES (" + user_id + ", " + channel_id + ")", true);
                    server_out.write(EncryptionManager.encrypt_message("JOIN OK", client_public_key));
                    server_out.write('\n');
                    server_out.flush();
                    return true;
                } else {
                    //User already in channel
                    server_out.write(EncryptionManager.encrypt_message("JOIN BAD", client_public_key));
                    server_out.write('\n');
                    server_out.flush();
                    return false;
                }
            } else {
                //No such channel exists
                server_out.write(EncryptionManager.encrypt_message("JOIN BAD", client_public_key));
                server_out.write('\n');
                server_out.flush();
                return false;
            }
        } catch (Exception e){
            kill_self();
            running = false;
            return false;
        }
    }

    private void leave_channel(int channel_id){
        try {
            ResultSet rs = SQLManager.execute_query("SELECT channel_id FROM channels WHERE channel_id = " + channel_id, false);
            if (rs.next()){
                //Channel exists
                //Check if user in channel
                rs = SQLManager.execute_query("SELECT channel_id FROM Users_in_channel WHERE channel_id = " + channel_id + " AND user_id = " + user_id, false);
                if (rs.next()){
                    //User in channel
                    SQLManager.execute_query("DELETE FROM Users_in_channel WHERE user_id = " + user_id + " AND channel_id = " + channel_id, true);
                    server_out.write(EncryptionManager.encrypt_message("LEAVE OK", client_public_key));
                    server_out.write('\n');
                    server_out.flush();
                } else {
                    //User not in channel
                    server_out.write(EncryptionManager.encrypt_message("LEAVE BAD", client_public_key));
                    server_out.write('\n');
                    server_out.flush();
                }
            } else {
                //Channel does not exist
                server_out.write(EncryptionManager.encrypt_message("LEAVE BAD", client_public_key));
                server_out.write('\n');
                server_out.flush();
            } 
        } catch (Exception e){
            kill_self();
            e.printStackTrace();
        }
    }

    private int create_channel(String channel_name){       
        System.out.println("Creating a channel requested");
        try {
            //Get a new channel_id
            ResultSet rs = SQLManager.execute_query("SELECT channel_id FROM channels ORDER BY channel_id DESC", false);
            int channel_id;
            if (rs.next()){
                //There is already channels in the db
                channel_id = rs.getInt(1) + 1;
            } else {
                //No channels in db
                channel_id = 0;
            }
            //Add channel to channels
            SQLManager.execute_query("INSERT INTO channels (channel_id, name) VALUES (" + channel_id + ", '" + channel_name + "')", true);
            
            //Add user to channel
            SQLManager.execute_query("INSERT INTO Users_in_channel (user_id, channel_id) VALUES (" + user_id + ", " + channel_id + ")", true);
            //Let user know channel has been made
            server_out.write(EncryptionManager.encrypt_message("MAKE " + channel_id, client_public_key));
            server_out.write('\n');
            server_out.flush();
            return channel_id;
    } catch (Exception e){
        kill_self();
        e.printStackTrace();
        return -1;
    }
}

    private void delete_channel(int channel_id){
        
    }


}
