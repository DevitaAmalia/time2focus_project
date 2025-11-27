import java.awt.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicSpinnerUI;

public class SettingsPanel extends JPanel {
    
    private KoneksiDatabase db;
    private int userId;
    private JFrame parentFrame;
    private Image backgroundImage;
    private String username;
    
    // Components
    private JSpinner workSpinner;
    private JSpinner sbSpinner;
    private JSpinner lbSpinner;
    private JSpinner cycleSpinner;
    private JComboBox<BackgroundItem> bgComboBox;
    private JComboBox<MusicItem> musicComboBox;
    private JButton btnBack;
    private JButton btnLogout;
    private ButtonDefault btnSave;
    
    // Current settings
    private int currentWorkDuration = 25;
    private int currentSbDuration = 5;
    private int currentLbDuration = 15;
    private int currentBgId = 1;
    private int currentMusicId = 1;
    
    public SettingsPanel(int userId, KoneksiDatabase db, JFrame parentFrame) {
        this.userId = userId;
        this.db = db;
        this.parentFrame = parentFrame;
        
        loadCurrentSettings();
        loadUsername();
        initComponents();
    }
    
    private void loadCurrentSettings() {
        Map<String, Object> settings = db.getUserSettings(userId);
        
        if (!settings.isEmpty()) {
            currentWorkDuration = (Integer) settings.getOrDefault("work_duration", 25);
            currentSbDuration = (Integer) settings.getOrDefault("sb_duration", 5);
            currentLbDuration = (Integer) settings.getOrDefault("lb_duration", 15);
            currentBgId = (Integer) settings.getOrDefault("id_bg", 1);
            currentMusicId = (Integer) settings.getOrDefault("id_music", 1);
            
            String bgPath = (String) settings.get("path_bg");
            if (bgPath != null) {
                backgroundImage = new ImageIcon(bgPath).getImage();
            }
        }
    }
    
