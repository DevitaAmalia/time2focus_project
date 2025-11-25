import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import javax.swing.text.SimpleAttributeSet;

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
    
    // Deklarasi Font (Nanti diisi di blok static)
    public static Font FONT_TIMER_BIG;    // Pakai Bold
    public static Font FONT_TITLE;        // Pakai Bold
    public static Font FONT_SUBHEADER;    // Pakai Medium/Regular
    public static Font FONT_BODY;         // Pakai Regular
    public static Font FONT_BUTTON;       // Pakai Bold
    public static Font FONT_CAPTION;      // Pakai Light 

    // --- 5. STYLES (SimpleAttributeSet) ---
    public static final SimpleAttributeSet STYLE_DEFAULT = new SimpleAttributeSet();
    public static final SimpleAttributeSet STYLE_WORK = new SimpleAttributeSet();
    public static final SimpleAttributeSet STYLE_BREAK = new SimpleAttributeSet();

    // --- 6. STATIC INITIALIZATION (Setup Font & Styles) ---
    static {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

            // A. LOAD BASE FONTS (Muat file mentahannya)
            // Sesuaikan path ini dengan folder assets Anda
            Font baseBold    = loadFont("assets/font/SpaceGrotesk-Bold.ttf");
            Font baseRegular = loadFont("assets/font/SpaceGrotesk-Regular.ttf");
            Font baseLight   = loadFont("assets/font/SpaceGrotesk-Light.ttf");
            Font baseMedium  = loadFont("assets/font/SpaceGrotesk-Medium.ttf");
            
            // Register agar sistem mengenali (opsional tapi good practice)
            if (baseBold != null) ge.registerFont(baseBold);
            if (baseRegular != null) ge.registerFont(baseRegular);
            if (baseLight != null) ge.registerFont(baseLight);
            if (baseMedium != null) ge.registerFont(baseMedium);

            // B. SETUP FONT PENGGUNAAN (Derive Size dari Base Font yang tepat)
            
            // 1. Timer & Judul -> Pakai baseBold
            if (baseBold != null) {
                FONT_TIMER_BIG = baseBold.deriveFont(90f);
                FONT_TITLE     = baseBold.deriveFont(22f);
                FONT_SUBHEADER = baseRegular.deriveFont(16f); 
            }

            // 2. Teks Biasa -> Pakai baseRegular
            if (baseRegular != null) {
                FONT_CAPTION   = baseLight.deriveFont(12f);
            }

            // 3. Teks Tipis/Kecil -> Pakai baseLight
            if (baseMedium != null) {
                FONT_BUTTON    = baseBold.deriveFont(16f);
                FONT_BODY      = baseRegular.deriveFont(14f);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback ke default font jika gagal load
            loadFallbackFonts();
        }
    }
    
    // Helper method agar kodingan rapi (Private)
    private static Font loadFont(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                return Font.createFont(Font.TRUETYPE_FONT, file);
            }
        } catch (FontFormatException | IOException e) {
            System.err.println("Gagal load font: " + path);
        }
        return null; // Return null jika gagal
    }

    private static void loadFallbackFonts() {
        Font f = new Font("SansSerif", Font.PLAIN, 14);
        FONT_TIMER_BIG = f.deriveFont(Font.BOLD, 90f);
        FONT_TITLE = f.deriveFont(Font.BOLD, 22f);
        FONT_BODY = f.deriveFont(Font.PLAIN, 14f);
        FONT_BUTTON = f.deriveFont(Font.BOLD, 16f);
        FONT_CAPTION = f.deriveFont(Font.PLAIN, 12f);
    }
    
    private Theme() {}
}