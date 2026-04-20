import java.util.Scanner;

public class CLIClient {
    
    public static void main(String[] args){
        try{
            Client c = new Client();
            Scanner s = new Scanner(System.in);
            System.out.print("Enter your username:");
            String username = s.nextLine();
            System.out.print("Enter password:");
            String password = s.nextLine();

            System.out.println(c.register_account(username, password));
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
