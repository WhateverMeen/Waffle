import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.*;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.device.MediaFormat;
import org.jitsi.service.neomedia.device.MediaFormatFactory;

import java.security.PublicKey;
import java.security.PrivateKey;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

public class CallHandler extends Thread{
    private Client client;
    private Socket notify_socket; //Socket to listen in on for incoming call notifications
    private BufferedReader notify_in;
    private HashMap<String, MediaStream> participants; //Hashmap for storing call participants, key is the username, value is the MediaStream used to send and write to server

    private boolean running;

    private PrivateKey client_private_key;
    private PublicKey server_public_key;


    public CallHandler(Client c, Socket s, PublicKey server_key, PrivateKey client_key){
        client = c;
        notify_socket = s;
        try {
            notify_in = new BufferedReader(new InputStreamReader(notify_socket.getInputStream()));
        } catch(IOException e) {
            e.printStackTrace();
        }
        client_private_key = client_key;
        server_public_key = server_key;
        participants = new HashMap<String, MediaStream>();
        
        LibJitsi.start(); //Initialise jitsi   
    }

    public void end(){
        running = false;
    }

    public void connect_peer(String username, String peerIp){
        //Function used to generate an audiostream and connect it to a participant
        try {
            MediaService mediaservice = LibJitsi.getMediaService();
            MediaDevice mic = mediaservice.getDefaultDevice(MediaType.AUDIO, MediaUseCase.CALL); //Get users microphone
            //Setup an audio stream for sending and reading in audio samples
            MediaStream audioStream = mediaservice.createMediaStream(mic);
            audioStream.setDirection(MediaDirection.SENDRECV);
            //Set the audio format
            MediaFormatFactory formatFactory = mediaservice.getFormatFactory();
            MediaFormat opus = formatFactory.createMediaFormat("opus", 48000, 2); //Stereo at 48 kiloHertz
            audioStream.setFormat(opus);
            //Connect to peer
            StreamConnector connector = new DefaultStreamConnector(new DatagramSocket(4200), new DatagramSocket(4201));
            audioStream.setConnector(connector);
            //Tell audioStream where to send data
            MediaStreamTarget target = new MediaStreamTarget(new InetSocketAddress(peerIp, 4200), new InetSocketAddress(peerIp, 4201));
            audioStream.setTarget(target);
            audioStream.start(); //Start the streaming
            participants.put(username, audioStream);
            
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void disconnect_peer(String username){
        participants.get(username).close(); //Close the audio stream
        participants.remove(username);
    }

    public void connect_on_join_call(String[] usernames, String[] ips){
        //Connect to all participants of a call, indexes in username map to ips
        for (int i = 0; i < usernames.length; i++){
            connect_peer(usernames[i], ips[i]);
        }
    }

    public void run(){
        try {
            running = true;
            while (running){
                String raw_in = EncryptionManager.decrypt_message(notify_in.readLine(), client_private_key);
                String[] in = raw_in.split(" ");

                if (in[0].equals("INCOMING")){
                    client.notify_call_incoming(Integer.parseInt(in[1]));
                } else if (in[0].equals("JOINED")){
                    connect_peer(in[1], in[2]);               
                } else if (in[0].equals("LEFT")){
                    disconnect_peer(in[1]);
                } else if (in[0].equals("ENDED")) {
                    client.notify_call_ended();
                }
            }
            LibJitsi.end();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
