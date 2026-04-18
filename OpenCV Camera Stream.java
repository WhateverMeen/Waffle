import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;

public class OpenCVCamera {
    public static void main(String[] args) {

        // Load OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        VideoCapture camera = new VideoCapture(0);

        if (!camera.isOpened()) {
            System.out.println("Error: Camera not detected");
            return;
        }

        Mat frame = new Mat();

        while (true) {
            if (camera.read(frame)) {
                HighGui.imshow("OpenCV Camera", frame);
            }

            // Press ESC to exit
            if (HighGui.waitKey(1) == 27) {
                break;
            }
        }

        camera.release();
        HighGui.destroyAllWindows();
    }
}
