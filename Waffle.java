import java.awt.*;

import javax.swing.*;


public class Waffle {

    private static Client client;


    static final Color BarCol = new Color(85,85,85);
    static final Color ButCol = new Color(200,200,200);
    static final Color ConCol = new Color(100,100,100);
    static final Color BotCol = new Color(150,150,150);
    

    public static void main(String[] args) {
        client = new Client();
        SwingUtilities.invokeLater(() -> {
            new Waffle().GUI();
        });
    }

    void GUI(){
        
        //making the panel to contain everything
        JPanel outerPanel = new JPanel(new BorderLayout());

        //making the leftmost menu bar, with box layout (top to bottom)
        JPanel narrowMenuBar = new JPanel();
        narrowMenuBar.setLayout(new BoxLayout(narrowMenuBar, BoxLayout.PAGE_AXIS));
        narrowMenuBar.setPreferredSize(new Dimension(50,0));
        narrowMenuBar.setBackground(BarCol);

        //making and aligning the buttons that will populate the leftmost menu
        JButton newChat = new JButton ("💬");
        JButton newGroup = new JButton ("👥");
        JButton signOut = new JButton("↩");

        

        newChat.setMaximumSize(new Dimension(Integer.MAX_VALUE,45));
        newChat.setAlignmentX(Component.CENTER_ALIGNMENT);
        newChat.setBackground(BarCol);
        newChat.setForeground(ButCol);
        newChat.setFocusPainted(false);
        newChat.setBorderPainted(false);
        newChat.setOpaque(true);
        newChat.setToolTipText("New Chat");

        newGroup.setMaximumSize(new Dimension(Integer.MAX_VALUE,45));
        newGroup.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGroup.setBackground(BarCol);
        newGroup.setForeground(ButCol);
        newGroup.setFocusPainted(false);
        newGroup.setBorderPainted(false);
        newGroup.setOpaque(true);
        newGroup.setToolTipText("New Group");


        signOut.setMaximumSize(new Dimension(Integer.MAX_VALUE,45));
        signOut.setAlignmentX(Component.CENTER_ALIGNMENT);
        signOut.setBackground(BarCol);
        signOut.setForeground(ButCol);
        signOut.setFocusPainted(false);
        signOut.setBorderPainted(false);
        signOut.setOpaque(true);
        signOut.setToolTipText("Sign Out");

        //adding the buttons to the leftmost menu
        narrowMenuBar.add(newChat);
        narrowMenuBar.add(Box.createVerticalStrut(5));
        narrowMenuBar.add(newGroup);
        
        //Glue acts as a flexible filler to push the buttons to the
        //top and bottom either side of it
        narrowMenuBar.add(Box.createVerticalGlue());
        //adding the rest of the left menu buttons separated by struts
        narrowMenuBar.add(signOut);
        narrowMenuBar.add(Box.createVerticalStrut(5));
        

        //making the second from left "contacts" panel with box layout
        JPanel contactsPanel = new JPanel();
        contactsPanel.setLayout(new BoxLayout(contactsPanel, BoxLayout.PAGE_AXIS));
        contactsPanel.setBackground(BarCol);
        contactsPanel.setBorder(BorderFactory.createLineBorder(new Color(175,175, 175)));

        //list of example contacts (will be replaced with server query)
        String[] contacts = {"Isaac","Joe","Matvii", "Michal", "Olivier", "Will"};

        //loop that takes the list of contacts and makes buttons to populate "contacts" menu
        for (int i = 0; i < contacts.length; i++) {
            String name = contacts[i];
            JButton contact= new JButton(name);
            contact.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
            contact.setAlignmentX(Component.LEFT_ALIGNMENT);
            contact.setBackground(ConCol);
            contact.setForeground(ButCol);
            contact.setFocusPainted(false);
            contact.setBorderPainted(false);
            contact.setOpaque(true);
            contactsPanel.add(contact);
            contactsPanel.add(Box.createVerticalStrut(5));
        }

        //makes a movable divider to separate the left menu and contact menu
        JSplitPane westSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, narrowMenuBar, contactsPanel);
        westSplit.setDividerLocation(50);
        westSplit.setPreferredSize(new Dimension(220,0));
        westSplit.setBorder(null);
        westSplit.setDividerSize(3);
        outerPanel.add(westSplit, BorderLayout.WEST);
        
