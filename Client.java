public class Client{
    private HashMap<int, ChannelContainer> channels;
    private boolean microphone_enabled;
    private boolean camera_enabled;
    private Socket socket;

    public Client();
    private void run();
    public boolean register_account(String username, String password, String email);
    public boolean login(String username, String password);
    public boolean reset_password(String old_password, String new_password);
    public boolean send_message(String message, int channel_id);
    //public send_photo();
    public boolean delete_message(Date date, int channel_id);
    public boolean edit_message(Date date, int channel_id);
    public boolean create_group(String group_name);
    public boolean leave_group(int channel_id);
    public boolean  join_group(int channel_id);
    public void delete_group(int channel_id);
    public boolean start_call(int channel_id);
    public void leave_call();
    public void join_call(channel_id);
    public boolean send_friend_request(String username);
    public boolean accept_friend_request(String username);
    public void deny_friend_request(String username);
    public void remove_friend(String username);
    public void block_user(String username);
    public boolean unmute_microphone();
    public void mute_microphone();
    public boolean enable_camera();
    public void disable_camera();
    
}
