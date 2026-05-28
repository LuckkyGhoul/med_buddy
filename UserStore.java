package medbuddy;

import java.util.*;

/**
 * In-memory session layer on top of DatabaseManager.
 * All mutations are immediately persisted to H2.
 */
public class UserStore {

    // ── Domain objects ───────────────────────────────────────────────
    public static class HealthProfile {
        public double weightKg, heightCm, sugarLevel, cholesterolLevel, vitaminD3, vitaminB12;
        public List<String>        conditions  = new ArrayList<>();
        public List<ProgressEntry> progressLog = new ArrayList<>();
    }

    public static class ProgressEntry {
        public int    id;   // DB primary key (0 = not yet saved)
        public String date;
        public double weight, sugar, cholesterol, vitaminD3, vitaminB12;
        public String notes;

        public ProgressEntry(String date, double weight, double sugar,
                             double cholesterol, double vitD3, double vitB12, String notes) {
            this(0, date, weight, sugar, cholesterol, vitD3, vitB12, notes);
        }

        public ProgressEntry(int id, String date, double weight, double sugar,
                             double cholesterol, double vitD3, double vitB12, String notes) {
            this.id = id; this.date = date; this.weight = weight; this.sugar = sugar;
            this.cholesterol = cholesterol; this.vitaminD3 = vitD3;
            this.vitaminB12 = vitB12; this.notes = notes;
        }
    }

    public static class User {
        public int    id;
        public String username, email, passwordHash, gender;
        public int    age;
        public HealthProfile profile = new HealthProfile();

        public User(String username, String email, String passwordHash, int age, String gender) {
            this.username = username; this.email = email;
            this.passwordHash = passwordHash; this.age = age; this.gender = gender;
        }
    }

    // ── Session ──────────────────────────────────────────────────────
    private static User currentUser = null;

    public static boolean register(String username, String email, String password, int age, String gender) {
        return DatabaseManager.register(username, email, password, age, gender);
    }

    public static boolean login(String username, String password) {
        int uid = DatabaseManager.authenticate(username, password);
        if (uid < 0) return false;
        currentUser = DatabaseManager.loadUser(uid);
        return currentUser != null;
    }

    public static void logout() { currentUser = null; }

    public static User getCurrentUser() { return currentUser; }

    public static void saveHealthProfile(HealthProfile p) {
        if (currentUser == null) return;
        currentUser.profile = p;
        DatabaseManager.saveHealthProfile(currentUser.id, p);
    }

    public static void addProgressEntry(ProgressEntry e) {
        if (currentUser == null) return;
        DatabaseManager.addProgressEntry(currentUser.id, e);
        currentUser.profile.progressLog.add(e);
    }

    public static void deleteProgressEntry(ProgressEntry e) {
        if (currentUser == null) return;
        DatabaseManager.deleteProgressEntry(e.id);
        currentUser.profile.progressLog.remove(e);
    }
}