        //makes a new innerpanel, to contain the top bar,
        //the bottom message writing bar, and the centre "sent
        //messages" message history section
        JPanel innerPanel = new JPanel(new BorderLayout());


        JPanel bottomBar = new JPanel(new BorderLayout(5,5));
        bottomBar.setPreferredSize(new Dimension(0,50));
        bottomBar.setBackground(ConCol);

        JTextField writeText = new JTextField();
        writeText.setBackground(ConCol);
        writeText.setForeground(ButCol);
        writeText.setCaretColor(Color.WHITE);


        JButton sendButton = new JButton ("Send ➤");
        sendButton.setBackground(ConCol);
        sendButton.setForeground(ButCol);
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setOpaque(true);

        bottomBar.add(writeText, BorderLayout.CENTER);
        bottomBar.add(sendButton, BorderLayout.EAST);



        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(0,35));
        topBar.setBackground(ConCol);

        JPanel PinMuteButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT,4,4));
        PinMuteButtons.setBackground(ConCol);

        JButton pinButton = new JButton("📌");
        JButton muteButton = new JButton("🔔");//🔕


        
        pinButton.setBackground(ConCol);
        pinButton.setForeground(ButCol);
        pinButton.setFocusPainted(false);
        pinButton.setBorderPainted(false);
        pinButton.setOpaque(true);

        muteButton.setBackground(ConCol);
        muteButton.setForeground(ButCol);
        muteButton.setFocusPainted(false);
        muteButton.setBorderPainted(false);
        muteButton.setOpaque(true);

        pinButton.setToolTipText("Pin");
        muteButton.setToolTipText("Mute/Unmute");

        PinMuteButtons.add(pinButton);
        PinMuteButtons.add(muteButton);
        topBar.add(PinMuteButtons, BorderLayout.EAST);

        innerPanel.add(topBar, BorderLayout.NORTH);


        innerPanel.add(bottomBar, BorderLayout.SOUTH);
        
        //places the inner bar inside of the outer bar (in the centre)
        outerPanel.add(innerPanel, BorderLayout.CENTER);


        JPanel chats = new JPanel();
        chats.setLayout(new BoxLayout(chats, BoxLayout.PAGE_AXIS));
        chats.setBackground(BarCol);
        chats.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(chats);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getViewport().setBackground(BarCol);
        scroll.setBorder(null);
        scroll.setViewportBorder(null);

        innerPanel.add(scroll, BorderLayout.CENTER);


        sendButton.addActionListener(client.send_message(writeText.getText()));



        //makes a main frame to put everything in
        JFrame frame = new JFrame("Waffle");
        frame.setIconImage(new ImageIcon("waffleLogo.png").getImage());
        frame.getRootPane().setDefaultButton(sendButton);

        //frame setup, size, put "outerPanel" inside the frame,
        //stop running programme when cross is pressed, make visible
        //let os decide the location of the application
        frame.setSize(400, 300);
        frame.setContentPane(outerPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setLocationByPlatform(true);
    }
    static JPanel sendMessage(String text, boolean sentBy) {
        Color colour;
        Color textColour;
        if (sentBy) {
            colour = new Color(53,76,124);
            textColour = new Color (234,235,254);}
        else{
            colour = new Color	(176,184,206);
            textColour = new Color (2,41,84);
        }
        
        JPanel row = new JPanel(new BorderLayout(10,0));
        
        JTextArea bubble = new JTextArea(text);

        bubble.setLineWrap(true);
        bubble.setWrapStyleWord(true);
        bubble.setEditable(false);
        bubble.setFocusable(false);
        bubble.setOpaque(true);
        bubble.setBackground(colour);
        bubble.setForeground(textColour);
        bubble.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bubble.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
        bubble.setColumns(20);
        row.add(bubble);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height + 200));
        return row;
    }
}