import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // SwingUtilities memastikan GUI berjalan di Thread yang aman (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Inisialisasi Database
                System.out.println("Menghubungkan ke Database...");
                KoneksiDatabase db = new KoneksiDatabase(); 
                // Constructor KoneksiDatabase otomatis membuat tabel & data default jika belum ada
                
                // 2. Simulasi Login Otomatis (Hardcoded untuk testing)
                String username = "phaichan";
                String password = "ppppp";
                
                // Coba register dulu (jaga-jaga kalau database masih kosong/baru direset)
                db.registerUser(username, password);
                
                // Lakukan Login untuk mendapatkan ID User
                int userId = db.loginUser(username, password);
                
                if (userId != -1) {
                    System.out.println("Login Berhasil! User ID: " + userId);
                    
                    // 3. Setup Frame Utama
                    JFrame frame = new JFrame("Time2Focus - Pomodoro Timer");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setSize(900, 700);
                    frame.setLocationRelativeTo(null); // Posisi di tengah layar
                    
                    // 4. Masukkan Panel Utama (MainMenuPanel)
                    // Kita oper userId dan objek db ke panel ini
                    MainMenuPanel mainMenu = new MainMenuPanel(userId, db);
                    frame.add(mainMenu);
                    
                    // Tampilkan
                    frame.setVisible(true);
                    
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "Login Gagal! Cek username/password di Main.java", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Terjadi kesalahan database: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}