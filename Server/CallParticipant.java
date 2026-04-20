public class CallParticipant{
    private String ip;
    private int user_id;
    private String username;
    
    public CallParticipant(String ip, int user_id, String username){
        this.ip = ip;
        this.user_id = user_id;
        this.username = username;
    }

    public String get_ip(){
        return ip;
    }
    public int get_user_id(){
        return user_id;
    }

    public String get_username(){
        return username;
    }
}
