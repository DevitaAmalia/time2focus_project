import java.sql.*;
import java.util.*;

class KoneksiDatabase {

    private String dbName;
    private String dbUrl;
    private String username;
    private String pass;

    private Connection connection;

    // ================= Konstruktor =================
    public KoneksiDatabase() {
        this.dbName = "time2focus";
        this.username = "time2focus";
        this.pass = "time2focus";

        this.dbUrl = "jdbc:mysql://localhost:3306/" + this.dbName;

        initialize();
    }

    // ================= Koneksi =================
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(dbUrl, username, pass);
            }
        } catch (SQLException e) {
            System.err.println("Koneksi database gagal: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    // ================= Inisialisasi Database =================
    public void initialize() {

        String createUsersTableSql = "CREATE TABLE IF NOT EXISTS users (" +
                "  id_user INT NOT NULL AUTO_INCREMENT," +
                "  username VARCHAR(50) NOT NULL," +
                "  password VARCHAR(255) NOT NULL," +
                "  PRIMARY KEY (id_user)," +
                "  UNIQUE KEY username (username)" +
                ")";

        String createBackgroundsTableSql = "CREATE TABLE IF NOT EXISTS backgrounds (" +
                "  id_bg INT NOT NULL AUTO_INCREMENT," +
                "  nama_bg VARCHAR(50)," +
                "  path_bg VARCHAR(255) NOT NULL," +
                "  PRIMARY KEY (id_bg)" +
                ")";

        String createMusicsTableSql = "CREATE TABLE IF NOT EXISTS musics (" +
                "  id_music INT NOT NULL AUTO_INCREMENT," +
                "  nama_music VARCHAR(50)," +
                "  path_music VARCHAR(255) NOT NULL," +
                "  PRIMARY KEY (id_music)" +
                ")";

        String createSettingsTableSql = "CREATE TABLE IF NOT EXISTS settings (" +
                "  id_setting INT NOT NULL AUTO_INCREMENT," +
                "  id_user INT NOT NULL UNIQUE," +
                "  work_duration INT NOT NULL DEFAULT 25," +
                "  sb_duration INT NOT NULL DEFAULT 5," +
                "  lb_duration INT NOT NULL DEFAULT 15," +
                "  id_bg INT DEFAULT 1," +
                "  id_music INT DEFAULT 1," +
                "  PRIMARY KEY (id_setting)," +
                "  FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE CASCADE," +
                "  FOREIGN KEY (id_bg) REFERENCES backgrounds(id_bg) ON DELETE SET NULL," +
                "  FOREIGN KEY (id_music) REFERENCES musics(id_music) ON DELETE SET NULL" +
                ")";

        String createHistoryTableSql = "CREATE TABLE IF NOT EXISTS history (" +
                "  id_history INT NOT NULL AUTO_INCREMENT," +
                "  id_user INT NOT NULL," +
                "  nama_session VARCHAR(100) NOT NULL," +
                "  work INT NOT NULL," +
                "  short_break INT NOT NULL," +
                "  long_break INT NOT NULL," +
                "  session_date DATETIME NOT NULL," +
                "  PRIMARY KEY (id_history)," +
                "  FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE CASCADE" +
                ")";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            if (conn != null) {
                stmt.execute(createUsersTableSql);
                stmt.execute(createBackgroundsTableSql);
                stmt.execute(createMusicsTableSql);
                stmt.execute(createSettingsTableSql);
                stmt.execute(createHistoryTableSql);

                // Masukkan default data background & musik
                ensureDefaultBackgrounds(conn);
                ensureDefaultMusics(conn);
            }

        } catch (SQLException e) {
            System.err.println("Gagal inisialisasi tabel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ================= Default Background =================
    private void ensureDefaultBackgrounds(Connection conn) {
        String countSql = "SELECT COUNT(*) FROM backgrounds";
        String insertSql = "INSERT INTO backgrounds (nama_bg, path_bg) VALUES (?, ?)";

        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(countSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Tabel 'backgrounds' kosong. Memuat data default...");

                List<String[]> defaultBg = Arrays.asList(
                        new String[] { "City Scrapper", "assets/background/city_scrapper.png" },
                        new String[] { "Firework Festive", "assets/background/firework_festive.png" },
                        new String[] { "Fractal Library", "assets/background/fractal_library.png" },
                        new String[] { "Midnight Forest", "assets/background/midnight_forest.png" },
                        new String[] { "Night Riverside", "assets/background/night_riverside.png" },
                        new String[] { "Sunset in The Beach", "assets/background/sunset_in_the_beach.png" });

                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    for (String[] bg : defaultBg) {
                        pstmt.setString(1, bg[0]);
                        pstmt.setString(2, bg[1]);
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
            }

        } catch (SQLException e) {
            System.err.println("Gagal inisialisasi default backgrounds: " + e.getMessage());
        }
    }

    // ================= Default Music =================
    private void ensureDefaultMusics(Connection conn) {
        String countSql = "SELECT COUNT(*) FROM musics";
        String insertSql = "INSERT INTO musics (nama_music, path_music) VALUES (?, ?)";

        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(countSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Tabel 'musics' kosong. Memuat data default...");

                List<String[]> defaultMusic = Arrays.asList(
                        new String[] { "Midsummer Rain", "assets/backsound/midsummer_rain.wav" },
                        new String[] { "Wasteland Route", "assets/backsound/wasteland_route.wav" },
                        new String[] { "Weightless Paradise", "assets/backsound/weightless_paradise.wav" });

                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    for (String[] music : defaultMusic) {
                        pstmt.setString(1, music[0]);
                        pstmt.setString(2, music[1]);
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
            }

        } catch (SQLException e) {
            System.err.println("Gagal inisialisasi default musics: " + e.getMessage());
        }
    }

    // ================= Register User =================
    public boolean registerUser(String username, String password) {
        String insertUserSql = "INSERT INTO users (username, password) VALUES (?, ?)";
        String insertSettingSql = "INSERT INTO settings (id_user) VALUES (?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            int newUserId = -1;

            // Insert User
            try (PreparedStatement pstmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, username);
                pstmt.setString(2, password);

                int rows = pstmt.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        newUserId = keys.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Insert Default Settings untuk User
            try (PreparedStatement pstmt = conn.prepareStatement(insertSettingSql)) {
                pstmt.setInt(1, newUserId);
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.err.println("Registrasi gagal: Username '" + username + "' sudah ada.");
            } else {
                System.err.println("Error saat registrasi: " + e.getMessage());
            }
            return false;
        }
    }

    // ================= Login User =================
    public int loginUser(String username, String password) {
        String sql = "SELECT id_user FROM users WHERE username = ? AND password = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_user");
                }
                return -1;
            }

        } catch (SQLException e) {
            System.err.println("Error saat login: " + e.getMessage());
            return -1;
        }
    }

    // ================= Simpan History Session =================
    public boolean saveSessionHistory(int userId, String sessionName,
            int workTime, int sbTime, int lbTime) {

        String sql = "INSERT INTO history " +
                "(id_user, nama_session, work, short_break, long_break, session_date) " +
                "VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, sessionName);
            pstmt.setInt(3, workTime);
            pstmt.setInt(4, sbTime);
            pstmt.setInt(5, lbTime);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error saat menyimpan history sesi: " + e.getMessage());
            return false;
        }
    }

    // ================= Ambil History Per User =================
    public List<Object[]> getSessionHistory(int userId) {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT nama_session, work, short_break, long_break, session_date " +
                "FROM history WHERE id_user = ? ORDER BY session_date DESC";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[5];
                    row[0] = rs.getString("nama_session");
                    row[1] = rs.getInt("work");
                    row[2] = rs.getInt("short_break");
                    row[3] = rs.getInt("long_break");
                    row[4] = rs.getTimestamp("session_date");
                    list.add(row);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error saat mengambil history: " + e.getMessage());
        }

        return list;
    }

    // ================= Baca Setting User =================
    public Map<String, Object> getUserSettings(int userId) {
        Map<String, Object> map = new HashMap<>();

        String sql = "SELECT s.work_duration, s.sb_duration, s.lb_duration, " +
                "       s.id_bg, s.id_music, " +
                "       b.path_bg, m.path_music " +
                "FROM settings s " +
                "LEFT JOIN backgrounds b ON s.id_bg = b.id_bg " +
                "LEFT JOIN musics m ON s.id_music = m.id_music " +
                "WHERE s.id_user = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    map.put("work_duration", rs.getInt("work_duration"));
                    map.put("sb_duration", rs.getInt("sb_duration"));
                    map.put("lb_duration", rs.getInt("lb_duration"));
                    map.put("id_bg", rs.getInt("id_bg"));
                    map.put("id_music", rs.getInt("id_music"));
                    map.put("path_bg", rs.getString("path_bg"));
                    map.put("path_music", rs.getString("path_music"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error saat mengambil setting: " + e.getMessage());
        }

        return map;
    }

    // ================= Update Setting User =================
    public boolean updateUserSettings(int userId,
            int workDur, int sbDur, int lbDur,
            int idBg, int idMusic) {

        String sql = "UPDATE settings SET " +
                "work_duration=?, sb_duration=?, lb_duration=?, " +
                "id_bg=?, id_music=? " +
                "WHERE id_user=?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, workDur);
            pstmt.setInt(2, sbDur);
            pstmt.setInt(3, lbDur);
            pstmt.setInt(4, idBg);
            pstmt.setInt(5, idMusic);
            pstmt.setInt(6, userId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error saat update setting: " + e.getMessage());
            return false;
        }
    }
}
