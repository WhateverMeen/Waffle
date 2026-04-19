// Figure out how to send the data to server ;

// Maybe try Game-DEV module Audio [ L25 ] ++ Research only ; ; 


import javax.sound.sampled.*;
import javax.swing.*;

public class AudioRecorderApp {

    private TargetDataLine microphone;
    private volatile boolean isRunning = false;
    private volatile boolean isMuted = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AudioRecorderApp().createUI());
    }

    private void createUI() {
        JFrame frame = new JFrame("Live Audio");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 120);

        JButton startBtn = new JButton("Start");
        JButton stopBtn = new JButton("Stop");
        JToggleButton muteBtn = new JToggleButton("Mute");

        startBtn.addActionListener(e -> start());
        stopBtn.addActionListener(e -> stop());

        muteBtn.addActionListener(e -> {
            isMuted = muteBtn.isSelected();
            muteBtn.setText(isMuted ? "Unmute" : "Mute");
        });

        JPanel panel = new JPanel();
        panel.add(startBtn);
        panel.add(stopBtn);
        panel.add(muteBtn);

        frame.add(panel);
        frame.setVisible(true);
    }

    private AudioFormat getFormat() {
        return new AudioFormat(
            16000, // Sample rate
            16, // Sample Size (bits)
            1, // Channels
            true, Signed 
            true
        );
    }

    private void start() {
        isRunning = true;

        new Thread(() -> {
            try {
                AudioFormat format = getFormat();
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);
                microphone.start();

                byte[] buffer = new byte[1024];

                while (isRunning) {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);

                    if (!isMuted) {
                        processAudio(buffer, bytesRead); 
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void stop() {
        isRunning = false;

        if (microphone != null) {
            microphone.stop();
            microphone.close();
        }
    }


    private void processAudio(byte[] data, int length) {
        // send audio over network
    }
}
