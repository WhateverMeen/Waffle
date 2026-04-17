import java.time.LocalDateTime;

public class Message{
    public String message;
    public String username;
    public LocalDateTime date;

    public Message(String message, String username, LocalDateTime date){
        this.message = message;
        this.username = username;
        this.date = date;
    }

    public LocalDateTime get_date(){
        return date;
    }

    public String get_message(){
        return message;
    }

    public String get_username(){
        return username;
    }
}
