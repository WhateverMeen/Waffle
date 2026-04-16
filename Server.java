import java.net.*;
import java.io.*;

public class Server implements Runnable{
    private final int PORT_NUMBER = 4567;
    private Boolean running;

    private static void main(String[] args){
        ServerSocket serverSocket = new ServerSocket
        while (true){
            Socket clientSocket = s.accept();
            new Thread(new ClientHandler(clientSocket)).start();
        }

    }

    private void stop_server();
       
}
