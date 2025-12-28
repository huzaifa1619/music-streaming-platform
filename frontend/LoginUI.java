import javax.swing.*;
import java.awt.*;

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

        // ================= NEW ARCHITECTURE LOGIN =================
        loginBtn.addActionListener(e -> {

            String user = username.getText().trim();

            if (user.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Username is required",
                    "Input Required",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try {
                // Start backend if needed
                if (!BackendBridge.isRunning()) {
                    BackendBridge.startBackend();
                }

                // Tell backend which user is active
                BackendBridge.setUser(user);

                // Open dashboard
                dispose();
                SwingUtilities.invokeLater(() ->
                    new Dashboard(user).setVisible(true)
                );

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Backend error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }
}
