import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginUI extends JFrame {

    private final Color BG = new Color(0x0E0E11);
    private final Color TEXT = new Color(0xFFFFFF);
    private final Color FIELD_BG = new Color(0x1A1A1D);
    private final Color ACCENT = new Color(0x1BC7B1);

    public LoginUI() {

        setTitle("Login - SoundRaft");
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(BG);

        JLabel appTitle = new JLabel("SoundRaft");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 38));
        appTitle.setForeground(new Color(0x635BFF));
        appTitle.setBounds(40, 20, 400, 50);
        add(appTitle);

        JPanel box = new JPanel(null);
        box.setBackground(BG);
        box.setBounds(350, 120, 500, 420);
        add(box);

        JLabel title = new JLabel("Welcome back");
        title.setFont(new Font("Segoe UI", Font.BOLD, 40));
        title.setForeground(TEXT);
        title.setBounds(110, 0, 400, 60);
        box.add(title);

        JLabel uLabel = new JLabel("Username");
        uLabel.setBounds(50, 90, 250, 20);
        uLabel.setForeground(new Color(180, 180, 180));
        uLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        box.add(uLabel);

        JTextField username = new JTextField();
        username.setBounds(50, 115, 400, 45);
        username.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        username.setBackground(FIELD_BG);
        username.setForeground(TEXT);
        username.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        box.add(username);

        JLabel pLabel = new JLabel("Password");
        pLabel.setBounds(50, 175, 250, 20);
        pLabel.setForeground(new Color(180, 180, 180));
        pLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        box.add(pLabel);

        JPasswordField password = new JPasswordField();
        password.setBounds(50, 200, 400, 45);
        password.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        password.setBackground(FIELD_BG);
        password.setForeground(TEXT);
        password.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        box.add(password);

        JButton loginBtn = new JButton("Continue");
        loginBtn.setBounds(50, 270, 400, 55);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 22));
        loginBtn.setBackground(ACCENT);
        loginBtn.setForeground(Color.BLACK);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        box.add(loginBtn);

        loginBtn.addActionListener(e -> {
            Connection conn = null;
            PreparedStatement pst = null;
            ResultSet rs = null;
            
            try {
                String user = username.getText().trim();
                String pass = new String(password.getPassword()).trim();

                // Validate input
                if (user.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Please enter both username and password", 
                        "Input Required", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                conn = DBConnection.getConnection();
                pst = conn.prepareStatement(
                    "SELECT userID, fullName FROM Users WHERE username=? AND password=?");

                pst.setString(1, user);
                pst.setString(2, pass);

                rs = pst.executeQuery();

                if (rs.next()) {
                    int uid = rs.getInt("userID");
                    String name = rs.getString("fullName");

                    // Close login page
                    dispose();
                    
                    // Open Dashboard with logged-in user's name
                    SwingUtilities.invokeLater(() -> {
                        new Dashboard(name).setVisible(true);
                    });

                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Invalid username or password", 
                        "Login Failed", 
                        JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Database Error: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                // Properly close all database resources
                try {
                    if (rs != null) rs.close();
                    if (pst != null) pst.close();
                    if (conn != null) conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        setVisible(true);
    }

    
}