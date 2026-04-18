//import com.github.sarxos.webcam.Webcam;
import javax.swing.*;
import java.awt.image.BufferedImage;

public class SimpleCameraApp {
    public static void main(String[] args) {
        Webcam webcam = Webcam.getDefault();
        webcam.open();

        JFrame window = new JFrame("Camera");
        JLabel label = new JLabel();
        window.add(label);
        window.setSize(640, 480);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        while (true) {
            BufferedImage image = webcam.getImage();
            label.setIcon(new ImageIcon(image));
        }
    }
}

