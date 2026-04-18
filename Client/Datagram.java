public class Datagram{
    int frame; //Holds the current frame number of video since the start of the call
    short packet_num; //Current packets number for this frame
    short packet_total; //Total amount of packets to send
    char flags; //lowest bit is the camera enabled flag, the next is the audio enabled flag
    short v_res_x; //X-axis resolution of video 
    short v_res_y; //y-axis resolution of video
    short sample_rate; //Audio sample rate
    short bit_depth; //Audio bit depth
    String username; //Max length of username is 25; 50 bytes total
    byte[] video_buffer; //Video buffer 500 bytes max
    byte[] audio_buffer; //Audio buffer 500 bytes max
    
    public Datagram(int frame, short packet_num, short packet_total, short res_x, short res_y, short sample_rate, short sample_depth, String username, byte[] audio_buffer, byte[] video_buffer){
        this.frame = frame;
        this.packet_num = packet_num;
        this.packet_total = packet_total;
        this.v_res_x = res_x;
        this.v_res_y = res_y;
        this.sample_rate = sample_rate;
        this.bit_depth = bit_depth;
        this.username = username;
        this.video_buffer = video_buffer;
        this.audio_buffer = audio_buffer;
    }

    public byte[] pack_data(){
        //Total size of packet is going to be 1042
        byte[] packed = new byte[1068];
        //Extract individual bytes from everything and put them in an array
        packed[0] = (byte)(frame >> 24);
        packed[1] = (byte)(frame >> 16);
        packed[2] = (byte)(frame >> 8);
        packed[3] = (byte)(frame);
        packed[4] = (byte)(packet_num >> 8);
        packed[5] = (byte)(packet_num);
        packed[6] = (byte)(packet_total >> 8);
        packed[7] = (byte)(packet_total);
        packed[8] = (byte)(v_res_x >> 8);
        packed[9] = (byte)(v_res_y);
        packed[10] = (byte)(sample_rate >> 8);
        packed[11] = (byte)(sample_rate);
        packed[12] = (byte)(bit_depth >> 8);
        packed[13] = (byte)(bit_depth);
        int index = 14;
        //Pack the username
        for (int i = 0; i < 25; i++){
            char c = username.charAt(i);
            packed[index] = (byte)(c >> 8); //Java chars are 16-bits
            index++;
            packed[index] = (byte)(c);
            index++;
        }

        for (int i = 0; i < 500; i++){
            packed[index] = video_buffer[i];
            index++;
        }
        
        for (int i = 0; i < 500; i++){
            packed[index] = audio_buffer[i];
            index++;
        }
        return packed;
    }
}
