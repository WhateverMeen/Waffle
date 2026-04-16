public class ChannelContainer{
    private String channelName;
    private int channel_id;
    private ArrayList<String> users;
    private ArrayList<String> messages;
    
    //MSG FORMAT <DATE><USERNAME><MSG CONTENT>

    public ChannelContainer(String name, String[] usersList){
        channelName = name;
        users = new ArrayList<String>();
        messages = new ArrayList<String>();

        for (int i = 0; i < usersList.length; i++){
            users.add(usersList);
        }
    }
    
    

    public void addMessage(String msg){
        messages.add(msg);
    }

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
