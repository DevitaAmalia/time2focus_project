import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class MainMenuPanel extends JPanel {

    // --- DEPENDENCIES ---
    private PomodoroController controller;
    private int userId;
    private KoneksiDatabase db;
    private JFrame parentFrame;

    // --- DATA DARI DB ---
    private Image backgroundImage;
    private String musicPath;
    
    // --- KOMPONEN UI ---
    private CardLayout timerCardLayout; 
    private JPanel timerContainer;      
    private JLabel lblSessionTitle;
    private JLabel lblPhase;
    private JLabel lblTimer;
    private JLabel lblCounter;
    private int workDuration = 25;
    private int shortBreakDuration = 5;
    private int longBreakDuration = 15;
    
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
    public MainMenuPanel(int userId, KoneksiDatabase db, JFrame parentFrame) {
        this.userId = userId;
        this.db = db;
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout());
        
        // 1. LOAD DATA DARI DATABASE
        loadUserData();

        // 2. SETUP SIDEBAR (KIRI)
        JPanel sidebar = new JPanel();
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(30, 20, 0, 0));

        JButton btnSetting = createImageButton(PATH_ICON_SETTING, 40);
        btnSetting.addActionListener(e -> openSettingsPanel());
        sidebar.add(btnSetting);
        sidebar.add(Box.createVerticalStrut(20));
        JButton btnHistory = createImageButton(PATH_ICON_HISTORY, 40);
        btnHistory.addActionListener(e -> openHistoryPanel());
        sidebar.add(btnHistory);
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
        centerPanel.add(createPhaseIndicator(), setGbc(gbc, 1, 8, 14));

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
        timerWrapper.setPreferredSize(new Dimension(360, 180));
        timerWrapper.setBorder(new EmptyBorder(14, 14, 14, 14));
        timerWrapper.setLayout(new BorderLayout());

        // Label Phase di atas timer
        lblPhase = new JLabel("-");
        lblPhase.setFont(Theme.FONT_BODYBOLD);            
        lblPhase.setForeground(Theme.TEXT_WHITE);
        lblPhase.setHorizontalAlignment(SwingConstants.CENTER);
        lblPhase.setBorder(new EmptyBorder(0, 0, 0, 0));
        timerWrapper.add(lblPhase, BorderLayout.NORTH);
        
        // Card Timer
        JPanel cardTimer = new JPanel(new GridBagLayout());
        cardTimer.setOpaque(false);
        lblTimer = new JLabel("25:00"); 
        lblTimer.setFont(Theme.FONT_TIMER_BIG); 
        lblTimer.setForeground(Theme.TEXT_WHITE);
        cardTimer.add(lblTimer);

        timerContainer.add(cardTimer, "TIMER");
        timerWrapper.add(timerContainer, BorderLayout.CENTER);
        centerPanel.add(timerWrapper, setGbc(gbc, 2, 0, 0));

        // D. Counter
        lblCounter = new JLabel("total work(s) finished: 0");
        lblCounter.setFont(Theme.FONT_CAPTION); 
        lblCounter.setForeground(Theme.TEXT_WHITE);
        centerPanel.add(lblCounter, setGbc(gbc, 3, 15, 5));

        // E. Kontrol Tombol (Restart, Start, Pause/Resume)
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
        
        centerPanel.add(controlPanel, setGbc(gbc, 4, 25, 0));

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

        Object work = settings.get("work_duration");
        Object sb = settings.get("sb_duration");
        Object lb = settings.get("lb_duration");
        if (work instanceof Integer) workDuration = (Integer) work;
        if (sb instanceof Integer) shortBreakDuration = (Integer) sb;
        if (lb instanceof Integer) longBreakDuration = (Integer) lb;
    }

    // --- LOGIC MUSIK (TOGGLE) ---
    private void toggleMusic() {
        AudioPlayer player = AudioPlayer.getInstance();
        if (player.isMuted()) {
            player.unmute();
            updateButtonIcon(btnMusic, PATH_ICON_MUSIC_ON, 40);
        } else {
            player.mute();
            updateButtonIcon(btnMusic, PATH_ICON_MUSIC_OFF, 40);
        }
    }
    
    private void playBackgroundMusic() {
        if (musicPath != null && !musicPath.isEmpty()) {
            AudioPlayer.getInstance().play(musicPath);
            updateButtonIcon(btnMusic, !AudioPlayer.getInstance().isMuted() ? PATH_ICON_MUSIC_ON : PATH_ICON_MUSIC_OFF, 40);
        }
    }

    private void openHistoryPanel() {
        if (parentFrame == null) return;

        HistoryPanel historyPanel = new HistoryPanel(userId, db);
        parentFrame.getContentPane().removeAll();
        parentFrame.setContentPane(historyPanel);
        parentFrame.revalidate();
        parentFrame.repaint();
    }

    private void openSettingsPanel() {
        if (parentFrame == null) return;

        SettingsPanel settingsPanel = new SettingsPanel(userId, db, parentFrame);
        parentFrame.getContentPane().removeAll();
        parentFrame.setContentPane(settingsPanel);
        parentFrame.revalidate();
        parentFrame.repaint();
    }

    private void showSessionInputDialog() {
        if (parentFrame == null) return;

        JDialog dialog = new JDialog(parentFrame, "New Session", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel container = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(35, 35, 35, 230));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 32, 32);
            }
        };
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(18, 22, 18, 22));
        container.setPreferredSize(new Dimension(520, 280));

        // Header dengan tombol close (X)
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JButton btnClose = new JButton("X");
        btnClose.setFont(Theme.FONT_TITLE.deriveFont(20f));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dialog.dispose());
        header.add(btnClose, BorderLayout.EAST);
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(header);

        container.add(Box.createVerticalStrut(10));

        JLabel title = new JLabel("what do you want to do?");
        title.setFont(Theme.FONT_TITLE.deriveFont(28f));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(title);

        container.add(Box.createVerticalStrut(24));

        // Input field
        JTextField txtSession = new JTextField();
        txtSession.setFont(Theme.FONT_BODY.deriveFont(18f));
        txtSession.setForeground(Color.WHITE);
        txtSession.setOpaque(true);
        txtSession.setBackground(new Color(25, 25, 25));
        txtSession.setHorizontalAlignment(JTextField.CENTER);
        txtSession.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        txtSession.setCaretColor(Color.WHITE);
        txtSession.setSelectionColor(new Color(255, 255, 255, 80));
        txtSession.setSelectedTextColor(Color.WHITE);
        txtSession.setMaximumSize(new Dimension(400, 46));
        txtSession.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(txtSession);
        container.add(Box.createVerticalStrut(28));

        // Tombol save
        ButtonDefault btnSave = new ButtonDefault("save");
        btnSave.setFont(Theme.FONT_BUTTON.deriveFont(14f));
        btnSave.setPreferredSize(new Dimension(110, 42));
        btnSave.setMaximumSize(new Dimension(130, 42));
        
        btnSave.addActionListener(e -> {
            startTheTimer(txtSession.getText());
            dialog.dispose();
        });
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(btnSave);
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(footer);

        dialog.setContentPane(container);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        SwingUtilities.invokeLater(txtSession::requestFocusInWindow);
        dialog.setVisible(true);
    }

    // --- LOGIC TIMER (PAUSE / RESUME / RESTART) ---
    
    // Toggle Pause/Resume
    private void handlePauseResume() {
        if (!isSessionActive || controller == null) return; // Tombol tidak aktif jika sesi belum mulai

        isPaused = !isPaused;

        if (isPaused) {
            System.out.println("Timer Paused");
            updateButtonIcon(btnPauseResume, PATH_ICON_RESUME, 50);
            controller.pauseCurrentPhase();
        } else {
            System.out.println("Timer Resumed");
            updateButtonIcon(btnPauseResume, PATH_ICON_PAUSE, 50);
            controller.resumeCurrentPhase();
        }
    }

    // Restart Logic
    private void handleRestart() {
        if (!isSessionActive || controller == null) return;
        
        System.out.println("Timer Restarted");
        // Reset timer ke awal
        controller.resetCurrentPhase();
        
        // Setelah restart, tampilkan ikon Resume agar user bisa melanjutkan
        isPaused = true;
        updateButtonIcon(btnPauseResume, PATH_ICON_RESUME, 50);
    }
    
    // Start / End Logic
    private void handleStartButton() {
        if (!isSessionActive) {
            showSessionInputDialog();
        } else {
            // Akhiri Sesi
            endSession();
        }
    }

    private void startTheTimer(String namaSesi) {
        if (namaSesi.trim().isEmpty()) namaSesi = "session";
        
        lblSessionTitle.setText(namaSesi);

         if (controller != null) {
            controller.startNewSession(namaSesi);
        }
        
        // UI Updates saat Timer Mulai
        btnStart.setText("end");
        isSessionActive = true;
        isPaused = false;
        
        // Pastikan icon Pause aktif (siap untuk di-pause)
        updateButtonIcon(btnPauseResume, PATH_ICON_PAUSE, 50);
        
        System.out.println("Session Started: " + namaSesi);
    }
    
    private void endSession() {
        if (controller != null) {
            controller.endSession();   // simpan history ke DB, matikan timer
        }

        btnStart.setText("start");
        isSessionActive = false;
        isPaused = false;
        
        lblSessionTitle.setText("session ended");
        
        // Reset icon pause ke default
        updateButtonIcon(btnPauseResume, PATH_ICON_PAUSE, 50);
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

    private JPanel createPhaseIndicator() {
        JPanel container = new JPanel(new GridLayout(1, 3, 16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                super.paintComponent(g);
            }
        };
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(4, 14, 4, 14));

        container.add(buildPhaseItem(workDuration + " min", "work"));
        container.add(buildPhaseItem(shortBreakDuration + " min", "short break"));
        container.add(buildPhaseItem(longBreakDuration + " min", "long break"));

        return container;
    }

    private JPanel buildPhaseItem(String top, String bottom) {
        JPanel item = new JPanel();
        item.setOpaque(false);
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setBorder(new EmptyBorder(0, 8, 0, 8));

        JLabel l1 = new JLabel(top);
        l1.setFont(Theme.FONT_BODY.deriveFont(16f));
        l1.setForeground(Color.BLACK);
        l1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel l2 = new JLabel(bottom);
        l2.setFont(Theme.FONT_CAPTION.deriveFont(14f));
        l2.setForeground(Color.BLACK);
        l2.setAlignmentX(Component.CENTER_ALIGNMENT);

        item.add(l1);
        item.add(l2);
        return item;
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

    public void setController(PomodoroController controller) {
        this.controller = controller;
    }

    public void setPauseState(boolean paused) {
        this.isPaused = paused;
        updateButtonIcon(btnPauseResume, paused ? PATH_ICON_RESUME : PATH_ICON_PAUSE, 50);
    }

    // misal labelnya bernama timerLabel dan phaseLabel:
    public void updateTimerLabel(String timeText) {
        lblTimer.setText(timeText);
    }

    public void updatePhaseLabel(String phaseName) {
        lblPhase.setText(phaseName);
    }

    public void updateWorkCounter(int count) {
        lblCounter.setText("total work(s) finished: " + count);
    }
}
