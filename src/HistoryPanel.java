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

    private String formatMinutes(Object value) {
        if (value == null) return "0";
        try {
            return String.valueOf(Integer.parseInt(value.toString()));
        } catch (NumberFormatException e) {
            return "0";
        }
    }

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
                g2.setColor(Color.BLACK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

                // Border putih seperti container Settings
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 40, 40);

                super.paintComponent(g);
            }
        };
        tableContainer.setOpaque(false);
        tableContainer.setBorder(new EmptyBorder(30, 30, 30, 30)); 
        tableContainer.setPreferredSize(new Dimension(540, 500));
        tableContainer.setMinimumSize(new Dimension(540, 500));
        tableContainer.setMaximumSize(new Dimension(540, 500));

        // --- A. HEADER TEXT (Di dalam container) ---
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0)); 
        
        JLabel lblHello = new JLabel("hello, " + username);
        lblHello.setFont(Theme.FONT_TITLE.deriveFont(28f)); 
        lblHello.setForeground(Theme.TEXT_WHITE);
        
        JLabel lblSub = new JLabel("let’s look to what you had done");
        lblSub.setFont(Theme.FONT_BODY);
        lblSub.setForeground(new Color(220, 220, 220)); 
        
        headerPanel.add(lblHello);
        headerPanel.add(lblSub);
        
        tableContainer.add(headerPanel, BorderLayout.NORTH);

        // --- B. CUSTOM TABLE (Di dalam container) ---
        createCustomTable();
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setOpaque(true);
        scrollPane.setBackground(Color.BLACK);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(Color.BLACK);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tableContainer, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(30, 20, 40, 40)); 
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
        String[] columns = {"session", "work", "short break", "long break"};
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        historyTable = new JTable(tableModel);
        
        // 1. Styling Dasar Tabel
        historyTable.setOpaque(true);
        historyTable.setBackground(Color.BLACK);
        historyTable.setForeground(Theme.TEXT_WHITE);
        historyTable.setFont(Theme.FONT_BODY.deriveFont(12f));
        historyTable.setRowHeight(54); 
        historyTable.setShowGrid(false); 
        historyTable.setIntercellSpacing(new Dimension(0, 0));
        historyTable.setFillsViewportHeight(true);
        historyTable.setRowSelectionAllowed(false);
        historyTable.setColumnSelectionAllowed(false);
        historyTable.setCellSelectionEnabled(false);
        historyTable.setFocusable(false);
        
        // 2. Styling Header 
        JTableHeader header = historyTable.getTableHeader();
        header.setOpaque(true); 
        header.setBackground(Color.BLACK); 
        header.setForeground(Theme.TEXT_WHITE);
        header.setFont(Theme.FONT_BODYBOLD.deriveFont(14f));
        header.setPreferredSize(new Dimension(0, 40));
        
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setOpaque(true);
                l.setBackground(Color.BLACK);
                l.setForeground(Theme.TEXT_WHITE);
                l.setFont(Theme.FONT_BUTTON.deriveFont(Font.BOLD, 14f));
                l.setHorizontalAlignment(column == 0 ? JLabel.LEFT : JLabel.CENTER);
                if (column == 0) l.setBorder(new EmptyBorder(0, 10, 0, 0));
                return l;
            }
        });
        
        historyTable.getColumnModel().getColumn(0).setCellRenderer(new SessionCellRenderer());
        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setOpaque(true);
                l.setBackground(Color.BLACK);
                l.setForeground(Theme.TEXT_WHITE);
                l.setHorizontalAlignment(JLabel.CENTER);
                l.setFont(Theme.FONT_BODY.deriveFont(13f)); 
                return l;
            }
        });
        
        // Lebar Kolom
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(90);
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
            String work = formatMinutes(row[1]);
            String sb = formatMinutes(row[2]);
            String lb = formatMinutes(row[3]);
            Timestamp date = (Timestamp) row[4];

            String dateStr = (date != null) ? dateFormat.format(date) : "-";
            SessionCell cell = new SessionCell(sessionName, dateStr);
            tableModel.addRow(new Object[]{cell, work, sb, lb});
        }
    }

    /** Data holder untuk kolom sesi (nama + tanggal). */
    private static class SessionCell {
        final String sessionName;
        final String dateText;
        SessionCell(String sessionName, String dateText) {
            this.sessionName = sessionName;
            this.dateText = dateText;
        }
    }

    /** Renderer Swing murni (tanpa HTML) untuk kolom pertama. */
    private static class SessionCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(true);
            panel.setBackground(Color.BLACK);
            panel.setBorder(new EmptyBorder(4, 10, 4, 4));

            JLabel nameLabel = new JLabel();
            nameLabel.setForeground(Theme.TEXT_WHITE);
            nameLabel.setFont(Theme.FONT_BODYBOLD.deriveFont(13f));

            JLabel dateLabel = new JLabel();
            dateLabel.setForeground(new Color(200, 200, 200));
            dateLabel.setFont(Theme.FONT_CAPTION.deriveFont(10f));

            if (value instanceof SessionCell) {
                SessionCell cell = (SessionCell) value;
                nameLabel.setText(cell.sessionName);
                dateLabel.setText(cell.dateText);
            } else if (value != null) {
                nameLabel.setText(value.toString());
                dateLabel.setText("");
            }

            panel.add(nameLabel);
            panel.add(dateLabel);
            return panel;
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
