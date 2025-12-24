import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.ArrayList;

public class MusicPlayerUI extends JFrame {

    // Colors
    private final Color BG = new Color(0x0E0E11);
    private final Color ACCENT = new Color(0x1DB954);
    private final Color ACCENT_RED = new Color(0xFF3B30);

    // Playback
    private volatile Clip audioClip;
    private volatile boolean playing = false;
    private volatile boolean isPaused = false;
    private File currentFile;
    private int durationSeconds = 0;
    private long clipPosition = 0;

    // Volume
    private float currentVolume = 0.7f;
    private float previousVolume = 0.7f;
    private FloatControl volumeControl;

    // UI
    private JButton playPauseBtn;
    private ProgressBarPanel progressBar;
    private VolumeBarPanel volumeBar;
    private JLabel elapsedLabel;
    private JLabel remainingLabel;
    private JLabel titleLabel;
    private JLabel artistLabel;
    private JLabel volumeIcon;
    private Timer progressTimer;
    private int elapsedSeconds = 0;

    // Images
    private BufferedImage originalImage = null;
    private BufferedImage blurredBackground = null;
    private String imagePath;

    // Song info

    private int songId;
    private String title;
    private String artist;

    // Queue
    private static List<SongDTO> songQueue = new ArrayList<>();
    private static int currentQueueIndex = 0;

    // Favorite
    private boolean isFavorite = false;
    private JLabel heartLabel;

    // Mode
    private boolean isCompactMode = false;
    private JPanel centerPanel;
    private JPanel albumArtPanel;

    // Callbacks
    private Runnable onFinish;

    public static void setQueue(List<SongDTO> queue, int startIndex) {
        songQueue = new ArrayList<>(queue);
        currentQueueIndex = Math.max(0, Math.min(startIndex, queue.size() - 1));
    }

    // Constructor with imagePath parameter
    public MusicPlayerUI(int songId, String filePath, String title, String artist, int durationSeconds, String imagePath, Runnable onFinish) {
        super("SoundRaft Player");
        this.currentFile = new File(filePath);
        this.durationSeconds = durationSeconds;
        this.onFinish = onFinish;
        this.title = title;
        this.artist = artist;
        this.imagePath = imagePath;
        this.songId = songId;

        // Try to load image
        loadImages();

        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        buildUI();

        setVisible(true);

        // Start playback
        SwingUtilities.invokeLater(() -> {
            if (currentFile.exists()) {
                startPlayback();
            } else {
                JOptionPane.showMessageDialog(this, "Audio file not found:\n" + currentFile.getAbsolutePath());
            }
        });
    }

    // Backward compatible constructor
    public MusicPlayerUI(int songId, String filePath, String title, String artist, int durationSeconds, Runnable onFinish) {
        this(songId, filePath, title, artist, durationSeconds, findImageForSong(filePath), onFinish);
    }

    private static String findImageForSong(String audioPath) {
        if (audioPath == null) return null;

        File audioFile = new File(audioPath);
        String baseName = audioFile.getName();
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) baseName = baseName.substring(0, dotIndex);

        // Check same directory
        String[] extensions = {".jpg", ".jpeg", ".png", ".webp"};
        File parentDir = audioFile.getParentFile();

        if (parentDir != null) {
            for (String ext : extensions) {
                File imgFile = new File(parentDir, baseName + ext);
                if (imgFile.exists()) return imgFile.getAbsolutePath();
            }
        }

        // Check songs folder
        File songsDir = new File("songs");
        if (songsDir.exists()) {
            for (String ext : extensions) {
                File imgFile = new File(songsDir, baseName + ext);
                if (imgFile.exists()) return imgFile.getAbsolutePath();
            }
        }

