import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class SettingsPanel extends JPanel {
    
    private KoneksiDatabase db;
    private int userId;
    private JFrame parentFrame;
    private Image backgroundImage;
    private String username;
    
    // Components
    private JTextField workField, sbField, lbField, cycleField;
    private JComboBox<BackgroundItem> bgComboBox;
    private JComboBox<MusicItem> musicComboBox;
    
    // Current settings
    private int currentWorkDuration = 25;
    private int currentSbDuration = 5;
    private int currentLbDuration = 15;
    private int currentBgId = 1;
    private int currentMusicId = 1;
    private int currentCycle = 4;
    
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
            currentCycle = (Integer) settings.getOrDefault("cycle", 4);
            String bgPath = (String) settings.get("path_bg");
            if (bgPath != null) backgroundImage = new ImageIcon(bgPath).getImage();
        }
    }
    
    private void loadUsername() {
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT username FROM users WHERE id_user = ?")) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            username = rs.next() ? rs.getString("username") : "username";
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
            g2d.setPaint(new GradientPaint(0, 0, new Color(22, 33, 62), 0, getHeight(), new Color(15, 52, 96)));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(30, 20, 0, 0));
        JButton btnBack = createImageButton("assets/button/BackButton.png", 40);
        btnBack.addActionListener(e -> returnToMainMenu());
        sidebar.add(btnBack);
        add(sidebar, BorderLayout.WEST);
        
        // Main container
        JPanel containerPanel = createContainerPanel();
        containerPanel.setPreferredSize(new Dimension(540, 500));
        containerPanel.setMinimumSize(new Dimension(540, 500));
        containerPanel.setMaximumSize(new Dimension(540, 500));

        JPanel centerWrapper = new JPanel();
        centerWrapper.setOpaque(false);
        centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
        centerWrapper.setBorder(new EmptyBorder(30, 0, 0, 0));
        containerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerWrapper.add(containerPanel);
        add(centerWrapper, BorderLayout.CENTER);
    }
    
    private JPanel createContainerPanel() {
        JPanel container = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 30, 30);
            }
        };
        container.setOpaque(false);
        container.setLayout(new GridBagLayout());
        container.setBorder(new EmptyBorder(10, 4, 10, 4));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        container.add(createTopRow(), gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        JLabel subtitle = new JLabel("let's set up the timer to your liking");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(new Color(200, 200, 200));
        container.add(subtitle, gbc);
        
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        container.add(createSessionSection(), gbc);
        
        gbc.gridy = 3;
        container.add(createCycleSection(), gbc);
        
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 12, 0);
        container.add(createThemeSection(), gbc);
        
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 20, 0);
        container.add(createSoundSection(), gbc);
        
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        ButtonDefault btnSave = new ButtonDefault("save");
        btnSave.setPreferredSize(new Dimension(120, 42));
        btnSave.setFont(Theme.FONT_BUTTON);
        btnSave.addActionListener(e -> handleSave());
        container.add(btnSave, gbc);
        
        return container;
    }
    
    private JPanel createTopRow() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel title = new JLabel("hello, " + username);
        title.setFont(Theme.FONT_TITLE.deriveFont(28f));
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.WEST);
        JButton btnLogout = createImageButton("assets/button/LogoutButton.png", 40);
        btnLogout.addActionListener(e -> handleLogout());
        panel.add(btnLogout, BorderLayout.EAST);
        return panel;
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
        JPanel main = new JPanel(new BorderLayout(0, 8));
        main.setOpaque(false);
        JLabel title = new JLabel("session");
        title.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD, 16f));
        title.setForeground(Color.WHITE);
        main.add(title, BorderLayout.NORTH);
        
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setOpaque(false);
        workField = createTimerField(currentWorkDuration);
        sbField = createTimerField(currentSbDuration);
        lbField = createTimerField(currentLbDuration);
        panel.add(createTimerBox(workField, "work"));
        panel.add(createTimerBox(sbField, "short break"));
        panel.add(createTimerBox(lbField, "long break"));
        main.add(panel, BorderLayout.CENTER);
        return main;
    }
    
    private JTextField createTimerField(int value) {
        JTextField field = new JTextField(String.valueOf(value));
        field.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD, 22f));
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setForeground(Color.WHITE);
        field.setOpaque(false);
        field.setBorder(null);
        field.setCaretColor(Color.WHITE);
        field.setSelectionColor(new Color(255, 255, 255, 80));
        field.setSelectedTextColor(Color.WHITE);
        field.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) && e.getKeyChar() != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
            }
        });
        return field;
    }
    
    private JPanel createTimerBox(JTextField field, String label) {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        
        JLabel topLabel = new JLabel(label);
        topLabel.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD, 13f));
        topLabel.setForeground(Color.WHITE);
        topLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(topLabel);
        container.add(Box.createVerticalStrut(5));
        
        JPanel box = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 10));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
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
        box.add(field);
        container.add(box);
        
        container.add(Box.createVerticalStrut(5));
        JLabel minutes = new JLabel("minutes");
        minutes.setFont(Theme.FONT_CAPTION.deriveFont(11f));
        minutes.setForeground(new Color(180, 180, 180));
        minutes.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(minutes);
        return container;
    }
    
    private JPanel createCycleSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        
        JLabel label = new JLabel("long break every");
        label.setFont(Theme.FONT_BODY.deriveFont(14f));
        label.setForeground(Color.WHITE);
        panel.add(label);
        
        cycleField = new JTextField(String.valueOf(currentCycle));
        cycleField.setPreferredSize(new Dimension(55, 30));
        cycleField.setFont(Theme.FONT_BODY.deriveFont(14f));
        cycleField.setHorizontalAlignment(JTextField.CENTER);
        cycleField.setForeground(Color.WHITE);
        cycleField.setOpaque(false);
        cycleField.setBorder(BorderFactory.createCompoundBorder(
            new javax.swing.border.AbstractBorder() {
                private final int arc = 16;
                @Override
                public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 80));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
                    g2.dispose();
                }
            },
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        cycleField.setCaretColor(Color.WHITE);
        cycleField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) && e.getKeyChar() != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
            }
        });
        panel.add(cycleField);
        
        JLabel works = new JLabel("works");
        works.setFont(Theme.FONT_BODY.deriveFont(14f));
        works.setForeground(Color.WHITE);
        panel.add(works);
        return panel;
    }
    
    private JPanel createThemeSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        JLabel label = new JLabel("background");
        label.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD, 16f));
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.NORTH);
        bgComboBox = createArrowOnlyComboBox();
        loadBackgrounds();
        panel.add(bgComboBox, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createSoundSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        JLabel label = new JLabel("music");
        label.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD, 16f));
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.NORTH);
        musicComboBox = createArrowOnlyComboBox();
        loadMusics();
        panel.add(musicComboBox, BorderLayout.CENTER);
        return panel;
    }
    
    private <T> JComboBox<T> createArrowOnlyComboBox() {
        JComboBox<T> combo = new JComboBox<T>() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Theme.BACKGROUND_TRANSLUCENT);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);
                g2d.dispose();
            }
        };
        
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                return new JButton() {
                    @Override
                    public Dimension getPreferredSize() {
                        return new Dimension(0, 0);
                    }
                    @Override
                    public void setBounds(int x, int y, int width, int height) {
                        super.setBounds(x, y, 0, 0);
                    }
                    @Override
                    public void paintComponent(Graphics g) {
                    }
                };
            }
            
            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(Theme.BACKGROUND_TRANSLUCENT);
                g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
            }
        });
        
        combo.setFont(Theme.FONT_BODY.deriveFont(14f));
        combo.setForeground(Color.WHITE);
        combo.setPreferredSize(new Dimension(460, 38));
        combo.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        combo.setOpaque(false);
        combo.setBackground(Theme.BACKGROUND_TRANSLUCENT);
        combo.setFocusable(false);
        combo.setMaximumRowCount(6);
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        
        combo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) combo.hidePopup();
        });
        
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                label.setFont(Theme.FONT_BODY.deriveFont(14f));
                Color cellBg = new Color(60, 60, 60, 220);
                label.setBackground(cellBg);
                label.setForeground(Color.WHITE);

                list.setBackground(new Color(40, 40, 40, 220));
                list.setSelectionBackground(cellBg);
                list.setSelectionForeground(Color.WHITE);
                
                return label;
            }
        });
        
        return combo;
    }
    
    private void loadBackgrounds() {
        DefaultComboBoxModel<BackgroundItem> model = new DefaultComboBoxModel<>();
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_bg, nama_bg, path_bg FROM backgrounds ORDER BY id_bg")) {
            while (rs.next()) {
                model.addElement(new BackgroundItem(rs.getInt("id_bg"), rs.getString("nama_bg"), rs.getString("path_bg")));
            }
        } catch (SQLException e) {
            System.err.println("Error loading backgrounds: " + e.getMessage());
        }
        bgComboBox.setModel(model);
        for (int i = 0; i < bgComboBox.getItemCount(); i++) {
            if (bgComboBox.getItemAt(i).id == currentBgId) {
                bgComboBox.setSelectedIndex(i);
                break;
            }
        }
    }
    
    private void loadMusics() {
        DefaultComboBoxModel<MusicItem> model = new DefaultComboBoxModel<>();
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_music, nama_music, path_music FROM musics ORDER BY id_music")) {
            while (rs.next()) {
                model.addElement(new MusicItem(rs.getInt("id_music"), rs.getString("nama_music"), rs.getString("path_music")));
            }
        } catch (SQLException e) {
            System.err.println("Error loading musics: " + e.getMessage());
        }
        musicComboBox.setModel(model);
        for (int i = 0; i < musicComboBox.getItemCount(); i++) {
            if (musicComboBox.getItemAt(i).id == currentMusicId) {
                musicComboBox.setSelectedIndex(i);
                break;
            }
        }
    }
    
    private void handleLogout() {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", 
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            AudioPlayer.getInstance().stop();
            LoginPanel loginPanel = new LoginPanel(db, parentFrame);
            parentFrame.getContentPane().removeAll();
            parentFrame.setContentPane(loginPanel);
            parentFrame.revalidate();
            parentFrame.repaint();
        }
    }
    
    private void handleSave() {
        try {
            int workDur = Integer.parseInt(workField.getText().trim());
            int sbDur = Integer.parseInt(sbField.getText().trim());
            int lbDur = Integer.parseInt(lbField.getText().trim());
            int cycle = Integer.parseInt(cycleField.getText().trim());
            
            if (workDur < 1 || workDur > 120 || sbDur < 1 || sbDur > 120 || lbDur < 1 || lbDur > 120) {
                JOptionPane.showMessageDialog(this, "Duration must be between 1 and 120 minutes.", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (cycle < 1 || cycle > 12) {
                JOptionPane.showMessageDialog(this, "Cycle must be between 1 and 12.", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            BackgroundItem bg = (BackgroundItem) bgComboBox.getSelectedItem();
            MusicItem music = (MusicItem) musicComboBox.getSelectedItem();
            
            if (bg == null || music == null) {
                JOptionPane.showMessageDialog(this, "Please select both background and music.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (db.updateUserSettings(userId, workDur, sbDur, lbDur, bg.id, music.id, cycle)) {
                backgroundImage = new ImageIcon(bg.path).getImage();
                repaint();
                AudioPlayer.getInstance().stop();
                AudioPlayer.getInstance().play(music.path);
                JOptionPane.showMessageDialog(this, "Settings saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save settings. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for durations.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void returnToMainMenu() {
        MainMenuPanel mainMenu = new MainMenuPanel(userId, db, parentFrame);
        new PomodoroController(mainMenu, db, userId);
        parentFrame.getContentPane().removeAll();
        parentFrame.setContentPane(mainMenu);
        parentFrame.revalidate();
        parentFrame.repaint();
    }
    
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
