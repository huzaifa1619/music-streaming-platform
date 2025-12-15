import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SignupUI extends JFrame {
    private final Color BG = new Color(0x0E0E11);
    private final Color SURFACE = new Color(0x1C1C22);
    private final Color ACCENT = new Color(0x635BFF);
    private final Color TEXT = new Color(0xF2F2F4);
    private final Color SUBTEXT = new Color(0xA2A2A8);

    public SignupUI() {
        setTitle("Sign up - SoundRaft");
        setSize(480, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(null);

        JLabel t = new JLabel("Create account");
        t.setFont(new Font("Segoe UI", Font.BOLD, 28));
        t.setForeground(TEXT);
        t.setBounds(24, 20, 360, 40);
        add(t);

        JLabel n = new JLabel("Full name");
        n.setForeground(SUBTEXT); n.setBounds(24, 80, 100, 20); add(n);
        JTextField fullname = new JTextField(); fullname.setBounds(24,100,420,36); fullname.setBackground(SURFACE); fullname.setForeground(TEXT); add(fullname);

        JLabel u = new JLabel("Username");
        u.setForeground(SUBTEXT); u.setBounds(24, 150, 100, 20); add(u);
        JTextField username = new JTextField(); username.setBounds(24,170,420,36); username.setBackground(SURFACE); username.setForeground(TEXT); add(username);

        JLabel p = new JLabel("Password");
        p.setForeground(SUBTEXT); p.setBounds(24, 220, 100, 20); add(p);
        JPasswordField password = new JPasswordField(); password.setBounds(24,240,420,36); password.setBackground(SURFACE); password.setForeground(TEXT); add(password);

        JButton signup = new JButton("Create");
        signup.setBounds(24, 300, 420, 40);
        signup.setBackground(ACCENT);
        signup.setForeground(Color.BLACK);
        add(signup);

        signup.addActionListener(e -> {
            String name = fullname.getText().trim();
            String user = username.getText().trim();
            String pass = new String(password.getPassword()).trim();
            if (name.isEmpty() || user.isEmpty() || pass.isEmpty()) { JOptionPane.showMessageDialog(this,"All fields required"); return; }
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement check = conn.prepareStatement("SELECT userID FROM Users WHERE username=?")) {
                check.setString(1, user);
                ResultSet rs = check.executeQuery();
                if (rs.next()) { JOptionPane.showMessageDialog(this, "Username taken"); return; }
                try (PreparedStatement ins = conn.prepareStatement("INSERT INTO Users(username,password,fullName) VALUES(?,?,?)")) {
                    ins.setString(1, user); ins.setString(2, pass); ins.setString(3, name);
                    ins.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Account created. You can login now.");
                dispose();
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "DB error"); }
        });

        setVisible(true);
    }
}
