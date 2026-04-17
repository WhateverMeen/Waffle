import java.io.*;
import java.net.*;

import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;

import java.util.Base64;

import java.sql.ResultSet;

public class ClientHandler implements Runnable{
    private Server mainServer;

    private Socket socket;
    private PublicKey server_public_key;
    private PrivateKey server_private_key;
    private PublicKey client_public_key;

    private boolean running;
    private boolean authorised;
    private String username;
    private int user_id;

    private BufferedReader server_in;
    private BufferedWriter server_out;

    public ClientHandler(Socket client_socket, Server server){
        socket = client_socket;
        main_server = server;
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


    private void authenticate_user(String username, String password){
        ResultSet rs = SQLManager.execute_query("SELECT user_id, username, password FROM Users WHERE username = " + username, false);
        if (rs.next()){
            //User with such username exists
            if (rs.getNString(2).equals(username) && rs.getNString(3).equals(password)){
                //Set clients status to authorised
                authorised = true;
                this.username = username;
                user_id = rs.getInt(1);

                //Let client know authentication succeded
                server_out.write(EncryptionManager.encrypt_message("AUTH OK", client_public_key));
                server_out.write('\n');
                server_out.flush();
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
    }

    private void register_user(String username, String password){
        ResultSet rs = SQLManager.execute_query("SELECT username FROM users", false);
        if (rs != null){   
            int rows = rs.last(); //Get total count of rows
            rs.absolute(0); //Get absolute
            boolean user_exists = false;
            //Iterate through all rows in the result to see if username already exists
            do {
                if (rs.getNString(1) == username){
                    user_exists = true;
                    break; //Leave loop early if user is in the list
                }
            } while (rs.next()); //Repeat as long as there is a next row
            }

            if (!user_exists){
                //User does not exist hence send REG OK to client 
                //Find the highest user_id
                rs = SQLManager.execute_query("SELECT user_id FROM Users ORDER BY user_id DESC", false);
                
                if(rs.next()){
                    //There is already users in the database
                    int user_id = rs.getInt(1) + 1;
                } else {
                    //There is no users in the database
                    int user_id = 0;
                }
                //Add user to db
                rs = SQLManager.execute_query("INSERT INTO users (user_id, username, password) VALUES (" + user_id + ", " + username + "," + password + ")", true);
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
            server_out.write(EncryptionManager.encrypt_message("ERR"));
            server_out.write('\n');
            server_out.flush();
        }
    }

    private void get_channels(){
        ResultSet rs = SQL.execute_query("SELECT uic.channel_id
                FROM Users u
                JOIN Users_in_channel uic ON u.user_id = uic.user_id
                WHERE u.username = " + username, false);
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
    }

    private void get_channel_data(int channel_id){
        ResultSet rs = SQLQuery.Execute_query("SELECT name FROM Channels WHERE channel_id = " + channel_id, false);
        if (rs.next()){
            //channel exists
            server_out.write(EncryptionManager.encrypt_message(rs.getNString(1), client_public_key));
            server_out.write('\n');
            server_out.flush();

            rs = SQLQuery.Execute_query("SELECT u.username 
                    FROM Users u 
                    JOIN Users_in_channel uic ON u.user_id = uic.user_id
                    WHERE uic.channel_id = " + channel_id, false);
            rs.next(); //Get to the first row, should always work as channel exists
            //Return all users to client
            do {
                server_out.write(EncryptionManager.encrypt_message(rs.getNString(1), client_public_key));
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
    }
    
    /*TODO
    private void start_call(int channel_id){

    }
    */

    private String[] get_messages(int channel_id){

    }

    private boolean put_message(int channel_id, String message){

    }

    private boolean join_channel(int channel_id){
        ResultSet rs = SQLManager.execute_query("SELECT channel_id FROM Channels WHERE channel_id = " + channel_id, false);
        if (rs.next()){
            //Channel exists
            
            //Make sure user is not already in the channel
            rs = SQLManager.executy_query("SELECT user_id FROM Users_in_channels WHERE user_id = " + user_id, false);
            if (!rs.next()){
                //User not in channel
                SQLManager.execute_query("INSER INTO Users_in_channels (user_id, channel_id) VALUES (" + user_id + ", " + channel_id + ")", true);
                server_out.write(EncryptionManager.encrypt_message("JOIN OK", client_public_key));
                server_out.write('\n');
                server_out.flush();
            } else {
                //User already in channel
                server_out.write(EncryptionManager.encrypt_message("JOIN BAD", client_public_key));
                server_out.write('\n');
                server_out.flush();
            }
        } else {
            //No such channel exists
            server_out.write(EncryptionManager.encrypt_message("JOIN BAD", client_public_key));
            server_out.write('\n');
            server_out.flush();
        }
    }

    private boolean leave_channel(int channel_id){
        ResultSet rs = SQLManager.execute_query("SELECT channel_id FROM channels WHERE channel_id = " + channel_id, false);
        if (rs.next()){
            //Channel exists
            //Check if user in channel
            rs = SQLManager.execute_query("SELECT channel_id FROM channels WHERE channel_id = " + channel_id + " AND user_id = " + user_id, false);
            if (rs.next()){
                //User in channel
                SQLManager.execute_query("DELETE FROM Users_in_channels WHERE user_id = " + user_id + " AND channel_id = " + channel_id, true);
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
    }

    private int create_channel(String channel_name){
        //Get a new channel_id
        ResultSet rs = SQLManager.execute_query("SELECT channel_id FROM channels ORDER BY channel_id DESC");
        if (rs.next()){
            //There is already channels in the db
            int channel_id = rs.getInt(1) + 1;
        } else {
            //No channels in db
            int channel_id = 0;
        }
        //Add channel to channels
        SQLManager.execute_query("INSERT INTO channels (channel_id, name) VALUES (" + channel_id + ", " + channel_name + ")", true);
        
        //Add user to channel
        SQLManager.execute_query("INSERT INTO Users_in_channels (user_id, channel_id) VALUES (" + user_id + ", " + channel_id + ")", true);
        //Let user know channel has been made
        server_out.write(EncryptionManager.encrypt_message("MAKE " + channel_id, client_public_key));
        server_out.write('\n');
        server_out.flush();
    }

    private void delete_channel(int channel_id){

    }


}