    private void loadUsername() {
        String sql = "SELECT username FROM users WHERE id_user = ?";
        
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    username = rs.getString("username");
                } else {
                    username = "username";
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading username: " + e.getMessage());
            username = "username";
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(
                0, 0, new Color(22, 33, 62),
                0, getHeight(), new Color(15, 52, 96)
            );
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    
    private void initComponents() {
        setLayout(null); 
        
        // Back button (outside panel, top-left)
        btnBack = createImageButton("assets/button/BackButton.png", 50);
        btnBack.setBounds(30, 30, 50, 50);
        btnBack.addActionListener(e -> handleBack());
        add(btnBack);
        
        // Create centered container panel
        JPanel containerPanel = createContainerPanel();
        
        // Center the container
        int panelWidth = 540;
        int panelHeight = 600;
        
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int x = (getWidth() - panelWidth) / 2;
                int y = (getHeight() - panelHeight) / 2;
                containerPanel.setBounds(x, y, panelWidth, panelHeight);
            }
        });
        
        add(containerPanel);
    }
    
    private JPanel createContainerPanel() {
        JPanel container = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(0, 0, 0, 160));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };
        container.setOpaque(false);
        container.setLayout(new GridBagLayout());
        container.setSize(540, 600);
        container.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 5, 0);
        container.add(createHeaderPanel(), gbc);
        
        // Title
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 2, 0);
        JLabel titleLabel = new JLabel("hello, " + username);
        titleLabel.setFont(Theme.FONT_TITLE.deriveFont(28f));
        titleLabel.setForeground(Color.WHITE);
        container.add(titleLabel, gbc);
        
        // Subtitle
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        JLabel subtitleLabel = new JLabel("let's set up the timer to your liking");
        subtitleLabel.setFont(Theme.FONT_BODY.deriveFont(14f));
        subtitleLabel.setForeground(new Color(200, 200, 200));
        container.add(subtitleLabel, gbc);
        
        // Session section
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 15, 0);
        container.add(createSessionSection(), gbc);
        
        // Long break every section
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 15, 0);
        container.add(createCycleSection(), gbc);
        
        // Theme section
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 12, 0);
        container.add(createThemeSection(), gbc);
        
        // Sound section
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 20, 0);
        container.add(createSoundSection(), gbc);
        
        // Save button
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        container.add(createSaveButton(), gbc);
        
        return container;
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        btnLogout = createImageButton("assets/button/LogoutButton.png", 40);
        btnLogout.addActionListener(e -> handleLogout());
        header.add(btnLogout, BorderLayout.EAST);
        
        return header;
    }
    
    private JButton createImageButton(String path, int size) {
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        JButton btn = new JButton(new ImageIcon(img));
        
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0, 0, 0, 0));
        
        return btn;
    }
    
    private JPanel createSessionSection() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 8));
        mainPanel.setOpaque(false);
        
        // Section title
        JLabel titleLabel = new JLabel("session");
        titleLabel.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(Color.WHITE);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Timer boxes
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setOpaque(false);
        
        panel.add(createTimerBox("work", currentWorkDuration, "work"));
        panel.add(createTimerBox("short break", currentSbDuration, "sb"));
        panel.add(createTimerBox("long break", currentLbDuration, "lb"));
        
        mainPanel.add(panel, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createTimerBox(String label, int value, String type) {
        // Main container untuk label + box + minutes
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        
        JLabel topLabel = new JLabel(label);
        topLabel.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD, 13f));
        topLabel.setForeground(Color.WHITE);
        topLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(topLabel);
        container.add(Box.createVerticalStrut(5));
        
        // Box untuk angka
        JPanel box = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2d.setColor(new Color(255, 255, 255, 10));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Border
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);
            }
        };
        box.setOpaque(false);
        box.setLayout(new GridBagLayout());
        box.setPreferredSize(new Dimension(140, 55));
        box.setMaximumSize(new Dimension(140, 55));
        box.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Value spinner
        SpinnerNumberModel model = new SpinnerNumberModel(value, 1, 120, 1);
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD, 22f));
        
        // Store reference
        if (type.equals("work")) {
            workSpinner = spinner;
        } else if (type.equals("sb")) {
            sbSpinner = spinner;
        } else if (type.equals("lb")) {
            lbSpinner = spinner;
        }
        
        // Style spinner dengan UI custom untuk transparent arrows
        styleTransparentSpinner(spinner);
        
        box.add(spinner);
        container.add(box);
        
        container.add(Box.createVerticalStrut(5));
        JLabel minutesLabel = new JLabel("minutes");
        minutesLabel.setFont(Theme.FONT_CAPTION.deriveFont(11f));
        minutesLabel.setForeground(new Color(180, 180, 180));
        minutesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(minutesLabel);
        
        return container;
    }
    
    private void styleTransparentSpinner(JSpinner spinner) {
        spinner.setUI(new BasicSpinnerUI() {
            protected Component createNextButton() {
                JButton button = (JButton) super.createNextButton();
                button.setOpaque(false);
                button.setContentAreaFilled(false);
                button.setBorderPainted(false);
                button.setForeground(Color.WHITE);
                return button;
            }
            
            protected Component createPreviousButton() {
                JButton button = (JButton) super.createPreviousButton();
                button.setOpaque(false);
                button.setContentAreaFilled(false);
                button.setBorderPainted(false);
                button.setForeground(Color.WHITE);
                return button;
            }
        });
        
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setHorizontalAlignment(JTextField.CENTER);
            textField.setForeground(Color.WHITE);
            textField.setBackground(new Color(0, 0, 0, 0));
            textField.setBorder(null);
            textField.setCaretColor(Color.WHITE);
        }
        
        spinner.setOpaque(false);
        spinner.setBorder(null);
    }
    
    private JPanel createCycleSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        
        JLabel label = new JLabel("long break every");
        label.setFont(Theme.FONT_BODY.deriveFont(14f));
        label.setForeground(Color.WHITE);
        panel.add(label);
        
        // Cycle spinner with custom styling
        SpinnerNumberModel model = new SpinnerNumberModel(4, 1, 10, 1);
        cycleSpinner = new JSpinner(model);
        cycleSpinner.setPreferredSize(new Dimension(55, 30));
        cycleSpinner.setFont(Theme.FONT_BODY.deriveFont(14f));
        
        // Style dengan transparent arrows
        cycleSpinner.setUI(new BasicSpinnerUI() {
            protected Component createNextButton() {
                JButton button = (JButton) super.createNextButton();
                button.setOpaque(false);
                button.setContentAreaFilled(false);
                button.setBorderPainted(false);
                button.setForeground(Color.WHITE);
                return button;
            }
            
            protected Component createPreviousButton() {
                JButton button = (JButton) super.createPreviousButton();
                button.setOpaque(false);
                button.setContentAreaFilled(false);
                button.setBorderPainted(false);
                button.setForeground(Color.WHITE);
                return button;
            }
        });
        
        // Style the spinner
        JComponent editor = cycleSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setHorizontalAlignment(JTextField.CENTER);
            textField.setForeground(Color.WHITE);
            textField.setBackground(new Color(0, 0, 0, 0));
            textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 80), 1),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
            ));
            textField.setCaretColor(Color.WHITE);
        }
        
        cycleSpinner.setOpaque(false);
        panel.add(cycleSpinner);
        
        JLabel worksLabel = new JLabel("works");
        worksLabel.setFont(Theme.FONT_BODY.deriveFont(14f));
        worksLabel.setForeground(Color.WHITE);
        panel.add(worksLabel);
        
        return panel;
    }
    
    private JPanel createThemeSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        
        JLabel label = new JLabel("theme");
        label.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD, 16f));
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.NORTH);
        
        bgComboBox = createStyledComboBox();
        loadBackgrounds();
        panel.add(bgComboBox, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSoundSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        
        JLabel label = new JLabel("sound");
        label.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD, 16f));
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.NORTH);
        
        musicComboBox = createStyledComboBox();
        loadMusics();
        panel.add(musicComboBox, BorderLayout.CENTER);
        
        return panel;
    }
    
    private <T> JComboBox<T> createStyledComboBox() {
        JComboBox<T> combo = new JComboBox<T>() {
            @Override
            public void updateUI() {
                super.updateUI();
                setOpaque(false);
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2d.setColor(new Color(0, 0, 0, 0)); // Transparent
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Border
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);
            }
        };
        
        // Custom UI untuk transparent arrow button
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton() {
                    @Override
                    public void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        
                        // Transparent background
                        g2.setColor(new Color(0, 0, 0, 0));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        
                        // Draw white arrow
                        g2.setColor(Color.WHITE);
                        int w = getWidth();
                        int h = getHeight();
                        int[] xPoints = {w/2 - 4, w/2 + 4, w/2};
                        int[] yPoints = {h/2 - 2, h/2 - 2, h/2 + 3};
                        g2.fillPolygon(xPoints, yPoints, 3);
                    }
                };
                button.setOpaque(false);
                button.setContentAreaFilled(false);
                button.setBorderPainted(false);
                button.setFocusPainted(false);
                return button;
            }
        });
        
        combo.setFont(Theme.FONT_BODY.deriveFont(14f));
        combo.setForeground(Color.WHITE);
        combo.setPreferredSize(new Dimension(460, 38));
        combo.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        combo.setOpaque(false);
        
        // Custom renderer
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                label.setFont(Theme.FONT_BODY.deriveFont(14f));
                
                if (isSelected) {
                    label.setBackground(new Color(50, 60, 80));
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(new Color(25, 35, 55));
                    label.setForeground(Color.WHITE);
                }
                
                return label;
            }
        });
        
        return combo;
    }
    
    private void loadBackgrounds() {
        DefaultComboBoxModel<BackgroundItem> model = new DefaultComboBoxModel<>();
        
        String sql = "SELECT id_bg, nama_bg, path_bg FROM backgrounds ORDER BY id_bg";
        
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("id_bg");
                String nama = rs.getString("nama_bg");
                String path = rs.getString("path_bg");
                model.addElement(new BackgroundItem(id, nama, path));
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading backgrounds: " + e.getMessage());
        }
        
        bgComboBox.setModel(model);
        
        // Set selected
        for (int i = 0; i < bgComboBox.getItemCount(); i++) {
            if (bgComboBox.getItemAt(i).id == currentBgId) {
                bgComboBox.setSelectedIndex(i);
                break;
            }
        }
    }
    
    private void loadMusics() {
        DefaultComboBoxModel<MusicItem> model = new DefaultComboBoxModel<>();
        
        String sql = "SELECT id_music, nama_music, path_music FROM musics ORDER BY id_music";
        
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("id_music");
                String nama = rs.getString("nama_music");
                String path = rs.getString("path_music");
                model.addElement(new MusicItem(id, nama, path));
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading musics: " + e.getMessage());
        }
        
        musicComboBox.setModel(model);
        
        // Set selected
        for (int i = 0; i < musicComboBox.getItemCount(); i++) {
            if (musicComboBox.getItemAt(i).id == currentMusicId) {
                musicComboBox.setSelectedIndex(i);
                break;
            }
        }
    }
    
    private JButton createSaveButton() {
        ButtonDefault btnSave = new ButtonDefault("save");
        btnSave.setPreferredSize(new Dimension(120, 42));
        btnSave.setFont(Theme.FONT_BUTTON.deriveFont(18f));
        btnSave.addActionListener(e -> handleSave());
        
        return btnSave;
    }
    
    private void handleBack() {
        returnToMainMenu();
    }
    
    private void handleLogout() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Logout",
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Stop music
            AudioPlayer.getInstance().stop();
            
            // Return to login panel
            LoginPanel loginPanel = new LoginPanel(db, parentFrame);
            parentFrame.getContentPane().removeAll();
            parentFrame.setContentPane(loginPanel);
            parentFrame.revalidate();
            parentFrame.repaint();
        }
    }
    
    private void handleSave() {
        int workDur = (Integer) workSpinner.getValue();
        int sbDur = (Integer) sbSpinner.getValue();
        int lbDur = (Integer) lbSpinner.getValue();
        
        BackgroundItem selectedBg = (BackgroundItem) bgComboBox.getSelectedItem();
        MusicItem selectedMusic = (MusicItem) musicComboBox.getSelectedItem();
        
        if (selectedBg == null || selectedMusic == null) {
            JOptionPane.showMessageDialog(
                this,
                "Please select both background and music.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        int bgId = selectedBg.id;
        int musicId = selectedMusic.id;
        
        boolean success = db.updateUserSettings(userId, workDur, sbDur, lbDur, bgId, musicId);
        
        if (success) {
            backgroundImage = new ImageIcon(selectedBg.path).getImage();
            repaint();
            
            AudioPlayer.getInstance().stop();
            AudioPlayer.getInstance().play(selectedMusic.path);
            
            JOptionPane.showMessageDialog(
                this,
                "Settings saved successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            returnToMainMenu();
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Failed to save settings. Please try again.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private void returnToMainMenu() {
        MainMenuPanel mainMenu = new MainMenuPanel(userId, db, parentFrame);
        PomodoroController controller = new PomodoroController(mainMenu, db, userId);
        
        parentFrame.getContentPane().removeAll();
        parentFrame.setContentPane(mainMenu);
        parentFrame.revalidate();
        parentFrame.repaint();
    }
    
    // Helper classes
    private static class BackgroundItem {
        int id;
        String name;
        String path;
        
        BackgroundItem(int id, String name, String path) {
            this.id = id;
            this.name = name;
            this.path = path;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    private static class MusicItem {
        int id;
        String name;
        String path;
        
        MusicItem(int id, String name, String path) {
            this.id = id;
            this.name = name;
            this.path = path;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
}