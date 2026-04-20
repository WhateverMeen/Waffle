import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.border.EmptyBorder;

public class SignUpPage extends JFrame {

    private JTextField username_field;
    private JPasswordField password_field;
    private JPasswordField confirm_password_field;
    private JButton sign_up_button;
    private JLabel status_label;
    private JButton back_button;

    private final Color BarCol = new Color(85, 85, 85);
    private final Color ButCol = new Color(200, 200, 200);
    private final Color ConCol = new Color(100, 100, 100);
 
    public SignUpPage() {
        setTitle("Waffle - Sign Up");
        setSize(350, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BarCol);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Initialise components
        JLabel username_label = styledLabel("Username:");
        username_field = styledTextField();

        JLabel password_label = styledLabel("Password:");
        password_field = (JPasswordField) styledTextField(true);

        JLabel confirm_password_label = styledLabel("Confirm Password:");
        confirm_password_field = (JPasswordField) styledTextField(true);

        sign_up_button = styledButton("Sign Up");
        back_button = styledButton("Back to Login");

        status_label = styledLabel(" ");
        status_label.setForeground(new Color(255, 150, 150));

        // Add components to the frame
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(BarCol);
        buttonPanel.add(sign_up_button);
        buttonPanel.add(back_button);

        panel.add(username_label);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(username_field);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(password_label);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(password_field);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        
        panel.add(confirm_password_label);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(confirm_password_field);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        panel.add(buttonPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(status_label);

        add(panel);

        // Add action listener to sign-up button
        sign_up_button.addActionListener(this::handleSignUp);

        // Add action listener to back button
        back_button.addActionListener((ActionEvent e) -> {
            new LoginPage();
            dispose();
        });

        setVisible(true);
    }

    public void handleSignUp(ActionEvent e) {
        String username = username_field.getText();
        String password = new String(password_field.getPassword());
        String confirmPassword = new String(confirm_password_field.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            status_label.setText("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            status_label.setText("Passwords do not match.");
            return;
        }

        // Add code to save details to database
        status_label.setText("Sign-up successful!");
    }

     private JLabel styledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(ButCol);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JTextField styledTextField() {
        return styledTextField(false);
    }

    private JTextField styledTextField(boolean isPassword) {
        JTextField field = isPassword ? new JPasswordField() : new JTextField();
        field.setBackground(ConCol);
        field.setForeground(ButCol);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return field;
    }

    private JButton styledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(ButCol);
        button.setForeground(BarCol);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        return button;
    }   

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SignUpPage::new);
    }
}
