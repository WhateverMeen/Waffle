public class PopulateDb{
    public static void main(String[] args){
        SQLManager.execute_query("INSERT INTO Users VALUES (0, 'Hello', 'hi')", true);
        SQLManager.execute_query("INSERT INTO Channels VALUES (0, 'Whatever')", true);
        SQLManager.execute_query("INSERT INTO Users_in_channel VALUES (0, 0)", true);
    
    }
}
