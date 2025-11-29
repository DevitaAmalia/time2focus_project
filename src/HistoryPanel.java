import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class HistoryPanel extends JPanel {

    // --- DEPENDENSI ---
    private final int userId;
    private final KoneksiDatabase db;
    
    // --- KOMPONEN UI ---
    private JButton btnMusic; // Tambahkan tombol music untuk sidebar
    private Image backgroundImage; // Tambahkan Background Image (dari DB)

    private final String PATH_ICON_SETTING = "assets/button/SettingButton.png";
    private final String PATH_ICON_HISTORY = "assets/button/HistoryButton.png";
    private final String PATH_ICON_MUSIC_ON = "assets/button/SongButton.png";
    private final String PATH_ICON_MUSIC_OFF = "assets/button/MuteButton.png";

    // --- FORMATTER ---
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    // ================== KONSTRUKTOR ==================
    public HistoryPanel(int userId, KoneksiDatabase db) {
        this.userId = userId;
        this.db = db;
        
        loadUserData();

        setLayout(new BorderLayout());
        setOpaque(false);
        
        // 1. SETUP SIDEBAR (KIRI) 
        JPanel sidebar = new JPanel();
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(30, 20, 0, 0));

        // Placeholder Button Default
        sidebar.add(createImageButton(PATH_ICON_SETTING, 40));
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(createImageButton(PATH_ICON_HISTORY, 40)); 
        sidebar.add(Box.createVerticalStrut(20));
        
        btnMusic = createImageButton(PATH_ICON_MUSIC_ON, 40); // Default ON
        btnMusic.addActionListener(e -> toggleMusic());
        sidebar.add(btnMusic);

        add(sidebar, BorderLayout.WEST);

        // 2. SETUP CENTER CONTENT (TENGAH) - Menggunakan GridBagLayout
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 50, 0, 50);

        // A. Judul
        JLabel titleLabel = new JLabel("SESSION HISTORY");
        titleLabel.setFont(Theme.FONT_TITLE.deriveFont(35f)); 
        titleLabel.setForeground(Theme.TEXT_WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 20, 0);
        centerPanel.add(titleLabel, gbc);

        // B. Muat dan Tampilkan Riwayat
        JScrollPane scrollPane = createHistoryList();
        gbc.gridy = 1;
        gbc.weighty = 1.0; // Agar scrollpane mengambil sisa ruang vertikal
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0); // Reset Insets untuk ScrollPane
        centerPanel.add(scrollPane, gbc);
        
        add(centerPanel, BorderLayout.CENTER);
    }
    
    // --- LOAD DATA DARI DB ---
    private void loadUserData() {
        Map<String, Object> settings = db.getUserSettings(userId);
        
        String bgPath = (String) settings.get("path_bg");
        if (bgPath != null) {
            this.backgroundImage = new ImageIcon(bgPath).getImage();
        }
    }
    
    // --- LOGIC MUSIK (TOGGLE) ---
    private void toggleMusic() {
        boolean isMuted = btnMusic.getIcon().toString().contains("MuteButton");
        if (isMuted) {
             updateButtonIcon(btnMusic, PATH_ICON_MUSIC_ON, 40);
        } else {
             updateButtonIcon(btnMusic, PATH_ICON_MUSIC_OFF, 40);
        }
    }

    // ================== MUAT DATA & BUAT LIST ==================

    private JScrollPane createHistoryList() {
        // Kontainer untuk menampung setiap item riwayat
        JPanel historyContainer = new JPanel();
        historyContainer.setLayout(new BoxLayout(historyContainer, BoxLayout.Y_AXIS));
        historyContainer.setOpaque(false); 

        List<Object[]> historyData = db.getSessionHistory(userId);
        
        if (historyData.isEmpty()) {
            JLabel emptyLabel = new JLabel("belum ada riwayat sesi yang tersimpan.");
            emptyLabel.setFont(Theme.FONT_BODY);
            emptyLabel.setForeground(Theme.TEXT_WHITE);
            historyContainer.add(Box.createVerticalGlue()); 
            historyContainer.add(emptyLabel);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            historyContainer.add(Box.createVerticalGlue()); 
            historyContainer.setBorder(new EmptyBorder(100, 0, 100, 0));
        } else {
            // Tambahkan setiap sesi ke kontainer
            for (Object[] session : historyData) {
                JPanel itemPanel = createHistoryItem(session);
                historyContainer.add(itemPanel);
                historyContainer.add(Box.createVerticalStrut(10)); 
            }
        }

        JScrollPane scrollPane = new JScrollPane(historyContainer);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return scrollPane;
    }

    // ================== BUAT ITEM RIWAYAT ==================

    private JPanel createHistoryItem(Object[] session) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setOpaque(true);
        panel.setBackground(Theme.BACKGROUND_TRANSLUCENT); 
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.PROGRESS_BAR_FG, 1), 
            new EmptyBorder(10, 15, 10, 15)
        ));
        panel.setMaximumSize(new Dimension(600, 80)); 
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // --- 1. Sisi Kiri (Nama Sesi & Tanggal) ---
        JPanel leftPanel = new JPanel(new GridLayout(2, 1));
        leftPanel.setOpaque(false);
        
        String sessionName = (String) session[0]; 
        JLabel lblName = new JLabel(sessionName);
        lblName.setFont(Theme.FONT_BODYBOLD.deriveFont(20f)); // Font lebih besar
        lblName.setForeground(Theme.TEXT_WHITE);
        leftPanel.add(lblName);
        
        Timestamp timestamp = (Timestamp) session[4]; 
        String dateText = "Date N/A";
        if (timestamp != null) {
            dateText = LocalDateTime.ofInstant(timestamp.toInstant(), java.time.ZoneId.systemDefault())
                                     .format(DATE_FORMAT);
        }
        JLabel lblDate = new JLabel(dateText);
        lblDate.setFont(Theme.FONT_CAPTION); 
        lblDate.setForeground(Theme.TEXT_WHITE.darker()); 
        leftPanel.add(lblDate);
        
        panel.add(leftPanel, BorderLayout.WEST);

        // --- 2. Sisi Kanan (Detail Durasi) ---
        int workMin = (Integer) session[1];
        int sbMin = (Integer) session[2];
        int lbMin = (Integer) session[3];

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        rightPanel.add(createDurationItem(workMin, "Work", Theme.PROGRESS_BAR_FG)); 
        rightPanel.add(createDurationItem(sbMin, "S.Break", new Color(135, 206, 235))); // Light Blue
        rightPanel.add(createDurationItem(lbMin, "L.Break", new Color(255, 165, 0))); // Orange

        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }

    // Helper untuk membuat label durasi yang kecil
    private JPanel createDurationItem(int minutes, String label, Color color) {
        JPanel item = new JPanel();
        item.setOpaque(false);
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));

        JLabel lblValue = new JLabel(minutes + "m");
        lblValue.setFont(Theme.FONT_BODYBOLD.deriveFont(18f)); 
        lblValue.setForeground(color); 
        lblValue.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(Theme.FONT_CAPTION); 
        lblLabel.setForeground(Theme.TEXT_WHITE.darker());
        lblLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        item.add(lblValue);
        item.add(lblLabel);
        return item;
    }
    
    // Helper untuk membuat Image Button baru
    private JButton createImageButton(String path, int size) {
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        JButton btn = new JButton(new ImageIcon(img));
        
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0,0,0,0)); 
        return btn;
    }

    // Helper untuk mengubah Icon pada button yg sudah ada
    private void updateButtonIcon(JButton btn, String path, int size) {
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        btn.setIcon(new ImageIcon(img));
        btn.repaint();
    }

    // ================== PAINT BACKGROUND ==================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (backgroundImage != null) {
            // Gambar Background Image jika ada
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fallback Gradient Background (JIKA TIDAK ADA background image)
            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(
                0, 0, new Color(22, 33, 62), 
                0, getHeight(), new Color(15, 52, 96));
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}