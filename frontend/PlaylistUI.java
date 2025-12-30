import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class PlaylistUI extends JFrame {

    private final Color BG = new Color(0x0E0E11);
    private final Color SURFACE = new Color(0x1C1C22);
    private final Color TEXT = new Color(0xF2F2F4);
    private final Color ACCENT = new Color(0x635BFF);

    private JPanel listPanel;

        // simple in-memory tracking (backend persists internally)
    private Map<Integer, String> playlists = new LinkedHashMap<>();
    private int selectedPlaylistId = -1;

    public PlaylistUI() {
        setTitle("Playlists");
        setSize(900, 700);
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

        // Load existing playlists from backend
        try {
            List<String> pls = BackendBridge.getPlaylists();
            for (String line : pls) {
                if (line == null || line.isEmpty()) continue;
                int comma = line.indexOf(',');
                if (comma <= 0) continue;
                int id = Integer.parseInt(line.substring(0, comma));
                String name = line.substring(comma + 1);
                playlists.put(id, name);
            }
        } catch (Exception ignored) {}

        // Create playlist button
        JButton createBtn = new JButton("+ New Playlist");
        createBtn.setBounds(650, 25, 200, 36);
        styleButton(createBtn);
        createBtn.addActionListener(e -> createPlaylist());
        add(createBtn);

        // Add song section
        JLabel addLabel = new JLabel("Add Song to Selected Playlist:");
        addLabel.setBounds(80, 80, 350, 25);
        addLabel.setForeground(TEXT);
        addLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        add(addLabel);

        // Dropdown for song selection
        List<SongDTO> allSongs = BackendBridge.getAllSongs();
        String[] songOptions = new String[allSongs.size()];
        for (int i = 0; i < allSongs.size(); i++) {
            SongDTO song = allSongs.get(i);
            songOptions[i] = song.songId + ": " + song.title + " - " + song.artist;
        }

        JComboBox<String> songDropdown = new JComboBox<>(songOptions);
        songDropdown.setBounds(80, 105, 400, 30);
        songDropdown.setBackground(SURFACE);
        songDropdown.setForeground(TEXT);
        songDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(songDropdown);

        JButton addSongBtn = new JButton("Add to Playlist");
        addSongBtn.setBounds(495, 105, 140, 30);
        styleButton(addSongBtn);
        addSongBtn.addActionListener(e -> {
            if (selectedPlaylistId == -1) {
                JOptionPane.showMessageDialog(this, "Please select a playlist first", "No Playlist Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (songDropdown.getSelectedIndex() >= 0) {
                SongDTO selectedSong = allSongs.get(songDropdown.getSelectedIndex());
                boolean success = BackendBridge.addToPlaylist(selectedPlaylistId, selectedSong.songId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Song added to playlist!");
                    songDropdown.setSelectedIndex(0);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add song", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        add(addSongBtn);

        listPanel = new JPanel(null);
        listPanel.setBackground(BG);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBounds(80, 160, 740, 480);
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

        int pid = (int) (System.currentTimeMillis() / 1000);

        boolean ok = BackendBridge.createPlaylist(pid, name);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Failed to create playlist");
            return;
        }

        // refresh from backend
        playlists.clear();
        try {
            List<String> pls = BackendBridge.getPlaylists();
            for (String line : pls) {
                if (line == null || line.isEmpty()) continue;
                int comma = line.indexOf(',');
                if (comma <= 0) continue;
                int id = Integer.parseInt(line.substring(0, comma));
                String pname = line.substring(comma + 1);
                playlists.put(id, pname);
            }
        } catch (Exception ignored) {}

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
        row.setBackground(selectedPlaylistId == playlistId ? new Color(0x635BFF) : SURFACE);

        JLabel lbl = new JLabel(name);
        lbl.setBounds(20, 15, 300, 30);
        lbl.setForeground(TEXT);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        row.add(lbl);

        JButton selectBtn = new JButton(selectedPlaylistId == playlistId ? "Selected" : "Select");
        selectBtn.setBounds(380, 12, 90, 36);
        styleButton(selectBtn);
        selectBtn.setBackground(selectedPlaylistId == playlistId ? new Color(0x1BC7B1) : ACCENT);
        selectBtn.addActionListener(e -> {
            selectedPlaylistId = playlistId;
            refreshUI();
        });
        row.add(selectBtn);

        JButton viewBtn = new JButton("View");
        viewBtn.setBounds(480, 12, 80, 36);
        styleButton(viewBtn);
        viewBtn.addActionListener(e -> {
            new PlaylistViewUI(playlistId, name).setVisible(true);
        });
        row.add(viewBtn);

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