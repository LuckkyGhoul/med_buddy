package medbuddy;

import java.sql.*;
import java.io.File;

/**
 * Manages H2 embedded database for MedBuddy.
 * DB file stored at: user.home/.medbuddy/medbuddy.db
 * Tables: users, health_profiles, conditions, progress_log
 */
public class DatabaseManager {

    private static final String DB_DIR  = System.getProperty("user.home") + File.separator + ".medbuddy";
    private static final String DB_PATH = DB_DIR + File.separator + "medbuddy";
    private static final String URL     = "jdbc:h2:" + DB_PATH + ";AUTO_SERVER=FALSE";

    private static Connection conn;

    // ── Initialise ──────────────────────────────────────────────────
    public static void init() {
        try {
            new File(DB_DIR).mkdirs();
            Class.forName("org.h2.Driver");
            conn = DriverManager.getConnection(URL, "medbuddy", "");
            createTables();
            seedDemoUser();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot open database: " + e.getMessage());
        }
    }

    private static void createTables() throws SQLException {
        Statement st = conn.createStatement();

        st.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id            INTEGER PRIMARY KEY AUTO_INCREMENT,
                username      VARCHAR(60)  NOT NULL UNIQUE,
                email         VARCHAR(120) NOT NULL,
                password_hash VARCHAR(64)  NOT NULL,
                age           INTEGER      NOT NULL,
                gender        VARCHAR(20)  NOT NULL,
                created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS health_profiles (
                user_id           INTEGER PRIMARY KEY REFERENCES users(id),
                weight_kg         DOUBLE DEFAULT 0,
                height_cm         DOUBLE DEFAULT 0,
                sugar_level       DOUBLE DEFAULT 0,
                cholesterol       DOUBLE DEFAULT 0,
                vitamin_d3        DOUBLE DEFAULT 0,
                vitamin_b12       DOUBLE DEFAULT 0,
                updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS conditions (
                id         INTEGER PRIMARY KEY AUTO_INCREMENT,
                user_id    INTEGER NOT NULL REFERENCES users(id),
                condition  VARCHAR(100) NOT NULL
            )
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS progress_log (
                id           INTEGER PRIMARY KEY AUTO_INCREMENT,
                user_id      INTEGER NOT NULL REFERENCES users(id),
                period_label VARCHAR(40)  NOT NULL,
                weight       DOUBLE DEFAULT 0,
                sugar        DOUBLE DEFAULT 0,
                cholesterol  DOUBLE DEFAULT 0,
                vitamin_d3   DOUBLE DEFAULT 0,
                vitamin_b12  DOUBLE DEFAULT 0,
                notes        VARCHAR(500) DEFAULT '',
                logged_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        st.close();
    }

    private static void seedDemoUser() throws SQLException {
        // Only seed if demo doesn't exist
        PreparedStatement chk = conn.prepareStatement("SELECT id FROM users WHERE username=?");
        chk.setString(1, "demo");
        ResultSet rs = chk.executeQuery();
        if (rs.next()) { rs.close(); chk.close(); return; }
        rs.close(); chk.close();

        // Insert demo user
        PreparedStatement ins = conn.prepareStatement(
            "INSERT INTO users(username,email,password_hash,age,gender) VALUES(?,?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS);
        ins.setString(1, "demo");
        ins.setString(2, "demo@medbuddy.com");
        ins.setString(3, hash("demo123"));
        ins.setInt(4, 28);
        ins.setString(5, "Female");
        ins.executeUpdate();
        ResultSet keys = ins.getGeneratedKeys();
        keys.next();
        int uid = keys.getInt(1);
        keys.close(); ins.close();

        // Profile
        PreparedStatement hp = conn.prepareStatement(
            "INSERT INTO health_profiles VALUES(?,?,?,?,?,?,?,CURRENT_TIMESTAMP)");
        hp.setInt(1, uid);
        hp.setDouble(2, 65.0); hp.setDouble(3, 162.0);
        hp.setDouble(4, 110.0); hp.setDouble(5, 195.0);
        hp.setDouble(6, 18.0); hp.setDouble(7, 180.0);
        hp.executeUpdate(); hp.close();

        // Conditions
        for (String c : new String[]{"Diabetes", "Vitamin D3 Deficiency"}) {
            PreparedStatement pc = conn.prepareStatement("INSERT INTO conditions(user_id,condition) VALUES(?,?)");
            pc.setInt(1, uid); pc.setString(2, c);
            pc.executeUpdate(); pc.close();
        }

        // Progress entries
        Object[][] prog = {
            {"Jan 2025", 68.0, 130.0, 210.0, 12.0, 150.0, "Started diet plan"},
            {"Feb 2025", 67.0, 120.0, 202.0, 15.0, 165.0, "Feeling better"},
            {"Mar 2025", 65.0, 110.0, 195.0, 18.0, 180.0, "Good progress!"},
        };
        for (Object[] row : prog) {
            PreparedStatement pp = conn.prepareStatement(
                "INSERT INTO progress_log(user_id,period_label,weight,sugar,cholesterol,vitamin_d3,vitamin_b12,notes) VALUES(?,?,?,?,?,?,?,?)");
            pp.setInt(1, uid);
            pp.setString(2, (String) row[0]);
            pp.setDouble(3, (double) row[1]); pp.setDouble(4, (double) row[2]);
            pp.setDouble(5, (double) row[3]); pp.setDouble(6, (double) row[4]);
            pp.setDouble(7, (double) row[5]); pp.setString(8, (String) row[6]);
            pp.executeUpdate(); pp.close();
        }
    }

    // ── User CRUD ────────────────────────────────────────────────────
    public static boolean register(String username, String email, String password, int age, String gender) {
        try {
            PreparedStatement st = conn.prepareStatement(
                "INSERT INTO users(username,email,password_hash,age,gender) VALUES(?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            st.setString(1, username.toLowerCase());
            st.setString(2, email);
            st.setString(3, hash(password));
            st.setInt(4, age);
            st.setString(5, gender);
            st.executeUpdate();

            ResultSet keys = st.getGeneratedKeys();
            keys.next();
            int uid = keys.getInt(1);
            keys.close(); st.close();

            // Empty profile row
            PreparedStatement hp = conn.prepareStatement(
                "INSERT INTO health_profiles(user_id,weight_kg,height_cm,sugar_level,cholesterol,vitamin_d3,vitamin_b12) VALUES(?,0,0,0,0,0,0)");
            hp.setInt(1, uid);
            hp.executeUpdate(); hp.close();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false; // username taken
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Returns user id if valid, -1 otherwise */
    public static int authenticate(String username, String password) {
        try {
            PreparedStatement st = conn.prepareStatement(
                "SELECT id, password_hash FROM users WHERE username=?");
            st.setString(1, username.toLowerCase());
            ResultSet rs = st.executeQuery();
            if (!rs.next()) { rs.close(); st.close(); return -1; }
            int id = rs.getInt("id");
            String storedHash = rs.getString("password_hash");
            rs.close(); st.close();
            return storedHash.equals(hash(password)) ? id : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static UserStore.User loadUser(int userId) {
        try {
            PreparedStatement st = conn.prepareStatement(
                "SELECT * FROM users WHERE id=?");
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            if (!rs.next()) return null;
            UserStore.User u = new UserStore.User(
                rs.getString("username"), rs.getString("email"),
                rs.getString("password_hash"), rs.getInt("age"), rs.getString("gender"));
            u.id = userId;
            rs.close(); st.close();

            // Load profile
            PreparedStatement sp = conn.prepareStatement(
                "SELECT * FROM health_profiles WHERE user_id=?");
            sp.setInt(1, userId);
            ResultSet rp = sp.executeQuery();
            if (rp.next()) {
                u.profile.weightKg         = rp.getDouble("weight_kg");
                u.profile.heightCm         = rp.getDouble("height_cm");
                u.profile.sugarLevel       = rp.getDouble("sugar_level");
                u.profile.cholesterolLevel = rp.getDouble("cholesterol");
                u.profile.vitaminD3        = rp.getDouble("vitamin_d3");
                u.profile.vitaminB12       = rp.getDouble("vitamin_b12");
            }
            rp.close(); sp.close();

            // Conditions
            PreparedStatement sc = conn.prepareStatement(
                "SELECT condition FROM conditions WHERE user_id=?");
            sc.setInt(1, userId);
            ResultSet rc = sc.executeQuery();
            while (rc.next()) u.profile.conditions.add(rc.getString("condition"));
            rc.close(); sc.close();

            // Progress log
            PreparedStatement sl = conn.prepareStatement(
                "SELECT * FROM progress_log WHERE user_id=? ORDER BY logged_at ASC");
            sl.setInt(1, userId);
            ResultSet rl = sl.executeQuery();
            while (rl.next()) {
                u.profile.progressLog.add(new UserStore.ProgressEntry(
                    rl.getInt("id"),
                    rl.getString("period_label"),
                    rl.getDouble("weight"), rl.getDouble("sugar"),
                    rl.getDouble("cholesterol"), rl.getDouble("vitamin_d3"),
                    rl.getDouble("vitamin_b12"), rl.getString("notes")
                ));
            }
            rl.close(); sl.close();

            return u;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── Profile Save ────────────────────────────────────────────────
    public static void saveHealthProfile(int userId, UserStore.HealthProfile p) {
        try {
            PreparedStatement st = conn.prepareStatement("""
                MERGE INTO health_profiles(user_id,weight_kg,height_cm,sugar_level,
                    cholesterol,vitamin_d3,vitamin_b12,updated_at)
                KEY(user_id) VALUES(?,?,?,?,?,?,?,CURRENT_TIMESTAMP)
            """);
            st.setInt(1, userId);
            st.setDouble(2, p.weightKg); st.setDouble(3, p.heightCm);
            st.setDouble(4, p.sugarLevel); st.setDouble(5, p.cholesterolLevel);
            st.setDouble(6, p.vitaminD3); st.setDouble(7, p.vitaminB12);
            st.executeUpdate(); st.close();

            // Replace conditions
            PreparedStatement del = conn.prepareStatement("DELETE FROM conditions WHERE user_id=?");
            del.setInt(1, userId); del.executeUpdate(); del.close();
            for (String c : p.conditions) {
                PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO conditions(user_id,condition) VALUES(?,?)");
                ins.setInt(1, userId); ins.setString(2, c);
                ins.executeUpdate(); ins.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Progress Log ────────────────────────────────────────────────
    public static void addProgressEntry(int userId, UserStore.ProgressEntry e) {
        try {
            PreparedStatement st = conn.prepareStatement(
                "INSERT INTO progress_log(user_id,period_label,weight,sugar,cholesterol,vitamin_d3,vitamin_b12,notes) VALUES(?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, userId);
            st.setString(2, e.date);
            st.setDouble(3, e.weight); st.setDouble(4, e.sugar);
            st.setDouble(5, e.cholesterol); st.setDouble(6, e.vitaminD3);
            st.setDouble(7, e.vitaminB12); st.setString(8, e.notes);
            st.executeUpdate();
            ResultSet keys = st.getGeneratedKeys();
            if (keys.next()) e.id = keys.getInt(1);
            keys.close(); st.close();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public static void deleteProgressEntry(int entryId) {
        try {
            PreparedStatement st = conn.prepareStatement("DELETE FROM progress_log WHERE id=?");
            st.setInt(1, entryId); st.executeUpdate(); st.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Utility ─────────────────────────────────────────────────────
    static String hash(String s) {
        // SHA-256 hex
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(s.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(s.hashCode());
        }
    }

    public static void close() {
        try { if (conn != null && !conn.isClosed()) conn.close(); }
        catch (Exception ignored) {}
    }
}
