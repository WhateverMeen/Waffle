import java.time.LocalDateTime;

public class Message{
    private String message;
    private String username;
    private LocalDateTime date; 

    public Message(String message, String username, LocalDateTime date){
        this.message = message;
        this.username = username;
        this.date = date;
    }

    public LocalTime get_time(){
        return time;
    }

    public LocalDate get_date(){
        return date;
    }

    public String get_message(){
        return message;
    }

    public String get_username(){
        return username;
    }
}
