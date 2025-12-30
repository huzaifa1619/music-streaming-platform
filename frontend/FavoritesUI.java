import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FavoritesUI extends JFrame {

    public FavoritesUI() {
        super("Favorites Playlist");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(0x0E0E11));

        // Ensure backend is running
        try {
            if (!BackendBridge.isRunning()) {
                BackendBridge.startBackend();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Backend is not running!",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }

        // Back button
        JButton back = new JButton("✕");
        back.setBounds(20, 20, 50, 40);
        back.setBackground(new Color(0x1C1C22));
        back.setForeground(Color.WHITE);
        back.setFocusPainted(false);
        back.setBorder(null);
        back.addActionListener(e -> dispose());
        add(back);

        JLabel title = new JLabel("Favorites");
        title.setBounds(100, 20, 500, 40);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        add(title);

        // Add song to favorites
        JLabel addLabel = new JLabel("Add Song to Favorites:");
        addLabel.setBounds(100, 80, 300, 30);
        addLabel.setForeground(Color.WHITE);
        addLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(addLabel);

        // Dropdown for song selection
        List<SongDTO> allSongs = BackendBridge.getAllSongs();
        String[] songOptions = new String[allSongs.size()];
        for (int i = 0; i < allSongs.size(); i++) {
            SongDTO song = allSongs.get(i);
            songOptions[i] = song.songId + ": " + song.title + " - " + song.artist;
        }

        JComboBox<String> songDropdown = new JComboBox<>(songOptions);
        songDropdown.setBounds(100, 110, 400, 35);
        songDropdown.setBackground(new Color(0x1C1C22));
        songDropdown.setForeground(Color.WHITE);
        songDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        add(songDropdown);

        JButton addBtn = new JButton("Add");
        addBtn.setBounds(520, 110, 80, 35);
        addBtn.setBackground(new Color(0x635BFF));
        addBtn.setForeground(Color.BLACK);
        addBtn.setFocusPainted(false);
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addBtn.addActionListener(e -> {
            if (songDropdown.getSelectedIndex() >= 0) {
                SongDTO selectedSong = allSongs.get(songDropdown.getSelectedIndex());
                BackendBridge.addFavorite(selectedSong.songId);
                songDropdown.setSelectedIndex(0);
                JOptionPane.showMessageDialog(this, "Song added to favorites!");
                updateFavoritesDisplay();
            }
        });
        add(addBtn);

        JPanel listPanel = new JPanel(null);
        listPanel.setBackground(new Color(0x0E0E11));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBounds(100, 170, 650, 450);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(0x0E0E11));
        add(scroll);

        loadSongs(listPanel);
        setVisible(true);
    }

    private void updateFavoritesDisplay() {
        // This would be called after adding a favorite
        // The display updates automatically in loadSongs
    }

    private void loadSongs(JPanel list) {

        list.removeAll();

        int y = 0;
        List<SongDTO> favorites;

        try {
            favorites = BackendBridge.getFavorites();
        } catch (Exception e) {
            favorites = List.of();
        }

        if (favorites.isEmpty()) {
            JLabel empty = new JLabel("No favorite songs yet");
            empty.setBounds(20, 20, 400, 30);
            empty.setForeground(new Color(0xA2A2A8));
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 18));
            list.add(empty);
            list.setPreferredSize(new Dimension(500, 80));
            return;
        }

        for (SongDTO song : favorites) {

            JPanel row = new JPanel(null);
            row.setBounds(0, y, 600, 50);
            row.setBackground(new Color(0x29292E));

            JLabel name = new JLabel(song.title + " — " + song.artist);
            name.setBounds(20, 10, 360, 30);
            name.setForeground(Color.WHITE);
            name.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            row.add(name);

            // PLAY button
            JButton playBtn = new JButton("Play");
            playBtn.setBounds(390, 10, 70, 30);
            playBtn.setBackground(new Color(0x525252));
            playBtn.setForeground(Color.WHITE);
            playBtn.setBorder(null);
            playBtn.setFocusPainted(false);

            playBtn.addActionListener(e -> {
                if (song.filePath == null || song.filePath.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Song file not found",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                new MusicPlayerUI(
                    song.songId,
                    song.filePath,
                    song.title,
                    song.artist,
                    song.duration,
                    song.imagePath,
                    null
                ).setVisible(true);
            });

            row.add(playBtn);

            // REMOVE button
            JButton removeBtn = new JButton("Remove");
            removeBtn.setBounds(470, 10, 100, 30);
            removeBtn.setBackground(new Color(0xFF3B30));
            removeBtn.setForeground(Color.WHITE);
            removeBtn.setBorder(null);
            removeBtn.setFocusPainted(false);

            removeBtn.addActionListener(e -> {
                BackendBridge.removeFavorite(song.songId);
                loadSongs(list);
                list.revalidate();
                list.repaint();
            });

            row.add(removeBtn);

            list.add(row);
            y += 60;
        }

        list.setPreferredSize(new Dimension(600, y + 20));
        list.revalidate();
        list.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FavoritesUI::new);
    }
}