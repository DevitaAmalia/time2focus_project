import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class HistoryPanel extends JPanel {

    // --- DEPENDENCIES ---
    private int userId;
    private KoneksiDatabase db;
    private JFrame parentFrame;

    // --- DATA ---
    private Image backgroundImage;
    private String username = "User"; 

    // --- KOMPONEN UI ---
    private JTable historyTable;
    private DefaultTableModel tableModel;

    // --- ASSETS ---
    private final String PATH_ICON_BACK = "assets/button/BackButton.png";

    public HistoryPanel(int userId, KoneksiDatabase db, JFrame parentFrame) {
        this.userId = userId;
        this.db = db;
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout());
        
        // 1. Load Data
        loadUserData();

        // 2. SETUP SIDEBAR (Tombol Back)
        JPanel sidebar = new JPanel();
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(30, 20, 0, 0));

        JButton btnBack = createImageButton(PATH_ICON_BACK, 40);
        btnBack.addActionListener(e -> goBackToMainMenu());
        sidebar.add(btnBack);

        add(sidebar, BorderLayout.WEST);

        // 3. SETUP CENTER CONTENT
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        // --- CONTAINER HITAM TRANSPARAN (Rounded) ---
        JPanel tableContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BACKGROUND_TRANSLUCENT); // Hitam Transparan
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                
                // Garis tepi tipis (opsional)
                g2.setColor(new Color(255, 255, 255, 30));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 40, 40);
                
                super.paintComponent(g);
            }
        };
        tableContainer.setOpaque(false);
        // Padding di dalam kotak hitam: Top, Left, Bottom, Right
        tableContainer.setBorder(new EmptyBorder(30, 30, 30, 30)); 
        tableContainer.setPreferredSize(new Dimension(850, 500));

        // --- A. HEADER TEXT (Di dalam container) ---
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setOpaque(false);
        // Beri jarak antara teks header dengan tabel di bawahnya
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0)); 
        
        JLabel lblHello = new JLabel("hello, " + username);
        lblHello.setFont(Theme.FONT_TITLE.deriveFont(28f)); 
        lblHello.setForeground(Theme.TEXT_WHITE);
        
        JLabel lblSub = new JLabel("let’s look to what you had done");
        lblSub.setFont(Theme.FONT_BODY.deriveFont(16f));
        lblSub.setForeground(new Color(220, 220, 220)); 
        
        headerPanel.add(lblHello);
        headerPanel.add(lblSub);
        
        // Masukkan Header ke Bagian ATAS (NORTH) dari Container Hitam
        tableContainer.add(headerPanel, BorderLayout.NORTH);

        // --- B. CUSTOM TABLE (Di dalam container) ---
        createCustomTable();
        
        // ScrollPane Transparan
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(0,0,0,0));
        
        // Masukkan Tabel ke Bagian TENGAH (CENTER) dari Container Hitam
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tableContainer, gbc);

        // Wrapper agar container tidak mepet layar kanan/bawah
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(20, 20, 40, 40)); 
        wrapper.add(centerPanel, BorderLayout.CENTER);

        add(wrapper, BorderLayout.CENTER);
        
        // 4. Populate Data
        loadHistoryData();
    }

    // --- NAVIGASI ---
    private void goBackToMainMenu() {
        MainMenuPanel mainMenu = new MainMenuPanel(userId, db, parentFrame);
        PomodoroController controller = new PomodoroController(mainMenu, db, userId);
        
        parentFrame.getContentPane().removeAll();
        parentFrame.setContentPane(mainMenu);
        parentFrame.revalidate();
        parentFrame.repaint();
    }

    // --- SETUP TABLE ---
    private void createCustomTable() {
        String[] columns = {"session", "<html><center>work<br>(minutes)</center></html>", 
                            "<html><center>short break<br>(minutes)</center></html>", 
                            "<html><center>long break<br>(minutes)</center></html>"};
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        historyTable = new JTable(tableModel);
        
        // 1. Styling Dasar Tabel
        historyTable.setOpaque(false);
        historyTable.setBackground(new Color(0,0,0,0)); // Transparan total
        historyTable.setForeground(Theme.TEXT_WHITE);
        historyTable.setFont(Theme.FONT_BODY.deriveFont(14f));
        historyTable.setRowHeight(65); 
        historyTable.setShowGrid(false); 
        historyTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Selection Style
        historyTable.setSelectionBackground(new Color(255, 255, 255, 30));
        historyTable.setSelectionForeground(Theme.TEXT_WHITE);
        
        // 2. Styling Header (KUNCI AGAR TIDAK PUTIH)
        JTableHeader header = historyTable.getTableHeader();
        header.setOpaque(false); // Matikan opaque
        header.setBackground(new Color(0,0,0,0)); // Warna background transparan
        header.setForeground(Theme.TEXT_WHITE);
        header.setFont(Theme.FONT_BUTTON.deriveFont(Font.BOLD, 14f));
        header.setPreferredSize(new Dimension(0, 60));
        
        // Renderer Header (Memaksa transparan saat digambar)
        header.setDefaultRenderer(new TransparentHeaderRenderer());
        
        // Renderer Isi Tabel
        historyTable.setDefaultRenderer(Object.class, new TransparentCellRenderer());
        
        // Lebar Kolom
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(300); 
    }

    // --- LOAD DATA ---
    private void loadUserData() {
        Map<String, Object> settings = db.getUserSettings(userId);
        String bgPath = (String) settings.get("path_bg");
        if (bgPath != null) {
            this.backgroundImage = new ImageIcon(bgPath).getImage();
        }

        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT username FROM users WHERE id_user = ?")) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                this.username = rs.getString("username");
            }
        } catch (SQLException e) {
            System.err.println("Gagal load username: " + e.getMessage());
        }
    }

    private void loadHistoryData() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        List<Object[]> dataList = db.getSessionHistory(userId);
        
        for (Object[] row : dataList) {
            String sessionName = (String) row[0];
            int work = (int) row[1];
            int sb = (int) row[2];
            int lb = (int) row[3];
            Timestamp date = (Timestamp) row[4];
            
            String dateStr = (date != null) ? dateFormat.format(date) : "-";
            String formattedSession = "<html><div style='padding-left:10px;'>" + 
                                      "<b style='font-size:14px'>" + sessionName + "</b><br>" +
                                      "<span style='color:#cccccc; font-size:11px'>" + dateStr + "</span>" +
                                      "</div></html>";
            
            tableModel.addRow(new Object[]{formattedSession, work, sb, lb});
        }
    }

    // --- RENDERERS ---
    
    // Renderer Header Transparan
    class TransparentHeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Gunakan JLabel biasa, jangan super.getTableCell... karena sering membawa style default L&F
            JLabel l = new JLabel();
            l.setText(value.toString());
            l.setOpaque(false); // PENTING
            l.setBackground(new Color(0,0,0,0));
            l.setForeground(Theme.TEXT_WHITE);
            l.setFont(Theme.FONT_BUTTON.deriveFont(Font.BOLD, 14f));
            
            // Alignment
            l.setHorizontalAlignment(column == 0 ? JLabel.LEFT : JLabel.CENTER);
            if (column == 0) l.setBorder(new EmptyBorder(0, 10, 0, 0)); 
            
            return l;
        }
    }

    // Renderer Cell Transparan
    class TransparentCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            l.setOpaque(false); // Pastikan false
            l.setForeground(Theme.TEXT_WHITE);
            
            if (column == 0) {
                l.setHorizontalAlignment(JLabel.LEFT);
            } else {
                l.setHorizontalAlignment(JLabel.CENTER);
                l.setFont(Theme.FONT_BODY.deriveFont(16f)); 
            }
            
            // Efek hover/selection
            if (isSelected) {
                l.setOpaque(true);
                l.setBackground(new Color(255, 255, 255, 30));
            } else {
                l.setBackground(new Color(0,0,0,0));
            }
            
            return l;
        }
    }

    // --- UTILS UI ---
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