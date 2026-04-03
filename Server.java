public class Server implements Runnable{
    private Boolean running;

    private void run_server();
    private void stop_server();
    private void run();
    private String[] get_friends(String username);
    private String[] get_channels(String username);
    private void start_call(int channel_id);
    private String[] get_messages(int channel_id, stack_top);
    private boolean put_message(int channel_id, String username, String message);
    private boolean send_friend_request(String username, String friend_username);
    private boolean accept_friend_request(String username, String friend_username);
    private void deny_friend_request(String username, String friend_username);
    private String[] get_friends_requests(String username);
    Private String[] get_friend_list(String username);
    private String[] get_channels(String username);
    private int create_channel(String channel_name, String username);
    private void delete_channel(int channel_id);
    private boolean authenticate_user(String username, String password);
        
}
