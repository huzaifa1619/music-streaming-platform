import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RecommendationPage extends JFrame {

    private JPanel recPanel;
    private int baseSongId;

    // baseSongId = song used for recommendation
    public RecommendationPage(int baseSongId) {
        this.baseSongId = baseSongId;

        setTitle("Recommended For You");
        setSize(450, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0x0E0E11));

        JLabel title = new JLabel("Recommendations", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(title, BorderLayout.NORTH);

        recPanel = new JPanel();
        recPanel.setLayout(new BoxLayout(recPanel, BoxLayout.Y_AXIS));
        recPanel.setBackground(new Color(0x0E0E11));

        JScrollPane scroll = new JScrollPane(recPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(0x0E0E11));
        add(scroll, BorderLayout.CENTER);

        loadRecommendations();
        setVisible(true);
    }

    private void loadRecommendations() {
        recPanel.removeAll();

        List<SongDTO> recs = BackendBridge.recommend(baseSongId);

        if (recs.isEmpty()) {
            JLabel empty = new JLabel("No recommendations available");
            empty.setForeground(new Color(0xA2A2A8));
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            recPanel.add(Box.createVerticalStrut(40));
            recPanel.add(empty);
        }

        for (SongDTO song : recs) {
            recPanel.add(createCard(song));
            recPanel.add(Box.createVerticalStrut(12));
        }

        recPanel.revalidate();
        recPanel.repaint();
    }

    private JPanel createCard(SongDTO song) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(0x1C1C22));
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel lbl = new JLabel(
            "<html><b>" + song.title + "</b><br>" +
            song.artist + "<br><i>" + song.genre + "</i></html>"
        );
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        card.add(lbl, BorderLayout.CENTER);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (song.filePath == null || song.filePath.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        RecommendationPage.this,
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
            }
        });

        return card;
    }
}
