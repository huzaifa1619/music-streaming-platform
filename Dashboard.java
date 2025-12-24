import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;

public class Dashboard extends JFrame {

    private final Color BG = new Color(0x0E0E11);
    private final Color SURFACE = new Color(0x1C1C22);
    private final Color CARD_BG = new Color(0x181820);
    private final Color PRIMARY = new Color(0x635BFF);
    private final Color SECONDARY = new Color(0x1BC7B1);
    private final Color TEXT = new Color(0xF2F2F4);
    private final Color SUBTEXT = new Color(0xA2A2A8);
    private final Color HOVER = new Color(0x282832);

    private JPanel trendingContainer;
    private JPanel quickPicksContainer;
    private JPanel historyPanel;
    private String loggedUser;
    private JTextField search;
    private List<SongDTO> allSongs = new ArrayList<>();

    public Dashboard() {
        this(null);
    }

    public Dashboard(String loggedUser) {
        this.loggedUser = loggedUser;

        // Backend init
        try {
            // Note: If you don't have BackendBridge compiled, this might throw errors.
            // Wrap in try-catch or comment out if testing UI only.
            try {
                if (!BackendBridge.isRunning()) {
                    BackendBridge.startBackend();
                    BackendBridge.getAllSongs();
                }
            } catch (Throwable t) {
                System.out.println("Backend bridge not found or failed, skipping...");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setTitle("SoundRaft");
        setSize(1400, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        // Load all songs first
        loadAllSongs();

        // Build UI
        add(createSidebar(), BorderLayout.WEST);
        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        setVisible(true);
    }

    private void loadAllSongs() {
    allSongs.clear();

    try {
        // Fetch from C++ backend (CSV-based)
        List<SongDTO> songs = BackendBridge.getAllSongs();
        if (songs != null && !songs.isEmpty()) {
            allSongs.addAll(songs);
            return;
        }
    } catch (Exception e) {
        System.out.println("Backend not available, loading dummy data...");
    }

    
}


    // ==================== MAIN CONTENT ====================
    private JScrollPane createMainContent() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 40, 30));

        // Trending Now Section
        mainPanel.add(createSectionLabel("Trending Now"));
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(createTrendingSection());
        mainPanel.add(Box.createVerticalStrut(40));

        // Quick Picks Section
        mainPanel.add(createSectionLabel("Quick Picks"));
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(createQuickPicksSection());
        mainPanel.add(Box.createVerticalStrut(40));

        // Recently Played Section
        mainPanel.add(createSectionLabel("Recently Played"));
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(createHistorySection());

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        customizeScrollBar(scrollPane.getVerticalScrollBar());

        return scrollPane;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 26));
        label.setForeground(TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // ==================== TRENDING SECTION (FIXED) ====================
    private JPanel createTrendingSection() {
        // Container with fixed height
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 270));
        wrapper.setPreferredSize(new Dimension(800, 270));

        // Inner panel for cards with FlowLayout
        trendingContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        trendingContainer.setBackground(BG);

        // Load songs
        loadTrendingSongs();

        // Calculate required width for scrolling
        int cardWidth = 180;
        int gap = 20;
        int totalWidth = (cardWidth + gap) * Math.min(allSongs.size(), 15) + 20;
        trendingContainer.setPreferredSize(new Dimension(totalWidth, 250));

        // Create horizontal scroll pane
        JScrollPane scrollPane = new JScrollPane(trendingContainer);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(40);
        customizeScrollBar(scrollPane.getHorizontalScrollBar());

        // Enable mouse wheel horizontal scrolling
        scrollPane.addMouseWheelListener(e -> {
            JScrollBar hBar = scrollPane.getHorizontalScrollBar();
            int delta = e.getUnitsToScroll() * 25;
            hBar.setValue(hBar.getValue() + delta);
        });

        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private void loadTrendingSongs() {
        trendingContainer.removeAll();

        List<SongDTO> shuffled = new ArrayList<>(allSongs);
        Collections.shuffle(shuffled);

        int count = 0;
        for (SongDTO song : shuffled) {
            if (count >= 15) break;
            trendingContainer.add(createTrendingCard(song));
            count++;
        }

        // Update preferred size
        int cardWidth = 180;
        int gap = 20;
        int totalWidth = (cardWidth + gap) * count + 40;
        trendingContainer.setPreferredSize(new Dimension(totalWidth, 250));

        trendingContainer.revalidate();
        trendingContainer.repaint();
    }

    private JPanel createTrendingCard(SongDTO song) {
        // Fixed size card
        final int CARD_WIDTH = 180;
        final int CARD_HEIGHT = 240;
        final int ART_SIZE = 150;

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };

        card.setLayout(null);
        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setBackground(SURFACE);
        card.setOpaque(false);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Album art panel
        JPanel artPanel = new JPanel() {
            private Image cachedImage = null;
            private boolean imageLoaded = false;

            {
                loadImage();
            }

            private void loadImage() {
                if (song.imagePath != null && !song.imagePath.trim().isEmpty()) {
                    try {
                        File imgFile = new File(song.imagePath);
                        if (imgFile.exists()) {
                            cachedImage = ImageIO.read(imgFile);
                            imageLoaded = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // Rounded clip
                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

                if (cachedImage != null) {
                    // Draw scaled image
                    g2.drawImage(cachedImage, 0, 0, getWidth(), getHeight(), null);
                } else {
                    // Placeholder
                    GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0x2A2A35),
                        getWidth(), getHeight(), new Color(0x1E1E28)
                    );
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    // Music note icon
                    g2.setColor(new Color(0x505060));
                    g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 48));
                    FontMetrics fm = g2.getFontMetrics();
                    String note = "♪";
                    int x = (getWidth() - fm.stringWidth(note)) / 2;
                    int y = (getHeight() + fm.getAscent()) / 2 - 10;
                    g2.drawString(note, x, y);
                }

                g2.dispose();
            }
        };
        artPanel.setBounds(15, 12, ART_SIZE, ART_SIZE - 30);
        artPanel.setOpaque(false);
        card.add(artPanel);

        // Title
        JLabel titleLabel = new JLabel(truncate(song.title, 18));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT);
        titleLabel.setBounds(15, ART_SIZE - 10, CARD_WIDTH - 50, 22);
        card.add(titleLabel);

        // Play button
        JButton playBtn = createCircularPlayButton(32);
        playBtn.setBounds(CARD_WIDTH - 48, ART_SIZE - 15, 32, 32);
        playBtn.setVisible(false);
        playBtn.addActionListener(e -> playSong(song));
        card.add(playBtn);

        // Artist
        JLabel artistLabel = new JLabel(truncate(song.artist, 22));
        artistLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        artistLabel.setForeground(SUBTEXT);
        artistLabel.setBounds(15, ART_SIZE + 12, CARD_WIDTH - 30, 18);
        card.add(artistLabel);

        // Duration
        JLabel durLabel = new JLabel(formatDuration(song.duration));
        durLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        durLabel.setForeground(new Color(0x666670));
        durLabel.setBounds(15, ART_SIZE + 30, 80, 16);
        card.add(durLabel);

        // Hover effects
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(HOVER);
                playBtn.setVisible(true);
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(SURFACE);
                playBtn.setVisible(false);
                card.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                playSong(song);
            }
        });

        return card;
    }

    // ==================== QUICK PICKS SECTION (FIXED) ====================
    private JPanel createQuickPicksSection() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        wrapper.setPreferredSize(new Dimension(800, 230));

        // Container for columns
        quickPicksContainer = new JPanel();
        quickPicksContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 0));
        quickPicksContainer.setBackground(BG);

        loadQuickPicks();

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(quickPicksContainer);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(40);
        customizeScrollBar(scrollPane.getHorizontalScrollBar());

        // Mouse wheel horizontal scroll
        scrollPane.addMouseWheelListener(e -> {
            JScrollBar hBar = scrollPane.getHorizontalScrollBar();
            int delta = e.getUnitsToScroll() * 25;
            hBar.setValue(hBar.getValue() + delta);
        });

        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private void loadQuickPicks() {
        quickPicksContainer.removeAll();

        // Create columns of 3 cards each
        int cardsPerColumn = 3;
        int columnWidth = 300;
        int numColumns = (int) Math.ceil((double) allSongs.size() / cardsPerColumn);

        for (int col = 0; col < numColumns; col++) {
            JPanel column = new JPanel();
            column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
            column.setBackground(BG);
            column.setAlignmentY(Component.TOP_ALIGNMENT);

            for (int row = 0; row < cardsPerColumn; row++) {
                int index = col * cardsPerColumn + row;
                if (index < allSongs.size()) {
                    if (row > 0) column.add(Box.createVerticalStrut(10));
                    column.add(createQuickPickCard(allSongs.get(index)));
                }
            }

            quickPicksContainer.add(column);
        }

        // Update container size
        int totalWidth = (columnWidth + 15) * numColumns + 30;
        quickPicksContainer.setPreferredSize(new Dimension(totalWidth, 210));

        quickPicksContainer.revalidate();
        quickPicksContainer.repaint();
    }

    private JPanel createQuickPickCard(SongDTO song) {
        final int CARD_WIDTH = 290;
        final int CARD_HEIGHT = 62;
        final int ART_SIZE = 48;

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
            }
        };

        card.setLayout(new BorderLayout(12, 0));
        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setBackground(CARD_BG);
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 12));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Album art
        JPanel artPanel = new JPanel() {
            private Image cachedImage = null;

            {
                if (song.imagePath != null && !song.imagePath.trim().isEmpty()) {
                    try {
                        File imgFile = new File(song.imagePath);
                        if (imgFile.exists()) {
                            cachedImage = ImageIO.read(imgFile);
                        }
                    } catch (Exception e) {
                    }
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));

                if (cachedImage != null) {
                    g2.drawImage(cachedImage, 0, 0, getWidth(), getHeight(), null);
                } else {
                    g2.setColor(new Color(0x2A2A35));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(new Color(0x505060));
                    g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
                    g2.drawString("♪", 15, 32);
                }

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(ART_SIZE, ART_SIZE);
            }
        };
        artPanel.setOpaque(false);
        card.add(artPanel, BorderLayout.WEST);

        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel titleLabel = new JLabel(truncate(song.title, 28));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel artistLabel = new JLabel(truncate(song.artist, 32));
        artistLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        artistLabel.setForeground(SUBTEXT);
        artistLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(artistLabel);

        card.add(infoPanel, BorderLayout.CENTER);

        // Play button (hidden by default)
        JButton playBtn = createCircularPlayButton(30);
        playBtn.setVisible(false);
        playBtn.addActionListener(e -> playSong(song));

        JPanel btnWrapper = new JPanel(new GridBagLayout());
        btnWrapper.setOpaque(false);
        btnWrapper.add(playBtn);
        card.add(btnWrapper, BorderLayout.EAST);

        // Hover effects
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(HOVER);
                playBtn.setVisible(true);
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD_BG);
                playBtn.setVisible(false);
                card.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                playSong(song);
            }
        });

        return card;
    }

    // ==================== HISTORY SECTION ====================
    private JPanel createHistorySection() {
        historyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        historyPanel.setBackground(BG);
        historyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel placeholder = new JLabel("Play some songs to see your history here");
        placeholder.setForeground(SUBTEXT);
        placeholder.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        historyPanel.add(placeholder);

        return historyPanel;
    }

    // ==================== SIDEBAR ====================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(260, getHeight()));
        sidebar.setBackground(SURFACE);
        sidebar.setLayout(null);

        JLabel appName = new JLabel("SoundRaft");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 30));
        appName.setForeground(PRIMARY);
        appName.setBounds(24, 20, 220, 40);
        sidebar.add(appName);

        String[] menuItems = {"Home", "Your Library", "Playlists", "Favorites", "Settings"};
        int y = 90;

        for (String item : menuItems) {
            JButton btn = createSidebarButton(item);
            btn.setBounds(20, y, 220, 42);
            sidebar.add(btn);
            y += 55;
        }

        return sidebar;
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(TEXT);
        btn.setBackground(SURFACE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (text.equals("Playlists")) {
            btn.addActionListener(e -> new PlaylistUI().setVisible(true));
        } else if (text.equals("Favorites")) {
            btn.addActionListener(e -> new FavoritesUI().setVisible(true));
        } else if (text.equals("Home")) {
            btn.addActionListener(e -> {
                loadAllSongs();
                loadTrendingSongs();
                loadQuickPicks();
            });
        }

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(SECONDARY); }
            public void mouseExited(MouseEvent e) { btn.setForeground(TEXT); }
        });

        return btn;
    }

    // ==================== HEADER ====================
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setPreferredSize(new Dimension(getWidth(), 72));
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        // Search field
        search = new JTextField("  Search songs, artists...");
        search.setPreferredSize(new Dimension(380, 42));
        search.setBackground(new Color(0x1A1A1E));
        search.setForeground(SUBTEXT);
        search.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        search.setCaretColor(TEXT);
        search.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x2A2A30), 1),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));

        search.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (search.getText().contains("Search songs")) {
                    search.setText("");
                    search.setForeground(TEXT);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (search.getText().trim().isEmpty()) {
                    search.setText("  Search songs, artists...");
                    search.setForeground(SUBTEXT);
                }
            }
        });

        search.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String query = search.getText().trim();
                if (query.isEmpty() || query.contains("Search songs")) {
                    loadTrendingSongs();
                } else {
                    performSearch(query);
                }
            }
        });

        header.add(search, BorderLayout.WEST);

        // Right side (user info or login buttons)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setBackground(BG);

        if (loggedUser != null && !loggedUser.isEmpty()) {
            // Username with gradient
            JLabel userName = new JLabel(loggedUser) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                    GradientPaint gp = new GradientPaint(
                        0, 0, SECONDARY,
                        getWidth(), 0, PRIMARY
                    );
                    g2.setPaint(gp);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(getText(), 0, fm.getAscent());
                    g2.dispose();
                }
            };
            userName.setFont(new Font("Segoe UI", Font.BOLD, 15));
            userName.setPreferredSize(new Dimension(
                userName.getFontMetrics(userName.getFont()).stringWidth(loggedUser) + 10, 28
            ));
            rightPanel.add(userName);

            // Profile avatar
            JPanel avatar = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0xFF6B6B),
                        getWidth(), getHeight(), new Color(0xFF8E53)
                    );
                    g2.setPaint(gp);
                    g2.fillOval(0, 0, getWidth(), getHeight());

                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
                    String initial = loggedUser.substring(0, 1).toUpperCase();
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(initial)) / 2;
                    int y = (getHeight() + fm.getAscent()) / 2 - 3;
                    g2.drawString(initial, x, y);

                    g2.dispose();
                }
            };
            avatar.setPreferredSize(new Dimension(40, 40));
            avatar.setOpaque(false);
            avatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            avatar.setToolTipText("Click to logout");

            avatar.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int choice = JOptionPane.showConfirmDialog(
                        Dashboard.this, "Do you want to logout?", "Logout", JOptionPane.YES_NO_OPTION
                    );
                    if (choice == JOptionPane.YES_OPTION) {
                        dispose();
                        new Dashboard("").setVisible(true);
                    }
                }
            });

            rightPanel.add(avatar);
        } else {
            // Guest mode
            JButton signupBtn = new JButton("Sign up");
            signupBtn.setForeground(SUBTEXT);
            signupBtn.setBackground(BG);
            signupBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            signupBtn.setFocusPainted(false);
            signupBtn.setBorderPainted(false);
            signupBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            signupBtn.addActionListener(e -> new SignupUI().setVisible(true));
            signupBtn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { signupBtn.setForeground(TEXT); }
                public void mouseExited(MouseEvent e) { signupBtn.setForeground(SUBTEXT); }
            });
            rightPanel.add(signupBtn);

            JButton loginBtn = new JButton("Log in");
            loginBtn.setForeground(Color.BLACK);
            loginBtn.setBackground(Color.WHITE);
            loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            loginBtn.setFocusPainted(false);
            loginBtn.setPreferredSize(new Dimension(90, 36));
            loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            loginBtn.addActionListener(e -> {
                dispose();
                new LoginUI().setVisible(true);
            });
            rightPanel.add(loginBtn);
        }

        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    // ==================== HELPER METHODS ====================

    private JButton createCircularPlayButton(int size) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Circle
                g2.setColor(SECONDARY);
                g2.fillOval(0, 0, getWidth(), getHeight());

                // Play triangle
                g2.setColor(Color.BLACK);
                int cx = getWidth() / 2 + 2;
                int cy = getHeight() / 2;
                int triSize = getWidth() / 4;
                int[] xPoints = {cx - triSize / 2, cx - triSize / 2, cx + triSize / 2 + 2};
                int[] yPoints = {cy - triSize, cy + triSize, cy};
                g2.fillPolygon(xPoints, yPoints, 3);

                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(size, size));
        btn.setMinimumSize(new Dimension(size, size));
        btn.setMaximumSize(new Dimension(size, size));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void customizeScrollBar(JScrollBar scrollBar) {
        scrollBar.setPreferredSize(new Dimension(8, 8));
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(0x505060);
                this.trackColor = new Color(0x1A1A20);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createEmptyButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createEmptyButton();
            }

            private JButton createEmptyButton() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                return btn;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 1,
                    thumbBounds.width - 2, thumbBounds.height - 2, 6, 6);
                g2.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                g.setColor(trackColor);
                g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            }
        });
    }

    private void performSearch(String query) {
        trendingContainer.removeAll();

        String lowerQuery = query.toLowerCase();
        int count = 0;

        for (SongDTO song : allSongs) {
            if (song.title.toLowerCase().contains(lowerQuery) ||
                song.artist.toLowerCase().contains(lowerQuery)) {
                trendingContainer.add(createTrendingCard(song));
                count++;
            }
        }

        if (count == 0) {
            JLabel noResults = new JLabel("No songs found for: \"" + query + "\"");
            noResults.setForeground(SUBTEXT);
            noResults.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            trendingContainer.add(noResults);
        }

        int cardWidth = 180;
        int totalWidth = (cardWidth + 20) * count + 40;
        trendingContainer.setPreferredSize(new Dimension(Math.max(totalWidth, 800), 250));

        trendingContainer.revalidate();
        trendingContainer.repaint();
    }

    private void playSong(SongDTO song) {
        if (song.filePath == null || song.filePath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Song path is missing!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File songFile = new File(song.filePath);
        if (!songFile.exists()) {
            JOptionPane.showMessageDialog(this,
                "Song file not found:\n" + song.filePath,
                "File Not Found",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Set queue
        int index = allSongs.indexOf(song);
        if (index == -1) index = 0;
        MusicPlayerUI.setQueue(allSongs, index);

        // Open player
        new MusicPlayerUI(song.songId,
             song.filePath,
              song.title,
               song.artist,
                song.duration,
                 song.imagePath,
                  null).setVisible(true);
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen - 2) + "..." : s;
    }

    private String formatDuration(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%d:%02d", m, s);
    }

    // ==================== MAIN METHOD ADDED HERE ====================
    public static void main(String[] args) {
        // Run on the Event Dispatch Thread for thread safety
        SwingUtilities.invokeLater(() -> {
            try {
                // Instantiate the Dashboard with a test user name
                new Dashboard("Guest User");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}