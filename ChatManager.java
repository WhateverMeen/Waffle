import java.time.LocalDateTime;

public class ChatManager{
    private int channel_id;
    private int[] users;

    public void send_message(Message message);
    public Message[] retrieve_messages();
    public void edit_message(LocalDateTime date, int user_id, Message message);
    public void delete_message(LocalDateTime date, int user_id);
    //public void send_image(Image image, int user_id);
    //public void delete_image(Date date, int user_id);
        
}
