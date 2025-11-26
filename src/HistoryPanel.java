import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class HistoryPanel extends JPanel {

    // --- DEPENDENSI ---
    private final int userId;
    private final KoneksiDatabase db;

    // --- FORMATTER ---
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    // ================== KONSTRUKTOR ==================
    public HistoryPanel(int userId, KoneksiDatabase db) {
        this.userId = userId;
        this.db = db;

        // Gunakan BorderLayout untuk menempatkan judul dan area konten
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. Tambahkan Judul
        JLabel titleLabel = new JLabel("SESSION HISTORY");
        // Gunakan Theme.FONT_TITLE dan sesuaikan ukurannya
        titleLabel.setFont(Theme.FONT_TITLE.deriveFont(28f)); 
        titleLabel.setForeground(Theme.TEXT_WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // 2. Muat dan Tampilkan Riwayat
        JScrollPane scrollPane = createHistoryList();
        add(scrollPane, BorderLayout.CENTER);
    }

    // ================== MUAT DATA & BUAT LIST ==================

    private JScrollPane createHistoryList() {
        // Kontainer untuk menampung setiap item riwayat
        JPanel historyContainer = new JPanel();
        historyContainer.setLayout(new BoxLayout(historyContainer, BoxLayout.Y_AXIS));
        historyContainer.setOpaque(false); 

        List<Object[]> historyData = db.getSessionHistory(userId);

        if (historyData.isEmpty()) {
            JLabel emptyLabel = new JLabel("Belum ada riwayat sesi yang tersimpan.");
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
        // Gunakan warna transparan dari Theme
        panel.setBackground(Theme.BACKGROUND_TRANSLUCENT); 
        panel.setBorder(BorderFactory.createCompoundBorder(
            // Border tipis dengan warna PROGRESS_BAR_FG
            BorderFactory.createLineBorder(Theme.PROGRESS_BAR_FG, 1), 
            new EmptyBorder(10, 15, 10, 15)
        ));
        panel.setMaximumSize(new Dimension(800, 80)); 
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // --- 1. Sisi Kiri (Nama Sesi & Tanggal) ---
        JPanel leftPanel = new JPanel(new GridLayout(2, 1));
        leftPanel.setOpaque(false);
        
        // Akses data menggunakan indeks array: [0]
        String sessionName = (String) session[0]; 
        JLabel lblName = new JLabel(sessionName);
        // Gunakan FONT_BODYBOLD
        lblName.setFont(Theme.FONT_BODYBOLD.deriveFont(16f)); 
        lblName.setForeground(Theme.TEXT_WHITE);
        leftPanel.add(lblName);
        
        // Akses data menggunakan indeks array: [4]
        Timestamp timestamp = (Timestamp) session[4]; 
        String dateText = "Date N/A";
        if (timestamp != null) {
            dateText = LocalDateTime.ofInstant(timestamp.toInstant(), java.time.ZoneId.systemDefault())
                                    .format(DATE_FORMAT);
        }
        JLabel lblDate = new JLabel(dateText);
        // Gunakan FONT_CAPTION
        lblDate.setFont(Theme.FONT_CAPTION); 
        // Gunakan warna sedikit pudar (TEXT_WHITE.darker())
        lblDate.setForeground(Theme.TEXT_WHITE.darker()); 
        leftPanel.add(lblDate);
        
        panel.add(leftPanel, BorderLayout.WEST);

        // --- 2. Sisi Kanan (Detail Durasi) ---
        
        // Akses data menggunakan indeks array: [1], [2], [3]
        int workMin = (Integer) session[1];
        int sbMin = (Integer) session[2];
        int lbMin = (Integer) session[3];

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        // Warna untuk Durasi (menggunakan Theme.PROGRESS_BAR_FG sebagai warna utama)
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
        // Gunakan FONT_BODYBOLD
        lblValue.setFont(Theme.FONT_BODYBOLD.deriveFont(18f)); 
        lblValue.setForeground(color); 
        lblValue.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblLabel = new JLabel(label);
        // Gunakan FONT_CAPTION
        lblLabel.setFont(Theme.FONT_CAPTION); 
        lblLabel.setForeground(Theme.TEXT_WHITE.darker());
        lblLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        item.add(lblValue);
        item.add(lblLabel);
        return item;
    }

    // ================== PAINT BACKGROUND ==================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Fallback Gradient Background (Jika panel ini tidak berada di atas komponen lain)
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(
            0, 0, new Color(22, 33, 62), 
            0, getHeight(), new Color(15, 52, 96)); 
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}