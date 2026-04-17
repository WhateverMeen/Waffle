import java.util.ArrayList;

public class ChannelContainer{
    private String channelName;
    private int channel_id;
    private ArrayList<String> users;
    private ArrayList<Message> messages;

    public ChannelContainer(String name, String[] usersList){
        channelName = name;
        users = new ArrayList<String>();
        messages = new ArrayList<String>();

        for (int i = 0; i < usersList.length; i++){
            users.add(usersList[i]);
        }
    }
    
    public ChannelContainer(String name, String user){
        channelName = name;
        users = new ArrayList<String>();
        messages = new ArrayList<String>();
        users.add(user);
    }
    
    public void addMessage(Message msg){
        messages.add(msg);
    }

    public String[] get_users(){
        return users.toArray(new String[users.size()]);
    }
    public Messages[] get_messages(){
        return messages.toArray(new Messages[messages.size()]);
    }

    public void add_messages(Messages[] messagesIn){
        for (int i = 0; i < messagesIn.length; i++){
            this.messages.add(messagesIn[i]);
        }
    }

    public String get_name(){
        return channelName;
    }
}
