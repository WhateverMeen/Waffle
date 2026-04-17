import java.time.LocalDate;
import java.time.LocalTime;

public class Message{
    private String message;
    private String username;
    private LocalDate date;
    private LocalTime time; 

    public Message(String message, String username, LocalDate date, LocalTime time){
        this.message = message;
        this.username = username;
        this.date = date;
        this.time = time;
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
