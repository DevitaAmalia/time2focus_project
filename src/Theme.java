import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class Theme {
    
    // --- 1. PALET WARNA (Monokrom / Clean) ---
    public static final Color BACKGROUND_TRANSLUCENT = new Color(0, 0, 0, 180); 
    
    // Warna Teks 
    public static final Color TEXT_WHITE = Color.WHITE; 
    public static final Color TEXT_BLACK = Color.BLACK;

    // --- 2. CONFIG TOMBOL (RoundedButton) ---
    // Normal: Putih
    public static final Color BTN_BG_NORMAL = Color.WHITE;
    public static final Color BTN_FG_NORMAL = Color.BLACK;
    
    // Hover: Hitam Transparan
    public static final Color BTN_BG_HOVER = new Color(0, 0, 0, 180); 
    public static final Color BTN_FG_HOVER = Color.WHITE;
    public static final Color BTN_BORDER_HOVER = Color.WHITE;

    // --- 3. CONFIG PROGRESS BAR ---
    public static final Color PROGRESS_BAR_FG = Color.WHITE;
    public static final Color PROGRESS_BAR_BG = new Color(255, 255, 255, 30); // Putih pudar

    // --- 4. FONT HANDLING ---
    public static final String FONT_NAME = "Space Grotesk";
    
    // Deklarasi Font 
    public static Font FONT_TIMER_BIG;    // Pakai Bold
    public static Font FONT_TITLE;        // Pakai Bold
    public static Font FONT_SUBHEADER;    // Pakai Bold
    public static Font FONT_BODY;         // Pakai Medium
    public static Font FONT_BUTTON;       // Pakai Medium
    public static Font FONT_CAPTION;      // Pakai Regular 

    // --- 5. STYLES (SimpleAttributeSet) ---
    public static final SimpleAttributeSet STYLE_DEFAULT = new SimpleAttributeSet();
    public static final SimpleAttributeSet STYLE_WORK = new SimpleAttributeSet();
    public static final SimpleAttributeSet STYLE_BREAK = new SimpleAttributeSet();

    // --- 6. STATIC INITIALIZATION (Setup Font & Styles) ---
    static {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

            // A. LOAD BASE FONTS 
            Font baseBold    = loadFont("assets/font/SpaceGrotesk-Bold.ttf");
            Font baseRegular = loadFont("assets/font/SpaceGrotesk-Regular.ttf");
            Font baseMedium  = loadFont("assets/font/SpaceGrotesk-Medium.ttf");
            
            // Register agar sistem mengenali 
            if (baseBold != null) ge.registerFont(baseBold);
            if (baseRegular != null) ge.registerFont(baseRegular);
            if (baseMedium != null) ge.registerFont(baseMedium);

            // B. SETUP FONT PENGGUNAAN 
            
            // 1. Timer, Judul, Subheader 
            if (baseBold != null) {
                FONT_TIMER_BIG = baseBold.deriveFont(100f);
                FONT_TITLE     = baseBold.deriveFont(35f);
                FONT_SUBHEADER = baseBold.deriveFont(30f); 
            }

            // 2. Teks Caption, untuk isi tabel history 
            if (baseRegular != null) {
                FONT_CAPTION   = baseRegular.deriveFont(18f);
            }

            // 3. Teks biasa, Button untuk tombol putih, body untuk tulisan selain title, isi history
            if (baseMedium != null) {
                FONT_BUTTON    = baseMedium.deriveFont(30f);
                FONT_BODY      = baseMedium.deriveFont(16f);
            }

            StyleConstants.setForeground(STYLE_DEFAULT, TEXT_WHITE);
            StyleConstants.setFontFamily(STYLE_DEFAULT, FONT_NAME);
            StyleConstants.setForeground(STYLE_WORK, TEXT_WHITE);
            StyleConstants.setForeground(STYLE_BREAK, TEXT_WHITE);

        } catch (Exception e) {
            e.printStackTrace();
            loadFallbackFonts();
        }
    }
    
    private static Font loadFont(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                return Font.createFont(Font.TRUETYPE_FONT, file);
            }
        } catch (FontFormatException | IOException e) {
            System.err.println("Gagal load font: " + path);
        }
        return null; 
    }

    private static void loadFallbackFonts() {
        Font f = new Font("SansSerif", Font.PLAIN, 14);
        FONT_TIMER_BIG = f.deriveFont(Font.BOLD, 90f);
        FONT_TITLE = f.deriveFont(Font.BOLD, 22f);
        FONT_BODY = f.deriveFont(Font.PLAIN, 14f);
        FONT_BUTTON = f.deriveFont(Font.BOLD, 16f);
        FONT_CAPTION = f.deriveFont(Font.PLAIN, 12f);
        FONT_SUBHEADER = f.deriveFont(Font.BOLD, 16f);
    }
    
    private Theme() {}
}