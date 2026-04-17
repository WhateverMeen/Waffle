import java.net.*;
import java.io.*;

import java.util.concurrent.ConcurrentHashMap;

public class Server{
    private final int PORT_NUMBER = 4567;
    private Boolean running;

    //Concurrency safe hashmaps for storing the unauthorised clients and authorised clients currently connected
    //This allows for communication between threads when initiating a p2p call
    ConcurrentHashMap<Integer, Thread> unauthorised_clients; //The key is unqiue id assigned to each client at creation
    ConcurrentHashMap<Integer, Thread> authorised_clients; //The key is user_id of the client

    private int unauthorised_count;

    public Server(){
        unauthorised_clients = new ConcurrentHashMap<Integer, Thread>();
        authorised_clients = new ConcurrentHashMap<Integer, Thread>();
        unauthorised_count = 0;
    }

    public static void main(String[] args){
        Server s = new Server();
        s.run();
    }
    
    public void run(){
        try {
            ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);
            while (true){
                //Wait for the client to connect
                Socket clientSocket = serverSocket.accept();
                //Add client to unauthorised clients and assign its id and then start the thread
                unauthorised_clients.put(unauthorised_count, new Thread(new ClientHandler(clientSocket, this, unauthorised_count)));
                unauthorised_clients.get(unauthorised_count - 1).start();
                unauthorised_count++;
            }
        } catch (Exception e){
            e.printStackTrace();
            SQLManager.close_con();
        }
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
