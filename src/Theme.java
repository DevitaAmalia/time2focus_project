import java.awt.Color;
import java.awt.Font;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class Theme {
    
    // Background dan Umum
    public static final Color BACKGROUND_TRANSLUCENT = new Color(0, 0, 0, 180); // Latar belakang semi-transparan utama.
    public static final Color BACKGROUND_SOLID = new Color(30, 30, 30); // Latar belakang solid gelap.
    public static final Color ACCENT_COLOR = new Color(137, 207, 240); // Biru Muda Cerah (Aksen UI).
    
    // Teks dan Input
    public static final Color TEXT_DEFAULT_COLOR = Color.WHITE; // Warna teks default.
    public static final Color INPUT_FIELD_BG = new Color(70, 70, 70, 150); // Background input semi-transparan.
    public static final Color HIGHLIGHT_BG = new Color(70, 70, 70, 200); // Highlight gelap untuk tombol/fokus.
    
    // Warna Sesi Pomodoro
    public static final Color COLOR_WORK = new Color(231, 76, 60); // Merah (Sesi Kerja).
    public static final Color COLOR_BREAK = new Color(46, 204, 113); // Hijau (Sesi Istirahat).

    // --- KONSTANTA FONT (Disajikan berdasarkan kebutuhan Pomodoro Timer) ---
    public static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 18); // Font untuk judul/header utama.
    public static final Font FONT_TIMER_BIG = new Font("Monospaced", Font.BOLD, 100); // Font besar untuk timer.
    public static final Font FONT_TEXT_DEFAULT = new Font("Arial", Font.PLAIN, 14); // Font teks umum.
    public static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 16); // Font untuk tombol.

    // --- KONSTANTA STYLE (SimpleAttributeSet) ---
    public static final SimpleAttributeSet STYLE_DEFAULT = new SimpleAttributeSet();
    public static final SimpleAttributeSet STYLE_WORK = new SimpleAttributeSet();
    public static final SimpleAttributeSet STYLE_BREAK = new SimpleAttributeSet();
    public static final SimpleAttributeSet STYLE_HIGHLIGHT_ACCENT = new SimpleAttributeSet();

    static {
        // STYLE_DEFAULT: Warna teks default (Putih)
        StyleConstants.setForeground(STYLE_DEFAULT, TEXT_DEFAULT_COLOR); 
        
        // STYLE_WORK: Style yang menunjukkan Sesi Kerja (Merah)
        StyleConstants.setForeground(STYLE_WORK, COLOR_WORK); 
        
        // STYLE_BREAK: Style yang menunjukkan Sesi Istirahat (Hijau)
        StyleConstants.setForeground(STYLE_BREAK, COLOR_BREAK); 
        
        // STYLE_HIGHLIGHT_ACCENT: Style highlight menggunakan warna aksen
        StyleConstants.setForeground(STYLE_HIGHLIGHT_ACCENT, ACCENT_COLOR);
        StyleConstants.setBackground(STYLE_HIGHLIGHT_ACCENT, BACKGROUND_SOLID); 
    }
    
    // Jika Anda ingin mempertahankan metode get instance (Singleton Pattern):
    private Theme() {}
}