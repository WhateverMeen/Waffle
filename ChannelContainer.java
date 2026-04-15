public class ChannelContainer{
    private String channelName;
    private ArrayList<String> users;
    private ArrayList<String> messages;

<<<<<<< HEAD
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
=======
    public String[] getUsers();
    public String[] getMessages();
    public String[] getName();
    public void addMessages(String[] messages);
>>>>>>> 386329d (Finished the EncryptionManager class and expanded on the Client)
}
