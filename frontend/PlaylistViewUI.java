import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PlaylistViewUI extends JFrame {

    private final Color BG = new Color(0x0E0E11);
    private final Color SURFACE = new Color(0x1C1C22);
    private final Color TEXT = new Color(0xF2F2F4);
    private final Color SUBTEXT = new Color(0xA2A2A8);

    public PlaylistViewUI(int playlistId, String playlistName) {
        super("Playlist: " + playlistName);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 650);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG);

        // Back button
        JButton back = new JButton("←");
        back.setBounds(20, 20, 50, 40);
        back.setBackground(SURFACE);
        back.setForeground(TEXT);
        back.setFocusPainted(false);
        back.setBorderPainted(false);
        back.addActionListener(e -> dispose());
        add(back);

        JLabel title = new JLabel(playlistName);
        title.setBounds(90, 20, 600, 40);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT);
        add(title);

        // Play All button
        JButton playAll = new JButton("Play All");
        playAll.setBounds(680, 22, 80, 36);
        playAll.setBackground(new Color(0x1BC7B1));
        playAll.setForeground(Color.BLACK);
        playAll.setFocusPainted(false);
        playAll.setBorderPainted(false);
        playAll.addActionListener(e -> {
            List<SongDTO> songs = BackendBridge.getPlaylist(playlistId);
            if (songs == null || songs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No songs to play in this playlist.", "Empty Playlist", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Set queue and open player on first song
            MusicPlayerUI.setQueue(songs, 0);
            SongDTO first = songs.get(0);
            try {
                BackendBridge.playSong(first.songId);
            } catch (Exception ignored) {}

            new MusicPlayerUI(
                first.songId,
                first.filePath,
                first.title,
                first.artist,
                first.duration,
                first.imagePath,
                null
            ).setVisible(true);
        });
        add(playAll);

        JPanel listPanel = new JPanel(null);
        listPanel.setBackground(BG);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBounds(50, 80, 700, 520);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        add(scroll);

        loadPlaylistSongs(listPanel, playlistId);
        setVisible(true);
    }

    private void loadPlaylistSongs(JPanel list, int playlistId) {
        list.removeAll();

        int y = 0;
        List<SongDTO> songs;

        try {
            songs = BackendBridge.getPlaylist(playlistId);
        } catch (Exception e) {
            songs = List.of();
        }

        if (songs.isEmpty()) {
            JLabel empty = new JLabel("No songs in this playlist yet");
            empty.setBounds(20, 20, 400, 30);
            empty.setForeground(SUBTEXT);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            list.add(empty);
            list.setPreferredSize(new Dimension(600, 80));
            return;
        }

        for (SongDTO song : songs) {
            JPanel row = new JPanel(null);
            row.setBounds(0, y, 650, 50);
            row.setBackground(SURFACE);

            JLabel name = new JLabel(song.title + " — " + song.artist);
            name.setBounds(20, 10, 380, 30);
            name.setForeground(TEXT);
            name.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            row.add(name);

            // PLAY button
            JButton playBtn = new JButton("Play");
            playBtn.setBounds(410, 10, 70, 30);
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
            removeBtn.setBounds(490, 10, 100, 30);
            removeBtn.setBackground(new Color(0xFF3B30));
            removeBtn.setForeground(Color.WHITE);
            removeBtn.setBorder(null);
            removeBtn.setFocusPainted(false);

            removeBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this, "Remove this song from playlist?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean ok = BackendBridge.removeFromPlaylist(playlistId, song.songId);
                    if (!ok) {
                        JOptionPane.showMessageDialog(this, "Failed to remove song.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    loadPlaylistSongs(list, playlistId);
                    list.revalidate();
                    list.repaint();
                }
            });

            row.add(removeBtn);

            list.add(row);
            y += 60;
        }

        list.setPreferredSize(new Dimension(650, y + 20));
        list.revalidate();
        list.repaint();
    }
}