        return null;
    }

    private void loadImages() {
        originalImage = null;
        blurredBackground = null;

        // Try provided imagePath first
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                File imgFile = new File(imagePath);
                if (imgFile.exists()) {
                    originalImage = ImageIO.read(imgFile);
                }
            } catch (Exception e) {
                System.err.println("Failed to load image from imagePath: " + e.getMessage());
            }
        }

        // Fallback: try to find image based on audio file name
        if (originalImage == null) {
            String foundPath = findImageForSong(currentFile.getAbsolutePath());
            if (foundPath != null) {
                try {
                    originalImage = ImageIO.read(new File(foundPath));
                    imagePath = foundPath;
                } catch (Exception e) {
                    System.err.println("Failed to load fallback image: " + e.getMessage());
                }
            }
        }

        // Create blurred background
        if (originalImage != null) {
            blurredBackground = createBlurredImage(originalImage, 40);
        }
    }

    private BufferedImage createBlurredImage(BufferedImage source, int radius) {
        // First, scale down the image for faster blur
        int smallWidth = 100;
        int smallHeight = (int) (source.getHeight() * (100.0 / source.getWidth()));

        BufferedImage small = new BufferedImage(smallWidth, smallHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = small.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, smallWidth, smallHeight, null);
        g.dispose();

        // Apply box blur multiple times
        BufferedImage blurred = small;
        for (int i = 0; i < 6; i++) {
            blurred = applyBoxBlur(blurred, 8);
        }

        return blurred;
    }

    private BufferedImage applyBoxBlur(BufferedImage source, int radius) {
        int w = source.getWidth();
        int h = source.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int r = 0, g = 0, b = 0, a = 0, count = 0;

                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        int nx = Math.max(0, Math.min(w - 1, x + dx));
                        int ny = Math.max(0, Math.min(h - 1, y + dy));
                        int pixel = source.getRGB(nx, ny);
                        a += (pixel >> 24) & 0xFF;
                        r += (pixel >> 16) & 0xFF;
                        g += (pixel >> 8) & 0xFF;
                        b += pixel & 0xFF;
                        count++;
                    }
                }

                int avgA = a / count;
                int avgR = r / count;
                int avgG = g / count;
                int avgB = b / count;
                result.setRGB(x, y, (avgA << 24) | (avgR << 16) | (avgG << 8) | avgB);
            }
        }

        return result;
    }

    private void buildUI() {
        // Top bar
        add(createTopBar(), BorderLayout.NORTH);

        // Center - album art and info
        centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Bottom - controls
        add(createBottomControls(), BorderLayout.SOUTH);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(0, 0, 0, 180));
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JLabel appTitle = new JLabel("SoundRaft");
        appTitle.setForeground(Color.WHITE);
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        topBar.add(appTitle, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        // Compact/Expand toggle
        JButton toggleBtn = createIconButton("⛶", 20);
        toggleBtn.setToolTipText("Toggle compact mode");
        toggleBtn.addActionListener(e -> toggleCompactMode());
        rightPanel.add(toggleBtn);

        // Minimize
        JButton minBtn = createIconButton("—", 20);
        minBtn.addActionListener(e -> setState(JFrame.ICONIFIED));
        rightPanel.add(minBtn);

        // Close
        JButton closeBtn = createIconButton("✕", 20);
        closeBtn.addActionListener(e -> stopAndClose());
        rightPanel.add(closeBtn);

        topBar.add(rightPanel, BorderLayout.EAST);
        return topBar;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // Draw blurred background
                if (blurredBackground != null) {
                    g2.drawImage(blurredBackground, 0, 0, getWidth(), getHeight(), null);
                } else {
                    // Gradient fallback
                    GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0x1a1a2e),
                        0, getHeight(), new Color(0x0f0f17)
                    );
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }

                // Dark overlay
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.dispose();
            }
        };
        panel.setOpaque(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Album art
        albumArtPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int size = Math.min(getWidth(), getHeight());
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                // Shadow
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fill(new RoundRectangle2D.Float(x + 10, y + 10, size, size, 24, 24));

                // Clip to rounded rect
                g2.setClip(new RoundRectangle2D.Float(x, y, size, size, 24, 24));

                if (originalImage != null) {
                    g2.drawImage(originalImage, x, y, size, size, null);
                } else {
                    // Placeholder
                    GradientPaint gp = new GradientPaint(
                        x, y, new Color(0x2A2A35),
                        x + size, y + size, new Color(0x1E1E28)
                    );
                    g2.setPaint(gp);
                    g2.fillRect(x, y, size, size);

                    // Music note
                    g2.setClip(null);
                    g2.setColor(new Color(0x555565));
                    g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, size / 3));
                    FontMetrics fm = g2.getFontMetrics();
                    String note = "♪";
                    int nx = x + (size - fm.stringWidth(note)) / 2;
                    int ny = y + (size + fm.getAscent()) / 2 - 20;
                    g2.drawString(note, nx, ny);
                }

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(320, 320);
            }
        };
        albumArtPanel.setOpaque(false);
        albumArtPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(albumArtPanel);
        contentPanel.add(Box.createVerticalStrut(35));

        // Title
        titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 34));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(8));

        // Artist
        artistLabel = new JLabel(artist);
        artistLabel.setForeground(new Color(0xB0B0B0));
        artistLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        artistLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(artistLabel);

        panel.add(contentPanel);
        return panel;
    }

    private JPanel createBottomControls() {
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBackground(new Color(0x0B0B0D));
        bottom.setBorder(BorderFactory.createEmptyBorder(18, 50, 25, 50));

        // Progress bar row
        JPanel progressRow = new JPanel(new BorderLayout(12, 0));
        progressRow.setOpaque(false);
        progressRow.setMaximumSize(new Dimension(700, 28));
        progressRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        elapsedLabel = new JLabel("0:00");
        elapsedLabel.setForeground(new Color(0xA0A0A0));
        elapsedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        elapsedLabel.setPreferredSize(new Dimension(40, 20));

        progressBar = new ProgressBarPanel();
        progressBar.setPreferredSize(new Dimension(500, 20));
        progressBar.addSeekListener(pct -> {
            if (durationSeconds > 0) {
                seekTo((int) (pct * durationSeconds));
            }
        });

        remainingLabel = new JLabel("-" + formatTime(durationSeconds));
        remainingLabel.setForeground(new Color(0xA0A0A0));
        remainingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        remainingLabel.setPreferredSize(new Dimension(45, 20));
        remainingLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        progressRow.add(elapsedLabel, BorderLayout.WEST);
        progressRow.add(progressBar, BorderLayout.CENTER);
        progressRow.add(remainingLabel, BorderLayout.EAST);

        bottom.add(progressRow);
        bottom.add(Box.createVerticalStrut(16));

        // Controls row
        JPanel controlsRow = new JPanel(new BorderLayout());
        controlsRow.setOpaque(false);
        controlsRow.setMaximumSize(new Dimension(850, 60));
        controlsRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Left - empty space for balance
        JPanel leftSpacer = new JPanel();
        leftSpacer.setOpaque(false);
        leftSpacer.setPreferredSize(new Dimension(200, 50));
        controlsRow.add(leftSpacer, BorderLayout.WEST);

        // Center - playback controls
        JPanel centerControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 22, 0));
        centerControls.setOpaque(false);

        JButton shuffleBtn = createIconButton("🔀", 18);
        centerControls.add(shuffleBtn);

        JButton prevBtn = createControlButton("prev");
        prevBtn.addActionListener(e -> playPrevious());
        centerControls.add(prevBtn);

        playPauseBtn = createPlayPauseButton();
        playPauseBtn.addActionListener(e -> togglePlayPause());
        centerControls.add(playPauseBtn);

        JButton nextBtn = createControlButton("next");
        nextBtn.addActionListener(e -> playNext());
        centerControls.add(nextBtn);

        JButton repeatBtn = createIconButton("🔁", 18);
        centerControls.add(repeatBtn);

        controlsRow.add(centerControls, BorderLayout.CENTER);

        // Right - heart, volume
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightControls.setOpaque(false);
        rightControls.setPreferredSize(new Dimension(200, 50));

        // Heart
        heartLabel = new JLabel("♡");
        heartLabel.setForeground(Color.WHITE);
        heartLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        heartLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        checkFavoriteStatus();
        heartLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleFavorite();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isFavorite) heartLabel.setForeground(ACCENT_RED);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!isFavorite) heartLabel.setForeground(Color.WHITE);
            }
        });
        rightControls.add(heartLabel);

        rightControls.add(Box.createHorizontalStrut(8));

        // Volume icon
        volumeIcon = new JLabel("🔊");
        volumeIcon.setForeground(Color.WHITE);
        volumeIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        volumeIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        volumeIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleMute();
            }
        });
        rightControls.add(volumeIcon);

        // Volume bar
        volumeBar = new VolumeBarPanel();
        volumeBar.setPreferredSize(new Dimension(110, 20));
        volumeBar.setVolume(currentVolume);
        volumeBar.addVolumeListener(vol -> {
            currentVolume = vol;
            applyVolume();
            updateVolumeIcon();
        });
        rightControls.add(volumeBar);

        controlsRow.add(rightControls, BorderLayout.EAST);

        bottom.add(controlsRow);

        return bottom;
    }

    private JButton createIconButton(String text, int size) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI Symbol", Font.PLAIN, size));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createPlayPauseButton() {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // White circle
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, getWidth(), getHeight());

                // Icon
                g2.setColor(Color.BLACK);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                if (playing && !isPaused) {
                    // Pause bars
                    int barW = 5;
                    int barH = 16;
                    int gap = 4;
                    g2.fillRoundRect(cx - gap - barW, cy - barH / 2, barW, barH, 2, 2);
                    g2.fillRoundRect(cx + gap, cy - barH / 2, barW, barH, 2, 2);
                } else {
                    // Play triangle
                    int[] xPts = {cx - 6, cx - 6, cx + 9};
                    int[] yPts = {cy - 10, cy + 10, cy};
                    g2.fillPolygon(xPts, yPts, 3);
                }

                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(54, 54));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createControlButton(String type) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                if (type.equals("prev")) {
                    // |◀◀
                    g2.fillRect(cx - 12, cy - 8, 3, 16);
                    g2.fillPolygon(new int[]{cx - 6, cx + 2, cx + 2}, new int[]{cy, cy - 7, cy + 7}, 3);
                    g2.fillPolygon(new int[]{cx + 4, cx + 12, cx + 12}, new int[]{cy, cy - 7, cy + 7}, 3);
                } else {
                    // ▶▶|
                    g2.fillPolygon(new int[]{cx - 12, cx - 4, cx - 12}, new int[]{cy - 7, cy, cy + 7}, 3);
                    g2.fillPolygon(new int[]{cx - 2, cx + 6, cx - 2}, new int[]{cy - 7, cy, cy + 7}, 3);
                    g2.fillRect(cx + 9, cy - 8, 3, 16);
                }

                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(40, 40));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void toggleCompactMode() {
        isCompactMode = !isCompactMode;

        if (isCompactMode) {
            setExtendedState(JFrame.NORMAL);
            setSize(450, 180);
            setLocationRelativeTo(null);
            albumArtPanel.setVisible(false);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            artistLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            albumArtPanel.setVisible(true);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 34));
            artistLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        }

        revalidate();
        repaint();
    }

    private void checkFavoriteStatus() {
        try {
            if (BackendBridge.isFavorite(songId)) {
                isFavorite = true;
                heartLabel.setText("♥");
                heartLabel.setForeground(ACCENT_RED);
            }
        } catch (Exception e) {}
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;
        if (isFavorite) {
            heartLabel.setText("♥");
            heartLabel.setForeground(ACCENT_RED);
            try { BackendBridge.addFavorite(songId); } catch (Exception e) {}
        } else {
            heartLabel.setText("♡");
            heartLabel.setForeground(Color.WHITE);
            try { BackendBridge.removeFavorite(songId); } catch (Exception e) {}
        }
    }

    private void toggleMute() {
        if (currentVolume > 0) {
            previousVolume = currentVolume;
            currentVolume = 0;
        } else {
            currentVolume = previousVolume > 0 ? previousVolume : 0.7f;
        }
        volumeBar.setVolume(currentVolume);
        applyVolume();
        updateVolumeIcon();
    }

    private void updateVolumeIcon() {
        if (currentVolume == 0) {
            volumeIcon.setText("🔇");
        } else if (currentVolume < 0.3f) {
            volumeIcon.setText("🔈");
        } else if (currentVolume < 0.7f) {
            volumeIcon.setText("🔉");
        } else {
            volumeIcon.setText("🔊");
        }
    }

    private void applyVolume() {
        if (volumeControl != null) {
            float min = volumeControl.getMinimum();
            float max = volumeControl.getMaximum();
            float range = max - min;

            float db;
            if (currentVolume <= 0) {
                db = min;
            } else {
                db = min + (float) (Math.log10(1 + 9 * currentVolume) * range);
            }

            volumeControl.setValue(Math.max(min, Math.min(max, db)));
        }
    }

    // ==================== PLAYBACK ====================

    private void startPlayback() {
        stopPlayback();

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(currentFile);
            AudioFormat baseFormat = audioStream.getFormat();

            AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false
            );

            AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, audioStream);

            audioClip = AudioSystem.getClip();
            audioClip.open(decodedStream);

            if (audioClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
                applyVolume();
            }

            audioClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP && playing && !isPaused) {
                    if (audioClip.getFramePosition() >= audioClip.getFrameLength()) {
                        SwingUtilities.invokeLater(this::onSongEnded);
                    }
                }
            });

            audioClip.start();
            playing = true;
            isPaused = false;

            if (durationSeconds <= 0) {
                durationSeconds = (int) (audioClip.getMicrosecondLength() / 1_000_000);
                remainingLabel.setText("-" + formatTime(durationSeconds));
            }

            playPauseBtn.repaint();
            startProgressTimer();

        } catch (UnsupportedAudioFileException e) {
            JOptionPane.showMessageDialog(this,
                "Unsupported audio format. Please use WAV files.\nMP3 requires additional libraries.",
                "Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to play: " + ex.getMessage());
        }
    }

    private void togglePlayPause() {
        if (audioClip == null) return;

        if (playing && !isPaused) {
            clipPosition = audioClip.getMicrosecondPosition();
            audioClip.stop();
            isPaused = true;
            stopProgressTimer();
        } else {
            if (isPaused) {
                audioClip.setMicrosecondPosition(clipPosition);
            }
            audioClip.start();
            playing = true;
            isPaused = false;
            startProgressTimer();
        }
        playPauseBtn.repaint();
    }

    private void seekTo(int seconds) {
        if (audioClip == null) return;

        long micros = (long) seconds * 1_000_000;
        audioClip.setMicrosecondPosition(micros);
        elapsedSeconds = seconds;

        float pct = (durationSeconds > 0) ? (float) seconds / durationSeconds : 0f;
        progressBar.setProgress(pct);
        elapsedLabel.setText(formatTime(seconds));
        remainingLabel.setText("-" + formatTime(durationSeconds - seconds));

        if (!playing || isPaused) {
            clipPosition = micros;
        }
    }

    private void stopPlayback() {
        playing = false;
        isPaused = false;
        stopProgressTimer();

        if (audioClip != null) {
            audioClip.stop();
            audioClip.close();
            audioClip = null;
        }
        volumeControl = null;
    }

    private void stopAndClose() {
        stopPlayback();
        dispose();
    }

    private void onSongEnded() {
        stopProgressTimer();
        playing = false;
        playPauseBtn.repaint();

        // Auto-play next
        playNext();
    }

    private void playNext() {
        if (songQueue.isEmpty()) return;

        currentQueueIndex++;
        if (currentQueueIndex >= songQueue.size()) {
            currentQueueIndex = 0;
        }

        loadSongFromQueue(currentQueueIndex);
    }

    private void playPrevious() {
        if (songQueue.isEmpty()) return;

        if (elapsedSeconds > 3) {
            seekTo(0);
            return;
        }

        currentQueueIndex--;
        if (currentQueueIndex < 0) {
            currentQueueIndex = songQueue.size() - 1;
        }

        loadSongFromQueue(currentQueueIndex);
    }

    private void loadSongFromQueue(int index) {
        if (index < 0 || index >= songQueue.size()) return;

        stopPlayback();

        SongDTO song = songQueue.get(index);
        currentFile = new File(song.filePath);
        title = song.title;
        artist = song.artist;
        durationSeconds = song.duration;
        imagePath = song.imagePath;

        // Reload images
        loadImages();

        // Update UI
        titleLabel.setText(title);
        artistLabel.setText(artist);
        elapsedSeconds = 0;
        elapsedLabel.setText("0:00");
        remainingLabel.setText("-" + formatTime(durationSeconds));
        progressBar.setProgress(0);

        // Repaint to show new images
        centerPanel.repaint();
        albumArtPanel.repaint();

        // Reset favorite status
        isFavorite = false;
        heartLabel.setText("♡");
        heartLabel.setForeground(Color.WHITE);
        checkFavoriteStatus();

        if (currentFile.exists()) {
            startPlayback();
        }
    }

    private void startProgressTimer() {
        stopProgressTimer();
        progressTimer = new Timer();
        progressTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (!playing || isPaused || audioClip == null) return;

                long micros = audioClip.getMicrosecondPosition();
                elapsedSeconds = (int) (micros / 1_000_000);

                if (elapsedSeconds > durationSeconds) {
                    elapsedSeconds = durationSeconds;
                }

                float pct = (durationSeconds > 0) ? (float) elapsedSeconds / durationSeconds : 0f;

                SwingUtilities.invokeLater(() -> {
                    progressBar.setProgress(pct);
                    elapsedLabel.setText(formatTime(elapsedSeconds));
                    remainingLabel.setText("-" + formatTime(durationSeconds - elapsedSeconds));
                });
            }
        }, 100, 200);
    }

    private void stopProgressTimer() {
        if (progressTimer != null) {
            progressTimer.cancel();
            progressTimer = null;
        }
    }

    private String formatTime(int sec) {
        if (sec < 0) sec = 0;
        return String.format("%d:%02d", sec / 60, sec % 60);
    }

    // ==================== PROGRESS BAR ====================
    private static class ProgressBarPanel extends JPanel {
        private float progress = 0f;
        private SeekListener seekListener;
        private boolean hovering = false;
        private boolean dragging = false;

        public ProgressBarPanel() {
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    dragging = true;
                    updateFromMouse(e.getX());
                }
                public void mouseReleased(MouseEvent e) {
                    dragging = false;
                }
                public void mouseEntered(MouseEvent e) {
                    hovering = true;
                    repaint();
                }
                public void mouseExited(MouseEvent e) {
                    hovering = false;
                    repaint();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    updateFromMouse(e.getX());
                }
            });
        }

        private void updateFromMouse(int x) {
            float pct = Math.max(0f, Math.min(1f, (float) x / getWidth()));
            setProgress(pct);
            if (seekListener != null) seekListener.onSeek(pct);
        }

        public void setProgress(float p) {
            progress = Math.max(0f, Math.min(1f, p));
            repaint();
        }

        public void addSeekListener(SeekListener l) {
            seekListener = l;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int h = getHeight();
            int w = getWidth();
            int barH = (hovering || dragging) ? 6 : 4;
            int barY = (h - barH) / 2;

            // Track
            g2.setColor(new Color(0x4D4D4D));
            g2.fillRoundRect(0, barY, w, barH, barH, barH);

            // Progress
            int filled = (int) (w * progress);
            g2.setColor(new Color(0x1DB954));
            g2.fillRoundRect(0, barY, filled, barH, barH, barH);

            // Knob
            if (hovering || dragging || progress > 0) {
                int knobSize = 14;
                int knobX = Math.max(0, Math.min(w - knobSize, filled - knobSize / 2));
                int knobY = (h - knobSize) / 2;
                g2.setColor(Color.WHITE);
                g2.fillOval(knobX, knobY, knobSize, knobSize);
            }

            g2.dispose();
        }

        interface SeekListener { void onSeek(float pct); }
    }

    // ==================== VOLUME BAR ====================
    private static class VolumeBarPanel extends JPanel {
        private float volume = 0.7f;
        private VolumeListener volumeListener;

        public VolumeBarPanel() {
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    updateFromMouse(e.getX());
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    updateFromMouse(e.getX());
                }
            });
        }

        private void updateFromMouse(int x) {
            float vol = Math.max(0f, Math.min(1f, (float) x / getWidth()));
            setVolume(vol);
            if (volumeListener != null) volumeListener.onVolumeChange(vol);
        }

        public void setVolume(float v) {
            volume = Math.max(0f, Math.min(1f, v));
            repaint();
        }

        public void addVolumeListener(VolumeListener l) {
            volumeListener = l;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int h = getHeight();
            int w = getWidth();
            int barH = 4;
            int barY = (h - barH) / 2;

            // Track
            g2.setColor(new Color(0x4D4D4D));
            g2.fillRoundRect(0, barY, w, barH, barH, barH);

            // Volume
            int filled = (int) (w * volume);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, barY, filled, barH, barH, barH);

            // Knob
            int knobSize = 12;
            int knobX = Math.max(0, Math.min(w - knobSize, filled - knobSize / 2));
            int knobY = (h - knobSize) / 2;
            g2.setColor(Color.WHITE);
            g2.fillOval(knobX, knobY, knobSize, knobSize);

            g2.dispose();
        }

        interface VolumeListener { void onVolumeChange(float vol); }
    }
}