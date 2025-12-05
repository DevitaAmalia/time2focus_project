import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends JPanel {
    private KoneksiDatabase db;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JFrame parentFrame;
    private Image backgroundImage;
    private JPanel formContainer;
    
    public LoginPanel(KoneksiDatabase db, JFrame parentFrame) {
        this.db = db;
        this.parentFrame = parentFrame;
        loadBackground();
        initComponents();
        setupResizeListener();
    }
    
    private void loadBackground() {
        try {
            ImageIcon icon = new ImageIcon("assets/background/city_scrapper.png");
            backgroundImage = icon.getImage();
        } catch (Exception e) {
            System.err.println("Background image not found, using solid color");
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(20, 40, 70),
                0, getHeight(), new Color(40, 60, 90)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    
    private void initComponents() {
        setLayout(null);
        
        formContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(Theme.BACKGROUND_TRANSLUCENT);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        formContainer.setOpaque(false);
        formContainer.setLayout(null);
        formContainer.setSize(400, 400);
        
        // Title
        JLabel titleLabel = new JLabel("time2focus", SwingConstants.CENTER);
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(0, 10, 400, 60);
        
        // Username label
        JLabel userLabel = new JLabel("username:");
        userLabel.setFont(Theme.FONT_BODYBOLD);
        userLabel.setForeground(Color.WHITE);
        userLabel.setBounds(60, 90, 100, 25);
        
        // Username field
        usernameField = createStyledTextField();
        usernameField.setBounds(60, 120, 280, 45);
        
        // Password label
        JLabel passLabel = new JLabel("password:");
        passLabel.setFont(Theme.FONT_BODYBOLD);
        passLabel.setForeground(Color.WHITE);
        passLabel.setBounds(60, 180, 100, 25);
        
        // Password field
        passwordField = createStyledPasswordField();
        passwordField.setBounds(60, 210, 280, 45);
        
        // Login button
        JButton loginBtn = new ButtonDefault("login");
        loginBtn.setBounds(100, 275, 200, 40);
        loginBtn.addActionListener(e -> handleLogin());
        
        // Register button
        JButton registerBtn = new ButtonDefault("register");
        registerBtn.setBounds(100, 330, 200, 40);
        registerBtn.addActionListener(e -> handleRegister());
        
        // Add all components to form container
        formContainer.add(titleLabel);
        formContainer.add(userLabel);
        formContainer.add(usernameField);
        formContainer.add(passLabel);
        formContainer.add(passwordField);
        formContainer.add(loginBtn);
        formContainer.add(registerBtn);
        
        // Add form container to main panel
        add(formContainer);
        
        // Initial centering
        centerFormContainer();
    }
    
    private void setupResizeListener() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                centerFormContainer();
            }
        });
    }
    
    private void centerFormContainer() {
        if (formContainer != null) {
            int x = (getWidth() - formContainer.getWidth()) / 2;
            int y = (getHeight() - formContainer.getHeight()) / 2;
            formContainer.setLocation(x, y);
        }
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                
                super.paintComponent(g);
            }
            
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(200, 200, 200, 100));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);
            }
        };
        
        field.setOpaque(false);
        field.setBackground(Color.WHITE);
        field.setFont(Theme.FONT_CAPTION);
        field.setForeground(new Color(40, 40, 40));
        field.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        return field;
    }
    
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                
                super.paintComponent(g);
            }
            
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);
            }
        };
        
        field.setOpaque(false);
        field.setBackground(Color.WHITE);
        field.setFont(Theme.FONT_CAPTION);
        field.setForeground(new Color(40, 40, 40));
        field.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        return field;
    }
    
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            showStyledMessage("Username dan password harus diisi!", "Peringatan");
            return;
        }
        
        int userId = db.loginUser(username, password);
        if (userId != -1) {
            openMainMenu(userId);
        } else {
            showStyledMessage("Login gagal! Periksa username dan password.", "Error");
        }
    }
    
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            showStyledMessage("Username dan password harus diisi!", "Peringatan");
            return;
        }
        
        if (db.registerUser(username, password)) {
            showStyledMessage("Registrasi berhasil! Silakan login.", "Sukses");
            usernameField.setText("");
            passwordField.setText("");
        } else {
            showStyledMessage("Registrasi gagal! Username mungkin sudah digunakan.", "Error");
        }
    }
    
    private void showStyledMessage(String message, String title) {
        UIManager.put("OptionPane.background", new Color(240, 240, 245));
        UIManager.put("Panel.background", new Color(240, 240, 245));
        UIManager.put("OptionPane.messageForeground", new Color(40, 40, 40));
        
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openMainMenu(int userId) {
        // 1. Buat MainMenuPanel untuk user ini
        MainMenuPanel mainMenu = new MainMenuPanel(userId, db, parentFrame);

        // 2. Buat PomodoroController dan sambungkan dengan mainMenu
        PomodoroController controller = new PomodoroController(mainMenu, db, userId);

        // 3. Ganti isi frame menjadi mainMenu
        parentFrame.getContentPane().removeAll();
        parentFrame.setContentPane(mainMenu);   
        parentFrame.revalidate();
        parentFrame.repaint();
    }

}
