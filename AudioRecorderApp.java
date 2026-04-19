import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;

public class AudioRecorderApp {

    private TargetDataLine line;
    private File audioFile = new File("recording.wav");

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AudioRecorderApp().createUI());
    }

    private void createUI() {
        JFrame frame = new JFrame("Audio Recorder");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);

        JButton recordBtn = new JButton("Record");
        JButton stopBtn = new JButton("Stop");
        JButton playBtn = new JButton("Play");

        recordBtn.addActionListener(e -> startRecording());
        stopBtn.addActionListener(e -> stopRecording());
        playBtn.addActionListener(e -> playAudio());

        JPanel panel = new JPanel();
        panel.add(recordBtn);
        panel.add(stopBtn);
        panel.add(playBtn);

        frame.add(panel);
        frame.setVisible(true);
    }

    private AudioFormat getFormat() {
        return new AudioFormat(
                16000, // sample rate
                16,    // sample size
                1,     // channels
                true,  // signed
                true   // big endian
        );
    }

    private void startRecording() {
        try {
            AudioFormat format = getFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                return;
            }

            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            System.out.println("Recording...");

            Thread thread = new Thread(() -> {
                try (AudioInputStream ais = new AudioInputStream(line)) {
                    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, audioFile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            thread.start();

        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }

    private void stopRecording() {
        if (line != null) {
            line.stop();
            line.close();
            System.out.println("Recording stopped");
        }
    }

    private void playAudio() {
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile)) {

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();

            System.out.println("Playing audio...");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
