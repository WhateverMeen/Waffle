import java.util.ArrayList;
import java.util.HashMap;

public class ChannelContainer{
    private String channelName;
    private int channel_id;
    private ArrayList<String> users;
    private HashMap<Integer, Message> messages;

    public ChannelContainer(String name, String[] usersList){
        channelName = name;
        users = new ArrayList<String>();
        messages = new HashMap<Integer, Message>();

        for (int i = 0; i < usersList.length; i++){
            users.add(usersList[i]);
        }
    }
    
    public ChannelContainer(String name, String user){
        channelName = name;
        users = new ArrayList<String>();
        messages = new HashMap<Integer, Message>();
        users.add(user);
    }
    
    public void addMessage(int id, Message msg){
        if (!messages.containsKey(id)){
            messages.put(id, msg);
        }
    }

    public String[] get_users(){
        return users.toArray(new String[users.size()]);
    }

    public Message[] get_messages(){
        //Returns an array of messages sorted from oldest to newest
        ArrayList<Message> msg_list = new ArrayList<Message>(messages.values());
        Message[] ret = msg_list.toArray(new Message[msg_list.size()]);
        //Sort messages by date from eldest to knewest
        boolean swap_made = true;
        while (swap_made){
            swap_made = false;
            for (int i = 0; i < ret.length - 1; i++){
                if (ret[i].get_datetime().isAfter(ret[i + 1].get_datetime())){
                    //swap messages
                    Message temp = ret[i + 1];
                    ret[i + 1] = ret[i];
                    ret[i] = temp;
                    swap_made = true;
                }
            }
        }
        return ret;
    }

    public String get_name(){
        return channelName;
    }
}
