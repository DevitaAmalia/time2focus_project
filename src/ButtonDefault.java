import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ButtonDefault extends JButton {

    private boolean isHovered = false; // State untuk melacak apakah mouse ada di atas tombol
    private int cornerRadius = 50;     

    public ButtonDefault(String text) {
        super(text);
        
        // 1. Setup Tampilan Dasar
        setFont(Theme.FONT_BUTTON);
        setContentAreaFilled(false); 
        setFocusPainted(false);      // Matikan kotak fokus saat diklik
        setBorderPainted(false);     // Matikan border bawaan
        setOpaque(false);     // Transparan agar rounded corner terlihat
        
        // Warna Default 
        setForeground(Theme.BTN_FG_NORMAL); 

        // 2. LOGIKA HOVER 
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                setCursor(new Cursor(Cursor.HAND_CURSOR)); // Ubah kursor jadi tangan
                
                // Ubah warna teks saat hover
                setForeground(Theme.BTN_FG_HOVER); 
                
                repaint(); 
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                
                // Balikkan warna teks ke normal
                setForeground(Theme.BTN_FG_NORMAL); 
                
                repaint(); 
            }
        });
    }

    // 3. Menggambar Ulang Tombol 
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isHovered) {
            // --- TAMPILAN SAAT HOVER ---
            // Background Hitam Transparan
            g2.setColor(Theme.BTN_BG_HOVER); 
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            
            // Garis Tepi Putih
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2)); 
            g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, cornerRadius, cornerRadius);
        } else {
            // Background Putih Solid
            g2.setColor(Theme.BTN_BG_NORMAL);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        }

        g2.dispose();
        
        // Panggil super untuk menggambar teks 
        super.paintComponent(g); 
    }
}
