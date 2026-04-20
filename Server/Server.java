import java.net.*;
import java.io.*;

import java.util.concurrent.ConcurrentHashMap;

public class Server extends Thread{
    private final int PORT_NUMBER = 4567;

    //Concurrency safe hashmaps for storing the unauthorised clients and authorised clients currently connected
    //This allows for communication between threads when initiating a p2p call
    ConcurrentHashMap<Integer, ClientHandler> unauthorised_clients; //The key is unqiue id assigned to each client at creation
    ConcurrentHashMap<Integer, ClientHandler> authorised_clients; //The key is user_id of the client
    ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, CallParticipant>> ongoing_calls; //The key is a channel_id and the value is a hashmap mapping user_ids to callParticipants


    private int unauthorised_count;
    private boolean running;
    
    ServerSocket serverSocket;


    public Server(){
        unauthorised_clients = new ConcurrentHashMap<Integer, ClientHandler>();
        authorised_clients = new ConcurrentHashMap<Integer, ClientHandler>();
        unauthorised_count = 0;
    }

    public void run(){
        running = true;
        
        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
            System.out.println("Setting up server...");
            System.out.println("Running server! Server listening at: " + serverSocket.getInetAddress());
            while (running){
                //Wait for the client to connect
                Socket clientSocket = serverSocket.accept();
                //Add client to unauthorised clients and assign its id and then start the thread
                unauthorised_clients.put(unauthorised_count, new ClientHandler(clientSocket, this, unauthorised_count));
                unauthorised_clients.get(unauthorised_count).start();
                unauthorised_count++;
            }
        } catch (Exception e){

            if (running){
                //Server crashed
                e.printStackTrace();
            }
            SQLManager.close_con();
        }
    }
    
    public boolean start_call(int user_id, int channel_id){ // to fix
        //If there isnt an ongoing call, add a call to the list with the initial participant being the one with user_id passed
        //Notify all users that are connected to the server about a call
        if (ongoing_calls.containsKey(channel_id)){ // if ongoing call, returns false
            return false;
        }
        ConcurrentHashMap<Integer, CallParticipant> participants = new ConcurrentHashMap<>(); // create the new call
        ClientHandler handler = authorised_clients.get(user_id);
        participants.put(user_id, new CallParticipant(handler.get_ip(), user_id, handler.get_username()));// User_id as first participant
        ongoing_calls.put(channel_id, participants);
        CallParticipant[] currentParticipants = get_call_participants(channel_id);
        for (int id : authorised_clients.keySet()){
            if (id != user_id){ // not original caller
                authorised_clients.get(id).notify_on_incoming_call(currentParticipants); // notify connected users
            }
        }
        return true;
    }

    public boolean leave_call(int user_id, int channel_id){
        //Remove participant from call, notify all current participants, if none left remove call from calls

        if (!ongoing_calls.containsKey(channel_id)){ // if no call, return false
            return false;
        }
        ClientHandler handler = authorised_clients.get(user_id); // get leaver details for other participants
        CallParticipant leaving = new CallParticipant(handler.get_ip(), user_id, handler.get_username());
        ongoing_calls.get(channel_id).remove(user_id);// remove participant
        if (ongoing_calls.get(channel_id).isEmpty()){ // kill the call if its now empty
            ongoing_calls.remove(channel_id);
            return true;
        }

        for (int id : ongoing_calls.get(channel_id).keySet()){
            authorised_clients.get(id).notify_on_participant_leaving(leaving); // notify users of leaver
        }
        return true;
    }

    public CallParticipant[] get_call_participants(int channel_id){
        //Return a list of all call participants

        if (!ongoing_calls.containsKey(channel_id)){ // if its an empty call then return an empty array
            return new CallParticipant[0];
        }
        return ongoing_calls.get(channel_id).values().toArray(new CallParticipant[ongoing_calls.get(channel_id).values().size()]); // get all call participents and returns them as an array
    }

    public void stop_clients(){
        for (int id : unauthorised_clients.keySet()){
            unauthorised_clients.get(id).kill_self();
            unauthorised_clients.remove(id);
        }
        for (int id : authorised_clients.keySet()){
            authorised_clients.get(id).kill_self();
            authorised_clients.remove(id);
        }  
    }

    public void stop_server(){
        System.out.println("Stopping server");
        stop_clients();
        try {
            if (serverSocket != null){
                //In case the server was closed before the socket was created
                serverSocket.close();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        running = false;
    }

    public void authorise_client(int user_id, int unauthorised_id){
        //Add the client to the authorised_clients and make its id the user_id
        authorised_clients.put(user_id, unauthorised_clients.get(unauthorised_id));
        unauthorised_clients.remove(unauthorised_id);
    }

    public void kill_client(int id, boolean authorised){
        //takes id of client, user_id if true, unauthorised_id otherwise
        if (authorised){
            authorised_clients.remove(id);
        } else {
            unauthorised_clients.remove(id);
        }
    }
}
