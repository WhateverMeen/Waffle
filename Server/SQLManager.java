import java.sql.*;

public class SQLManager{ 
    // Need to call close con when server stops running as it closes conections 
    private static final String URL = "jdbc:sqlite:./waffle.db"; //Connection to db, creates a file
    private static boolean first_run = true;
    private static Connection con = null;
    private static Statement state = null;

    public static ResultSet execute_query(String query, Boolean update){
        if (first_run){
            // first run hence establish connection to server and create statement
            try {
                con = DriverManager.getConnection(URL);
                state = con.createStatement();
                if (update == true) {
                    state.executeUpdate(query);
                    first_run = false;
                    return null; // updated database only so no return value
                } else {
                    ResultSet rs = state.executeQuery(query);
                    first_run = false;
                    return rs; // returns result set
                }
            } catch (Exception e) {
                e.printStackTrace(); // print any issues
                return null;
            }
        } else { // If this is not the first run
            try{
                if (update == true) {
                    state.executeUpdate(query);
                    return null; // updated so no return value
                } else {
                    ResultSet rs = state.executeQuery(query);
                    return rs;
                }
            } catch (Exception e) {
                    e.printStackTrace();
                    return null;
            }
        }
    }

    public static void close_con(){// Function to close connections
        try {
            con.close();
            state.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}