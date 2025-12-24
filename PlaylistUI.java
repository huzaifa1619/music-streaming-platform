import javax.swing.*;
import java.awt.*;
import java.util.*;

public class PlaylistUI extends JFrame {

    private final Color BG = new Color(0x0E0E11);
    private final Color SURFACE = new Color(0x1C1C22);
    private final Color TEXT = new Color(0xF2F2F4);
    private final Color ACCENT = new Color(0x635BFF);

    private JPanel listPanel;

    // simple in-memory tracking (backend persists internally)
    private Map<Integer, String> playlists = new LinkedHashMap<>();
    private int nextPlaylistId = 1;

    public PlaylistUI() {
        setTitle("Playlists");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(BG);

        // Back button
        JButton back = new JButton("←");
        back.setBounds(20, 20, 50, 40);
        styleButton(back);
        back.addActionListener(e -> dispose());
        add(back);

        JLabel title = new JLabel("Your Playlists");
        title.setBounds(90, 20, 400, 40);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT);
        add(title);

        // Create playlist button
        JButton createBtn = new JButton("+ New Playlist");
        createBtn.setBounds(650, 25, 200, 36);
        styleButton(createBtn);
        createBtn.addActionListener(e -> createPlaylist());
        add(createBtn);

        listPanel = new JPanel(null);
        listPanel.setBackground(BG);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBounds(80, 90, 740, 440);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        add(scroll);

        refreshUI();
        setVisible(true);
    }

    // ================= CORE =================

    private void createPlaylist() {
        String name = JOptionPane.showInputDialog(
                this,
                "Playlist name:",
                "Create Playlist",
                JOptionPane.PLAIN_MESSAGE
        );

        if (name == null || name.trim().isEmpty()) return;

        int pid = nextPlaylistId++;

        boolean ok = BackendBridge.createPlaylist(pid, name);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Failed to create playlist");
            return;
        }

        playlists.put(pid, name);
        refreshUI();
    }

    private void refreshUI() {
        listPanel.removeAll();

        int y = 0;
        for (Map.Entry<Integer, String> pl : playlists.entrySet()) {
            listPanel.add(createRow(pl.getKey(), pl.getValue(), y));
            y += 70;
        }

        listPanel.setPreferredSize(new Dimension(700, Math.max(400, y)));
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createRow(int playlistId, String name, int y) {
        JPanel row = new JPanel(null);
        row.setBounds(0, y, 700, 60);
        row.setBackground(SURFACE);

        JLabel lbl = new JLabel(name);
        lbl.setBounds(20, 15, 300, 30);
        lbl.setForeground(TEXT);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        row.add(lbl);

        JButton addSong = new JButton("Add Song");
        addSong.setBounds(480, 12, 100, 36);
        styleButton(addSong);

        addSong.addActionListener(e -> {
            String idStr = JOptionPane.showInputDialog(
                    this,
                    "Enter Song ID:",
                    "Add Song",
                    JOptionPane.PLAIN_MESSAGE
            );
            if (idStr == null) return;

            try {
                int songId = Integer.parseInt(idStr);
                BackendBridge.addToPlaylist(playlistId, songId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Song ID");
            }
        });

        row.add(addSong);
        return row;
    }

    // ================= UI HELPERS =================

    private void styleButton(JButton btn) {
        btn.setBackground(ACCENT);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
