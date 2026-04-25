import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.border.EmptyBorder;

public class LoginPage extends JFrame {

    private JTextField username_field;
    private JPasswordField password_field;
    private JButton login_button;
    private JButton register_button;
    private JLabel statusLabel;

    private Client client;

    private final Color BarCol = new Color(85, 85, 85);
    private final Color ButCol = new Color(200, 200, 200);
    private final Color ConCol = new Color(100, 100, 100);

    public LoginPage() {
        try {
            client = new Client();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to server.");
            System.exit(1);
        }
        initUI();
    }

    public LoginPage(Client client) {
        this.client = client;
        initUI();
    }

    private void initUI() {
        setTitle("Waffle - Login");
        setSize(350, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BarCol);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel username_label = styledLabel("Username:");
        username_field = styledTextField();

        JLabel password_label = styledLabel("Password:");
        password_field = (JPasswordField) styledTextField(true);

        login_button = styledButton("Login");
        register_button = styledButton("Register");

        statusLabel = styledLabel(" ");
        statusLabel.setForeground(new Color(255, 150, 150));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(BarCol);
        buttonPanel.add(login_button);
        buttonPanel.add(register_button);

        panel.add(username_label);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(username_field);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(password_label);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(password_field);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        panel.add(buttonPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(statusLabel);

        add(panel);

        login_button.addActionListener(this::login);

        register_button.addActionListener(e -> {
            new SignUpPage(client);
            dispose();
        });

        setVisible(true);
    }

    private void login(ActionEvent e) {
        try {
            String u = username_field.getText();
            String p = new String(password_field.getPassword());

            boolean ok = client.login(u, p);

            if (ok) {
                statusLabel.setText("Login successful");
                Waffle waffle = new Waffle(client);
                waffle.GUI();

                dispose();
            } else {
                statusLabel.setText("Invalid login");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Error");
        }
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
        SwingUtilities.invokeLater(LoginPage::new);
    }
}
