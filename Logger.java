import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.date.LocalDateTime;

public class Logger{
    private String[] log_buffer;
    private int top;
    private int max_buffer_size;

    public Logger(buff_size){
        max_buffer_size = buff_size;
        log_buffer = new String[max_buffer_size];
        top = 0;
    }

    public void log_message(String message){
        log_buffer[top] = message;
        top += 1;
        if (top == max_buffer_size){
            write_buffer();
            top = 0;
        }
    }
    private void write_buffer(){
        Boolean fileExists = false;
        File f = new File("Log.txt");
        if (!f.exitst()){
            try{
                f.createNewFile();
                fileExists = true;
            } catch(IOException e){
                System.out.println("Couldn`t create file");
            }
        }
        if (fileExists){
            FileWriter fw = new FileWriter(f);
            for (int i = 0; i < log_buffer.length; i++){
                fw.append(log_buffer[i]);
            }
        }

    }
}
