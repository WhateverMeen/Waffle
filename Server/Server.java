import java.net.*;
import java.io.*;

import java.util.concurrent.ConcurrentHashMap;

public class Server extends Thread{
    private final int PORT_NUMBER = 4567;

    //Concurrency safe hashmaps for storing the unauthorised clients and authorised clients currently connected
    //This allows for communication between threads when initiating a p2p call
    ConcurrentHashMap<Integer, ClientHandler> unauthorised_clients; //The key is unqiue id assigned to each client at creation
    ConcurrentHashMap<Integer, ClientHandler> authorised_clients; //The key is user_id of the client

    private int unauthorised_count;
    private boolean running;

    public Server(){
        unauthorised_clients = new ConcurrentHashMap<Integer, ClientHandler>();
        authorised_clients = new ConcurrentHashMap<Integer, ClientHandler>();
        unauthorised_count = 0;
    }
    
    public void run(){
        running = true;
        try (ServerSocket serverSocket = new ServerSocket(PORT_NUMBER)){
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
            e.printStackTrace();
            //Kill all threads
            stop_clients();  
            SQLManager.close_con();
        }
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
        stop_clients();
        
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
