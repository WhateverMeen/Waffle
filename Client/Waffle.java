import java.awt.*;
import javax.swing.*;

public class Waffle {

    private static Client client;
    private int currentChannelId = 0; // track selected chat
    private boolean isMuted = false;

    static final Color BarCol = new Color(85, 85, 85);
    static final Color ButCol = new Color(200, 200, 200);
    static final Color ConCol = new Color(100, 100, 100);
    static final Color BotCol = new Color(150, 150, 150);

    public Waffle(Client authenticatedClient) {
        client = authenticatedClient;
    }

    public Waffle() {}

    public static void main(String[] args) {
        try {
            client = new Client();
            
            SwingUtilities.invokeLater(() -> {
                if (authenticate()) {
                    new Waffle().GUI();
                } else {
                    System.exit(0);
                }
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Could not connect to server:\n" + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static boolean authenticate() {
        while (true) {
            String[] options = {"Login", "Register", "Exit"};
            int choice = JOptionPane.showOptionDialog(null, "Welcome to Waffle", "Authentication",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

            if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) return false;

            JTextField userField = new JTextField();
            JPasswordField passField = new JPasswordField();
            Object[] message = {"Username:", userField, "Password:", passField};

            int option = JOptionPane.showConfirmDialog(null, message, options[choice].toString(), JOptionPane.OK_CANCEL_OPTION);
            if (option != JOptionPane.OK_OPTION) continue;

            String username = userField.getText();
            String password = new String(passField.getPassword());

            try {
                if (choice == 0) {
                    if (client.login(username, password)) {
                        return true;
                    } else {
                        JOptionPane.showMessageDialog(null, "Login failed. Check credentials.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    if (client.register_account(username, password)) {
                        JOptionPane.showMessageDialog(null, "Registered successfully! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Registration failed. Username might be taken.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Communication error:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    void GUI() {
        // Main Frame
        JFrame frame = new JFrame("Waffle - " + client.get_username());
        frame.setIconImage(new ImageIcon("waffleLogo.png").getImage());
        JPanel outerPanel = new JPanel(new BorderLayout());

        // Left menu bar
        JPanel narrowMenuBar = new JPanel();
        narrowMenuBar.setLayout(new BoxLayout(narrowMenuBar, BoxLayout.PAGE_AXIS));
        narrowMenuBar.setPreferredSize(new Dimension(65, 0));
        narrowMenuBar.setBackground(BarCol);

        // Menu Buttons
        JButton newChat = createMenuButton("💬", "New Chat");
        JButton newGroup = createMenuButton("👥", "New Group");
        JButton joinChannelBtn = createMenuButton("➕", "Join Channel");
        JButton leaveChannelBtn = createMenuButton("➖", "Leave Channel");
        JButton signOut = createMenuButton("↩", "Sign Out");

        narrowMenuBar.add(newChat);
        narrowMenuBar.add(Box.createVerticalStrut(5));
        narrowMenuBar.add(newGroup);
        narrowMenuBar.add(Box.createVerticalStrut(5));
        narrowMenuBar.add(joinChannelBtn);
        narrowMenuBar.add(Box.createVerticalStrut(5));
        narrowMenuBar.add(leaveChannelBtn);
        narrowMenuBar.add(Box.createVerticalGlue());
        narrowMenuBar.add(signOut);
        narrowMenuBar.add(Box.createVerticalStrut(5));

        // Contacts Panel
        JPanel contactsPanel = new JPanel();
        contactsPanel.setLayout(new BoxLayout(contactsPanel, BoxLayout.PAGE_AXIS));
        contactsPanel.setBackground(BarCol);
        contactsPanel.setBorder(BorderFactory.createLineBorder(new Color(175, 175, 175)));

        // Central chat display
        JPanel chats = new JPanel();
        chats.setLayout(new BoxLayout(chats, BoxLayout.PAGE_AXIS));
        chats.setBackground(BarCol);
        chats.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(chats);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getViewport().setBackground(BarCol);
        scroll.setBorder(null);

        // Load channels initially
        refreshContacts(contactsPanel, chats);

        JSplitPane westSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, narrowMenuBar, contactsPanel);
        westSplit.setDividerLocation(65);
        westSplit.setPreferredSize(new Dimension(200, 0)); 
        westSplit.setBorder(null);
        westSplit.setDividerSize(3);
        outerPanel.add(westSplit, BorderLayout.WEST);

        // Inner panel 
        JPanel innerPanel = new JPanel(new BorderLayout());

        // Bottom Bar 
        JPanel bottomBar = new JPanel(new BorderLayout(5, 5));
        bottomBar.setPreferredSize(new Dimension(0, 55));
        bottomBar.setBackground(ConCol);

        JTextField writeText = new JTextField();
        writeText.setBackground(ConCol);
        writeText.setForeground(ButCol);
        writeText.setCaretColor(Color.WHITE);
        writeText.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JButton sendButton = new JButton("Send ➤");
        styleButton(sendButton, ConCol, ButCol);
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        frame.getRootPane().setDefaultButton(sendButton);

        bottomBar.add(writeText, BorderLayout.CENTER);
        bottomBar.add(sendButton, BorderLayout.EAST);

        // Top Bar 
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(0, 45));
        topBar.setBackground(ConCol);

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        actionButtons.setBackground(ConCol);

        JButton callBtn = createMenuButton("📞", "Start Call");
        JButton joinCallBtn = createMenuButton("📲", "Join Call");
        JButton endCallBtn = createMenuButton("📵", "End Call");
        JButton renameBtn = createMenuButton("✏️", "Rename Chat");
        JButton deleteBtn = createMenuButton("🗑️", "Delete/Leave Chat");
        JButton pinButton = createMenuButton("📌", "Pin Chat");
        JButton muteButton = createMenuButton("🔔", "Mute/Unmute Mic");

        actionButtons.add(callBtn);
        actionButtons.add(joinCallBtn);
        actionButtons.add(endCallBtn);
        actionButtons.add(renameBtn);
        actionButtons.add(deleteBtn);
        actionButtons.add(pinButton);
        actionButtons.add(muteButton);
        topBar.add(actionButtons, BorderLayout.EAST);

        innerPanel.add(topBar, BorderLayout.NORTH);
        innerPanel.add(bottomBar, BorderLayout.SOUTH);
        innerPanel.add(scroll, BorderLayout.CENTER);
        outerPanel.add(innerPanel, BorderLayout.CENTER);


        sendButton.addActionListener(e -> {
            try {
                String text = writeText.getText();
                if (!text.isEmpty() && currentChannelId != 0) {
                    if (client.send_message(text, currentChannelId)) {
                        chats.add(sendMessage(text, true));
                        chats.revalidate();
                        writeText.setText("");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Failed to send message.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        newChat.addActionListener(e -> createNewChannel(frame, "Enter username for new chat:", contactsPanel, chats));
        newGroup.addActionListener(e -> createNewChannel(frame, "Enter group name:", contactsPanel, chats));

        joinChannelBtn.addActionListener(e -> {
            String idStr = JOptionPane.showInputDialog(frame, "Enter Channel ID to join:");
            if (idStr != null && !idStr.isEmpty()) {
                try {
                    if (client.join_channel(Integer.parseInt(idStr))) {
                        refreshContacts(contactsPanel, chats);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Could not join channel.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        leaveChannelBtn.addActionListener(e -> handleLeaveChannel(frame, contactsPanel, chats));
        deleteBtn.addActionListener(e -> {
            if (currentChannelId == 0) {
                JOptionPane.showMessageDialog(frame, "Please select a chat first.", "No Chat Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete/leave this chat?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                handleLeaveChannel(frame, contactsPanel, chats);
            }
        });

        renameBtn.addActionListener(e -> {
            if (currentChannelId == 0) {
                JOptionPane.showMessageDialog(frame, "Please select a chat first.", "No Chat Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String newName = JOptionPane.showInputDialog(frame, "Enter new chat name:");
            if (newName != null && !newName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Rename feature coming soon.", "Work in Progress", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        signOut.addActionListener(e -> {
            client.quit();
            System.exit(0);
        });

        muteButton.addActionListener(e -> {
            isMuted = !isMuted;
            if (isMuted) {
                client.mute_microphone();
                muteButton.setText("🔕");
            } else {
                client.unmute_microphone();
                muteButton.setText("🔔");
            }
        });

        pinButton.addActionListener(e -> {
            if (currentChannelId != 0) {
                JOptionPane.showMessageDialog(frame, "Pinned Channel: " + currentChannelId);
            }
        });

        callBtn.addActionListener(e -> {
            if (currentChannelId != 0) {
                try {
                    client.start_call(currentChannelId);
                    JOptionPane.showMessageDialog(frame, "Call started on channel " + currentChannelId);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        joinCallBtn.addActionListener(e -> {
            if (currentChannelId != 0) {
                try {
                    client.join_call(currentChannelId);
                    JOptionPane.showMessageDialog(frame, "Joined call.");
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        endCallBtn.addActionListener(e -> {
            if (currentChannelId != 0) {
                try {
                    client.leave_call(currentChannelId);
                    JOptionPane.showMessageDialog(frame, "Left call.");
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        // Timer checking client states
        Timer stateTimer = new Timer(2000, e -> {
            if (client.get_call_incoming()) {
                int cId = client.get_call_channel();
                int answer = JOptionPane.showConfirmDialog(frame, 
                    "Incoming call on channel " + cId + ". Do you want to join?", 
                    "Incoming Call", JOptionPane.YES_NO_OPTION);
                
                if (answer == JOptionPane.YES_OPTION) {
                    try { client.join_call(cId); } catch (Exception ex) { ex.printStackTrace(); }
                }
                client.notify_call_ended(); 
            }

            if (client.get_in_call()) {
                String[] activeUsers = client.get_users_in_call();
                if (activeUsers.length > 0) {
                    frame.setTitle("Waffle - " + client.get_username() + " [In Call: " + activeUsers.length + " users]");
                } else {
                    frame.setTitle("Waffle - " + client.get_username() + " [In Call]");
                }
            } else {
                frame.setTitle("Waffle - " + client.get_username());
            }
        });
        stateTimer.start();

        frame.setSize(900, 600); 
        frame.setContentPane(outerPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    private void handleLeaveChannel(JFrame frame, JPanel contactsPanel, JPanel chats) {
        if (currentChannelId == 0) return;
        try {
            if (client.leave_channel(currentChannelId)) {
                currentChannelId = 0;
                chats.removeAll();
                chats.revalidate();
                chats.repaint();
                refreshContacts(contactsPanel, chats);
            } else {
                JOptionPane.showMessageDialog(frame, "Could not leave/delete channel.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private JButton createMenuButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        styleButton(btn, BarCol, ButCol);
        btn.setToolTipText(tooltip);
        return btn;
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 16));
        btn.setMargin(new Insets(2, 2, 2, 2)); 
    }

    private void createNewChannel(JFrame frame, String prompt, JPanel contactsPanel, JPanel chats) {
        String name = JOptionPane.showInputDialog(frame, prompt);
        if (name != null && !name.trim().isEmpty()) {
            try {
                int id = client.create_channel(name);
                if (id > 0) {
                    refreshContacts(contactsPanel, chats);
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to create channel.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void refreshContacts(JPanel contactsPanel, JPanel chats) {
        contactsPanel.removeAll();
        try {
            client.request_channels();
            Integer[] ids = client.get_channel_ids();
            
            for (Integer id : ids) {
                String name = client.get_channel_name(id);
                JButton contact = new JButton(name + " (#" + id + ")");
                contact.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                contact.setAlignmentX(Component.LEFT_ALIGNMENT);
                styleButton(contact, ConCol, ButCol);
                contact.setFont(new Font("SansSerif", Font.BOLD, 14));

                contact.addActionListener(e -> loadChatData(id, chats));

                contactsPanel.add(contact);
                contactsPanel.add(Box.createVerticalStrut(5));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        contactsPanel.revalidate();
        contactsPanel.repaint();
    }

    private void loadChatData(int channelId, JPanel chats) {
        currentChannelId = channelId;
        try {
            client.request_messages(channelId);
            chats.removeAll();

            Message[] messages = client.get_messages(channelId);
            if (messages != null) {
                for (Message m : messages) {
                    boolean sentByMe = m.get_username().equals(client.get_username());
                    chats.add(sendMessage(m.get_username() + ": " + m.get_message(), sentByMe));
                }
            }
            chats.revalidate();
            chats.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static JPanel sendMessage(String text, boolean sentBy) {
        Color colour;
        Color textColour;
        if (sentBy) {
            colour = new Color(53, 76, 124);
            textColour = new Color(234, 235, 254);
        } else {
            colour = new Color(176, 184, 206);
            textColour = new Color(2, 41, 84);
        }

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(BarCol);
        
        JTextArea bubble = new JTextArea(text);
        bubble.setLineWrap(true);
        bubble.setWrapStyleWord(true);
        bubble.setEditable(false);
        bubble.setFocusable(false);
        bubble.setOpaque(true);
        bubble.setBackground(colour);
        bubble.setForeground(textColour);
        bubble.setFont(new Font("SansSerif", Font.PLAIN, 14)); 
        bubble.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); 
        
        row.add(bubble, sentBy ? BorderLayout.EAST : BorderLayout.WEST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, bubble.getPreferredSize().height + 16));
        return row;
    }
}
