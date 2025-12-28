import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FavoritesUI extends JFrame {

    public FavoritesUI() {
        super("Favorites Playlist");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
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

        JPanel listPanel = new JPanel(null);
        listPanel.setBackground(new Color(0x0E0E11));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBounds(100, 100, 650, 600);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(0x0E0E11));
        add(scroll);

        loadSongs(listPanel);
        setVisible(true);
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
