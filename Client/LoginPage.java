import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginPage extends JFrame {

    private JTextField username_field;
    private JPasswordField password_field;
    private JButton login_button;
    private JButton register_button;
    private JLabel statusLabel;

    private Client client;

    public LoginPage() {

        setTitle("Login");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(6, 2));

        JLabel username_label = new JLabel("Username:");
        username_field = new JTextField();

        JLabel password_label = new JLabel("Password:");
        password_field = new JPasswordField();

        login_button = new JButton("Login");
        register_button = new JButton("Register");

        statusLabel = new JLabel(" ");

        add(username_label);
        add(username_field);
        add(password_label);
        add(password_field);
        add(login_button);
        add(register_button);
        add(statusLabel);

        try {
            client = new Client();
        } catch (Exception e) {
            statusLabel.setText("Failed to connect to server.");
            e.printStackTrace();
        }

        login_button.addActionListener((ActionEvent e) -> {
            try {
                handleLogin();
            }
            catch (Exception ex) {
                statusLabel.setText("Connection error.");
                ex.printStackTrace();
            }
        });

        register_button.addActionListener((ActionEvent e) -> {
            // Handle registration logic
            new SignUpPage();
            dispose();
        });

        setVisible(true);
    }

    private void handleLogin() throws Exception {
        String username = username_field.getText();
        String password = new String(password_field.getPassword());

        if (client == null) {
            statusLabel.setText("Not connected to server.");
            return;
        }
            
        boolean success = client.login(username, password);

        if (success) {
            statusLabel.setText("Login successful!");
            // Proceed to main application window
            Waffle waffleApp = new Waffle(client);
            waffleApp.GUI();
            this.dispose();
        } else {
            statusLabel.setText("Invalid username or password.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginPage::new);
    }
}
        add(login_button);
        add(register_button);
        add(statusLabel);

        try {
            client = new Client();
        } catch (Exception e) {
            statusLabel.setText("Failed to connect to server.");
            e.printStackTrace();
        }
        
        login_button.addActionListener((ActionEvent e) -> {
            try {
                handleLogin();
            }
            catch (Exception ex) {
                statusLabel.setText("Connection error.");
                ex.printStackTrace();
            }
        });

        register_button.addActionListener((ActionEvent e) -> {
            // Handle registration logic
            new SignUpPage();
            dispose();
        });

        setVisible(true);
    }

    private void handleLogin() throws Exception {
        String username = username_field.getText();
        String password = new String(password_field.getPassword());

        if (client == null) {
            statusLabel.setText("Not connected to server.");
            return;
        }
            
        boolean success = client.login(username, password);

        if (success) {
            statusLabel.setText("Login successful!");
            // Proceed to main application window
        } else {
            statusLabel.setText("Invalid username or password.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginPage::new);
    }
}
