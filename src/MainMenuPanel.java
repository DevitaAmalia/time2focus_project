import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class MainMenuPanel extends JPanel {

    // --- DEPENDENCIES ---
    private int userId;
    private KoneksiDatabase db;

    // --- DATA DARI DB ---
    private Image backgroundImage;
    private String musicPath;
    
    // --- KOMPONEN UI ---
    private CardLayout timerCardLayout; 
    private JPanel timerContainer;      
    private JTextField sessionInput;
    private JLabel lblSessionTitle;
    private JLabel lblTimer;
    private JProgressBar progressBar;
    
    // Tombol-tombol
    private ButtonDefault btnStart;
    private JButton btnMusic;
    private JButton btnPauseResume; // Tombol Toggle Pause/Resume
    private JButton btnRestart;     // Tombol Restart
    
    // State
    private boolean isSessionActive = false;
    private boolean isPaused = false; // State untuk melacak pause

    // --- PATH ASSETS ---
    // Sidebar & Music
    private final String PATH_ICON_SETTING = "assets/button/SettingButton.png";
    private final String PATH_ICON_HISTORY = "assets/button/HistoryButton.png";
    private final String PATH_ICON_MUSIC_ON = "assets/button/SongButton.png";
    private final String PATH_ICON_MUSIC_OFF = "assets/button/MuteButton.png";
    
    // Timer Controls
    private final String PATH_ICON_PAUSE = "assets/button/PauseButton.png";
    private final String PATH_ICON_RESUME = "assets/button/ResumeButton.png";
    private final String PATH_ICON_RESTART = "assets/button/RestartButton.png";

    // Constructor
    public MainMenuPanel(int userId, KoneksiDatabase db) {
        this.userId = userId;
        this.db = db;

        setLayout(new BorderLayout());
        
        // 1. LOAD DATA DARI DATABASE
        loadUserData();

        // 2. SETUP SIDEBAR (KIRI)
        JPanel sidebar = new JPanel();
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(30, 20, 0, 0));

        sidebar.add(createImageButton(PATH_ICON_SETTING, 50));
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(createImageButton(PATH_ICON_HISTORY, 50));
        sidebar.add(Box.createVerticalStrut(20));
        
        // Tombol Music (Toggle)
        btnMusic = createImageButton(AudioPlayer.getInstance().isMuted() ? PATH_ICON_MUSIC_OFF : PATH_ICON_MUSIC_ON, 40);
        btnMusic.addActionListener(e -> toggleMusic());
        sidebar.add(btnMusic);

        add(sidebar, BorderLayout.WEST);

        // 3. SETUP CENTER CONTENT (TENGAH)
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;

        // A. Judul
        lblSessionTitle = new JLabel("working till sunrise");
        lblSessionTitle.setFont(Theme.FONT_TITLE); 
        lblSessionTitle.setForeground(Theme.TEXT_WHITE);
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 10, 0);
        centerPanel.add(lblSessionTitle, gbc);

        // B. Indikator Fase
        centerPanel.add(createPhaseIndicator(), setGbc(gbc, 1, 10, 20));

        // C. Timer Container
        timerCardLayout = new CardLayout();
        timerContainer = new JPanel(timerCardLayout);
        timerContainer.setOpaque(false); 
        
        // Wrapper Background Hitam
        JPanel timerWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BACKGROUND_TRANSLUCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
            }
        };
        timerWrapper.setOpaque(false);
        timerWrapper.setPreferredSize(new Dimension(500, 250));
        timerWrapper.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Card Timer
        JPanel cardTimer = new JPanel(new GridBagLayout());
        cardTimer.setOpaque(false);
        lblTimer = new JLabel("25:00"); 
        lblTimer.setFont(Theme.FONT_TIMER_BIG); 
        lblTimer.setForeground(Theme.TEXT_WHITE);
        cardTimer.add(lblTimer);

        // Card Input
        JPanel cardInput = createInputPanel();

        timerContainer.add(cardTimer, "TIMER");
        timerContainer.add(cardInput, "INPUT");
        timerWrapper.add(timerContainer);
        centerPanel.add(timerWrapper, setGbc(gbc, 2, 0, 0));

        // D. Counter
        JLabel lblCounter = new JLabel("total work(s) finished: 0");
        lblCounter.setFont(Theme.FONT_CAPTION); 
        lblCounter.setForeground(Theme.TEXT_WHITE);
        centerPanel.add(lblCounter, setGbc(gbc, 3, 15, 5));

        // E. Progress Bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(450, 4));
        progressBar.setForeground(Theme.PROGRESS_BAR_FG);
        progressBar.setBackground(Theme.PROGRESS_BAR_BG);
        progressBar.setBorderPainted(false);
        centerPanel.add(progressBar, setGbc(gbc, 4, 0, 0));

        // F. Kontrol Tombol (Restart, Start, Pause/Resume)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        controlPanel.setOpaque(false);

        // 1. Tombol Restart
        btnRestart = createImageButton(PATH_ICON_RESTART, 50); // Ukuran 50 biar seragam
        btnRestart.addActionListener(e -> handleRestart());

        // 2. Tombol Pause/Resume (Toggle)
        // Default awal icon Pause (asumsi timer langsung jalan)
        btnPauseResume = createImageButton(PATH_ICON_PAUSE, 50);
        btnPauseResume.addActionListener(e -> handlePauseResume());

        // 3. Tombol Start (ButtonDefault)
        btnStart = new ButtonDefault("start");
        btnStart.setPreferredSize(new Dimension(140, 50)); 
        btnStart.addActionListener(e -> handleStartButton());

        // Urutan layout
        controlPanel.add(btnRestart);
        controlPanel.add(btnStart);
        controlPanel.add(btnPauseResume);
        
        centerPanel.add(controlPanel, setGbc(gbc, 5, 25, 0));

        add(centerPanel, BorderLayout.CENTER);
        
        // 4. MAINKAN MUSIK DARI DATABASE
        playBackgroundMusic();
    }

    // --- LOAD DATA DARI DB ---
    private void loadUserData() {
        Map<String, Object> settings = db.getUserSettings(userId);
        
        String bgPath = (String) settings.get("path_bg");
        if (bgPath != null) {
            this.backgroundImage = new ImageIcon(bgPath).getImage();
        }
        
        this.musicPath = (String) settings.get("path_music");
    }

    // --- LOGIC MUSIK (TOGGLE) ---
    private void toggleMusic() {
        AudioPlayer player = AudioPlayer.getInstance();
        if (player.isMuted()) {
            player.unmute();
            updateButtonIcon(btnMusic, PATH_ICON_MUSIC_ON, 50);
        } else {
            player.mute();
            updateButtonIcon(btnMusic, PATH_ICON_MUSIC_OFF, 50);
        }
    }
    
    private void playBackgroundMusic() {
        if (musicPath != null && !musicPath.isEmpty()) {
            AudioPlayer.getInstance().play(musicPath);
            updateButtonIcon(btnMusic, !AudioPlayer.getInstance().isMuted() ? PATH_ICON_MUSIC_ON : PATH_ICON_MUSIC_OFF, 40);
        }
    }

    // --- LOGIC TIMER (PAUSE / RESUME / RESTART) ---
    
    // Toggle Pause/Resume
    private void handlePauseResume() {
        if (!isSessionActive) return; // Tombol tidak aktif jika sesi belum mulai

        isPaused = !isPaused;

        if (isPaused) {
            System.out.println("Timer Paused");
            // Ganti Icon jadi RESUME (karena timer sedang berhenti)
            updateButtonIcon(btnPauseResume, PATH_ICON_RESUME, 50);
            
            // TODO: timer.stop();
        } else {
            System.out.println("Timer Resumed");
            // Ganti Icon jadi PAUSE (karena timer sedang jalan)
            updateButtonIcon(btnPauseResume, PATH_ICON_PAUSE, 50);
            
            // TODO: timer.start();
        }
    }

    // Restart Logic
    private void handleRestart() {
        if (!isSessionActive) return;
        
        System.out.println("Timer Restarted");
        // Reset timer ke awal
        // TODO: Reset logika timer backend
        
        // Jika restart, anggap otomatis jalan lagi (unpause)
        isPaused = false;
        updateButtonIcon(btnPauseResume, PATH_ICON_PAUSE, 50);
    }
    
    // Start / End Logic
    private void handleStartButton() {
        if (!isSessionActive) {
            // Buka Input Panel
            timerCardLayout.show(timerContainer, "INPUT");
            SwingUtilities.invokeLater(() -> sessionInput.requestFocusInWindow());
        } else {
            // Akhiri Sesi
            endSession();
        }
    }
    
    private void startTheTimer() {
        String namaSesi = sessionInput.getText();
        if (namaSesi.trim().isEmpty()) namaSesi = "Session";
        
        lblSessionTitle.setText(namaSesi);
        timerCardLayout.show(timerContainer, "TIMER");
        
        // UI Updates saat Timer Mulai
        btnStart.setText("end");
        isSessionActive = true;
        isPaused = false;
        
        // Pastikan icon Pause aktif (siap untuk di-pause)
        updateButtonIcon(btnPauseResume, PATH_ICON_PAUSE, 50);
        
        System.out.println("Session Started: " + namaSesi);
        // TODO: timer.start();
    }
    
    private void endSession() {
        btnStart.setText("start");
        isSessionActive = false;
        isPaused = false;
        
        lblSessionTitle.setText("Session Ended");
        sessionInput.setText("");
        
        // Reset icon pause ke default
        updateButtonIcon(btnPauseResume, PATH_ICON_PAUSE, 50);
        // TODO: timer.stop();
    }

    // --- UTILITIES UI ---
    
    // Helper untuk membuat Image Button baru
    private JButton createImageButton(String path, int size) {
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        JButton btn = new JButton(new ImageIcon(img));
        
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Hilangkan margin agar pas dengan gambar
        btn.setMargin(new Insets(0,0,0,0)); 
        return btn;
    }

    // Helper untuk mengubah Icon pada button yg sudah ada (Toggle logic)
    private void updateButtonIcon(JButton btn, String path, int size) {
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        btn.setIcon(new ImageIcon(img));
        btn.repaint();
    }
    
    private GridBagConstraints setGbc(GridBagConstraints gbc, int y, int top, int bottom) {
        gbc.gridy = y;
        gbc.insets = new Insets(top, 0, bottom, 0);
        return gbc;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        
        JLabel lblQ = new JLabel("what do you want to do?");
        lblQ.setFont(Theme.FONT_BODY); 
        lblQ.setForeground(Theme.TEXT_WHITE);
        g.gridy = 0;
        panel.add(lblQ, g);

        sessionInput = new JTextField(20);
        sessionInput.setFont(Theme.FONT_TITLE); 
        sessionInput.setForeground(Theme.TEXT_WHITE);
        sessionInput.setOpaque(false);
        sessionInput.setHorizontalAlignment(JTextField.CENTER);
        sessionInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        sessionInput.setCaretColor(Color.WHITE);
        
        g.gridy = 1;
        g.insets = new Insets(20, 0, 20, 0);
        panel.add(sessionInput, g);

        ButtonDefault btnSave = new ButtonDefault("save");
        btnSave.setPreferredSize(new Dimension(100, 40));
        btnSave.addActionListener(e -> startTheTimer());
        g.gridy = 2;
        panel.add(btnSave, g);

        return panel;
    }
    
    private JPanel createPhaseIndicator() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        
        JLabel l1 = new JLabel("25 min work");
        JLabel l2 = new JLabel("5 min short break");
        JLabel l3 = new JLabel("15 min long break");
        l1.setFont(Theme.FONT_CAPTION); l2.setFont(Theme.FONT_CAPTION); l3.setFont(Theme.FONT_CAPTION);
        l1.setForeground(Color.BLACK); l2.setForeground(Color.BLACK); l3.setForeground(Color.BLACK);
        p.add(l1); p.add(l2); p.add(l3);
        return p;
    }

    // --- PAINT COMPONENT (Background) ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(
                0, 0, new Color(22, 33, 62), 
                0, getHeight(), new Color(15, 52, 96));
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
