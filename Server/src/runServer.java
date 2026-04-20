import java.util.Scanner;

public class runServer{
    
    public static void main(String[] args){
        Server s = new Server();
        s.start();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Type q to close server");
        while (!scanner.nextLine().equals("q")){
        
        }
        s.stop_server();
    }

}
