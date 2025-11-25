import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Menghubungkan ke Database...");
                KoneksiDatabase db = new KoneksiDatabase();
                
                // Setup Frame Utama
                JFrame frame = new JFrame("Time2Focus");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(800, 600);
                frame.setLocationRelativeTo(null);
                frame.setResizable(true);
                
                // Tampilkan LoginPanel
                LoginPanel loginPanel = new LoginPanel(db, frame);
                frame.add(loginPanel);
                frame.setVisible(true);
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Terjadi kesalahan database: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}