public class ChannelContainer{
    private String channelName;
    private ArrayList<String> users;
    private ArrayList<String> messages;

    public String[] get_users(){
        return users.toArray();
    }
    public String[] get_messages(){
        return messages.toArray();
    }
    public void add_messages(String[] messagesIn){
        for (int i = 0; i < messagesIn.length; i++){
            this.messages.add(messagesIn[i]);
        }
    }
    public String get_name(){
        return channelName;
    }
}
