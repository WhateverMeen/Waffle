public class ChannelContainer{
    private ArrayList<String> users;
    private ArrayList<String> messages;

    public String[] getUsers(){
        return users.toArray();
    }
    public String[] getMessages(){
        return messages.toArray();
    }
    public void addMessages(String[] messagesIn){
        for (int i = 0; i < messagesIn.length; i++){
            this.messages.add(messagesIn[i]);
        }
    }
}
