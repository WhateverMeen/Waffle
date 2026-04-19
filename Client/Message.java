import java.time.LocalDateTime;

public class Message{
    private String message;
    private String username;
    private LocalDateTime datetime; 

    public Message(String message, String username, LocalDateTime datetime){
        this.message = message;
        this.username = username;
        this.datetime = datetime;
    }

    public LocalDateTime get_datetime(){
        return datetime;
    }

    public String get_message(){
        return message;
    }

    public String get_username(){
        return username;
    }
}
