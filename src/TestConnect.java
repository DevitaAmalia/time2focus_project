import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnect { //Class ini cuma dipake buat mastiin klo jdbc bener2 connect
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/time2focus";
        String user = "root";
        String pass = "";

        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Koneksi berhasil!");
            conn.close();
        } catch (SQLException e) {
            System.out.println("Koneksi gagal: " + e.getMessage());
        }
    }
}

