package medbuddy;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class DashboardFrame extends JFrame {

    private JTabbedPane tabs;

    public DashboardFrame() {
        setTitle("MedBuddy — Med Meal");
        setSize(1060, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(Theme.BG);

        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        tabs = new JTabbedPane();
        tabs.setFont(Theme.F_LABEL);
        tabs.setBackground(Theme.BG);
        tabs.setBorder(null);

        tabs.addTab("  Home  ",         buildHomeTab());
        tabs.addTab("  🤖 Smart Diet  ", new SmartDietPanel());
        tabs.addTab("  Diet Plans  ",   new DietPanel());
        tabs.addTab("  Progress  ",     new ProgressPanel());
        tabs.addTab("  Resources  ",    new ResourcesPanel());
        tabs.addTab("  About  ",        buildAboutTab());

        add(tabs, BorderLayout.CENTER);
    }

    // ── Header ───────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.TEAL);
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(loadLogoSmall(32, 32));
        JLabel appName = new JLabel("MED MEAL  ·  Disease-Based Diet Plan");
        appName.setFont(new Font("SansSerif", Font.BOLD, 15));
        appName.setForeground(Color.WHITE);
        left.add(appName);
        header.add(left, BorderLayout.WEST);

        UserStore.User u = UserStore.getCurrentUser();
        String info = u != null ? u.username + "  ·  " + u.gender + ", Age " + u.age : "Guest";

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        right.setOpaque(false);
        JLabel userLbl = new JLabel(info);
        userLbl.setFont(Theme.F_SMALL);
        userLbl.setForeground(new Color(178, 223, 219));
        right.add(userLbl);

        JButton editBtn = Theme.ghostButton("Edit Profile");
        editBtn.setForeground(Color.WHITE);
        editBtn.setBorder(BorderFactory.createLineBorder(new Color(178, 223, 219), 1));
        editBtn.setFont(Theme.F_SMALL);
        editBtn.addActionListener(e -> { dispose(); new HealthSetupWizard().setVisible(true); });
        right.add(editBtn);

        JButton logoutBtn = Theme.ghostButton("Logout");
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBorder(BorderFactory.createLineBorder(new Color(178, 223, 219), 1));
        logoutBtn.setFont(Theme.F_SMALL);
        logoutBtn.addActionListener(e -> { UserStore.logout(); dispose(); new LoginFrame().setVisible(true); });
        right.add(logoutBtn);

        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ── HOME TAB ─────────────────────────────────────────────────────
    private JPanel buildHomeTab() {
        JPanel p = new JPanel();
        p.setBackground(Theme.BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(Theme.PAD, Theme.PAD + 4, Theme.PAD, Theme.PAD + 4));

        UserStore.User u = UserStore.getCurrentUser();

        // Greeting
        JLabel welcome = new JLabel("Hello, " + (u != null ? u.username : "User") + " 👋");
        welcome.setFont(Theme.F_TITLE);
        welcome.setForeground(Theme.TEXT_PRIMARY);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(welcome);

        p.add(Box.createVerticalStrut(4));
        JLabel sub = Theme.caption("Here is a summary of your health metrics and conditions.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(sub);

        p.add(Box.createVerticalStrut(Theme.PAD));
        p.add(Theme.separator());
        p.add(Box.createVerticalStrut(Theme.PAD));

        // Metrics summary table
        if (u != null) {
            JLabel metricsTitle = Theme.heading("Health Metrics");
            metricsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(metricsTitle);
            p.add(Box.createVerticalStrut(Theme.GAP));

            double bmi = u.profile.heightCm > 0
                ? u.profile.weightKg / Math.pow(u.profile.heightCm / 100.0, 2) : 0;

            String[][] data = {
                {"Metric", "Your Value", "Normal Range", "Status"},
                {"Weight",            fmt(u.profile.weightKg, "kg"),      "—",                bmiWeight(bmi)},
                {"Height",            fmt(u.profile.heightCm, "cm"),      "—",                "—"},
                {"BMI",               bmi > 0 ? String.format("%.1f", bmi) : "N/A",
                                                                          "18.5 – 24.9",      bmiStatus(bmi)},
                {"Blood Sugar (Fasting)", fmt(u.profile.sugarLevel, "mg/dL"),
                                                                          "70 – 99 mg/dL",    sugarStatus(u.profile.sugarLevel)},
                {"Cholesterol",       fmt(u.profile.cholesterolLevel, "mg/dL"),
                                                                          "< 200 mg/dL",      cholStatus(u.profile.cholesterolLevel)},
                {"Vitamin D3",        fmt(u.profile.vitaminD3, "ng/mL"), "30 – 100 ng/mL",   vitD3Status(u.profile.vitaminD3)},
                {"Vitamin B12",       fmt(u.profile.vitaminB12, "pg/mL"),"200 – 900 pg/mL",  vitB12Status(u.profile.vitaminB12)},
            };

            JTable table = new JTable(data, data[0]) {
                public boolean isCellEditable(int r, int c) { return false; }
                public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
                    Component c = super.prepareRenderer(renderer, row, col);
                    c.setBackground(row % 2 == 0 ? Theme.SURFACE : new Color(245, 245, 245));
                    if (row > 0 && col == 3) {
                        String val = (String) getValueAt(row, col);
                        if ("Normal".equals(val) || "—".equals(val)) c.setForeground(Theme.GREEN_OK);
                        else if (val.contains("High") || val.contains("Low") || val.contains("Obese") || val.contains("Deficient"))
                            c.setForeground(Theme.RED_ERR);
                        else if (val.contains("Borderline") || val.contains("Pre"))
                            c.setForeground(Theme.AMBER_WARN);
                        else c.setForeground(Theme.TEXT_PRIMARY);
                    } else {
                        c.setForeground(Theme.TEXT_PRIMARY);
                    }
                    return c;
                }
            };
            table.setFont(Theme.F_BODY);
            table.setRowHeight(28);
            table.setGridColor(Theme.BORDER);
            table.setShowVerticalLines(false);
            table.getTableHeader().setFont(Theme.F_LABEL);
            table.getTableHeader().setBackground(Theme.TEAL);
            table.getTableHeader().setForeground(Color.WHITE);
            table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
            table.setIntercellSpacing(new Dimension(0, 1));

            JScrollPane tableScroll = Theme.scroll(table);
            tableScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
            tableScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
            p.add(tableScroll);
            p.add(Box.createVerticalStrut(Theme.PAD));
        }

        // Conditions chips
        if (u != null && !u.profile.conditions.isEmpty()) {
            p.add(Theme.separator());
            p.add(Box.createVerticalStrut(Theme.PAD));
            JLabel condTitle = Theme.heading("Your Conditions");
            condTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(condTitle);
            p.add(Box.createVerticalStrut(Theme.GAP));

            JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            chipRow.setOpaque(false);
            chipRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            String[] allDiseases = DietData.getAllDiseases();
            for (String cond : u.profile.conditions) {
                DietData.DietPlan plan = DietData.getPlan(cond);
                String icon = plan != null ? plan.icon + "  " : "";
                Color accent = accentColor(cond, allDiseases);
                JButton chip = new JButton(icon + cond);
                chip.setFont(Theme.F_LABEL);
                chip.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 20));
                chip.setForeground(accent.darker());
                chip.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accent, 1),
                    BorderFactory.createEmptyBorder(4, 12, 4, 12)
                ));
                chip.setFocusPainted(false);
                chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                chip.addActionListener(e -> tabs.setSelectedIndex(1));
                chipRow.add(chip);
            }
            p.add(chipRow);

            java.util.List<String> warnings = DietData.getCrossWarnings(u.profile.conditions);
            if (!warnings.isEmpty()) {
                p.add(Box.createVerticalStrut(Theme.GAP));
                JPanel warnCard = new JPanel();
                warnCard.setLayout(new BoxLayout(warnCard, BoxLayout.Y_AXIS));
                warnCard.setBackground(new Color(255, 248, 220));
                warnCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 183, 77), 1),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));
                warnCard.setAlignmentX(Component.LEFT_ALIGNMENT);
                warnCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, warnings.size() * 22 + 24));
                JLabel wHead = new JLabel("⚠  Cross-Condition Diet Conflicts");
                wHead.setFont(Theme.F_LABEL);
                wHead.setForeground(Theme.AMBER_WARN);
                warnCard.add(wHead);
                warnCard.add(Box.createVerticalStrut(6));
                for (String w : warnings) {
                    JLabel wl = new JLabel(w);
                    wl.setFont(Theme.F_SMALL);
                    wl.setForeground(new Color(100, 60, 0));
                    warnCard.add(wl);
                }
                p.add(warnCard);
            }
        }

        p.add(Box.createVerticalStrut(Theme.PAD));
        p.add(Theme.separator());
        p.add(Box.createVerticalStrut(Theme.GAP));

        JPanel quickBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.GAP, 0));
        quickBtns.setOpaque(false);
        quickBtns.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton dietBtn = Theme.primaryButton("🤖 Smart Diet Plan");
        JButton progBtn = Theme.ghostButton("Log Progress");
        dietBtn.addActionListener(e -> tabs.setSelectedIndex(1));
        progBtn.addActionListener(e -> tabs.setSelectedIndex(2));
        quickBtns.add(dietBtn);
        quickBtns.add(progBtn);
        p.add(quickBtns);

        JScrollPane scroll = Theme.scroll(p);
        scroll.setBorder(null);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BG);
        wrapper.add(scroll);
        return wrapper;
    }

    // ── ABOUT TAB ────────────────────────────────────────────────────
    private JPanel buildAboutTab() {
        JPanel p = new JPanel();
        p.setBackground(Theme.BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(Theme.PAD, Theme.PAD + 4, Theme.PAD, Theme.PAD + 4));

        JLabel title = Theme.heading("About MedBuddy — Med Meal");
        title.setFont(Theme.F_TITLE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(title);

        p.add(Box.createVerticalStrut(4));
        JLabel ver = Theme.caption("Version 4.0  ·  Disease-Based Diet Recommendation System  ·  H2 Database");
        ver.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(ver);

        p.add(Box.createVerticalStrut(Theme.PAD));

        JTextArea desc = new JTextArea(
            "PROJECT OVERVIEW\n" +
            "MedBuddy (Med Meal) is a Java Swing desktop application providing personalised\n" +
            "dietary recommendations for 8 chronic conditions: Diabetes, PCOS, Thyroid,\n" +
            "Heart Problems, Alzheimer's, Vitamin D3 & B12 Deficiency, High Cholesterol.\n\n" +
            "DATABASE (v4)\n" +
            "  Engine  : H2 Embedded (JDBC, pure Java, no install required)\n" +
            "  File    : ~/.medbuddy/medbuddy.mv.db  (created automatically)\n" +
            "  Tables  : users · health_profiles · conditions · progress_log\n" +
            "  Security: SHA-256 password hashing\n\n" +
            "ARCHITECTURE\n" +
            "  UI      : Java Swing (Theme.java design tokens)\n" +
            "  Data    : DatabaseManager.java — all SQL in one place\n" +
            "  Session : UserStore.java — in-memory after login\n\n" +
            "HOW TO USE\n" +
            "  1. Register or use demo/demo123\n" +
            "  2. Complete the 3-step health profile setup\n" +
            "  3. View your personalised diet plan under 'Diet Plans'\n" +
            "  4. Log weekly metrics under 'Progress' — they are saved to the DB\n" +
            "  5. Use 'Resources' for verified medical references\n\n" +
            "DISCLAIMER\n" +
            "This tool is for educational purposes only. Always consult a qualified\n" +
            "doctor or dietitian before making dietary changes."
        );
        desc.setFont(Theme.F_MONO);
        desc.setEditable(false);
        desc.setBackground(new Color(245, 245, 245));
        desc.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane descScroll = Theme.scroll(desc);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(descScroll);

        JScrollPane scroll = Theme.scroll(p);
        scroll.setBorder(null);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BG);
        wrapper.add(scroll);
        return wrapper;
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private String fmt(double v, String unit) {
        if (v <= 0) return "Not set";
        return (v == Math.floor(v) ? String.valueOf((int) v) : String.format("%.1f", v)) + " " + unit;
    }
    private String bmiWeight(double bmi) {
        if (bmi <= 0) return "—";
        return bmi < 18.5 ? "Underweight" : bmi < 25 ? "Normal" : bmi < 30 ? "Overweight" : "Obese";
    }
    private String bmiStatus(double bmi) { return bmi <= 0 ? "—" : bmiWeight(bmi); }
    private String sugarStatus(double v) {
        if (v <= 0) return "—";
        if (v < 100) return "Normal"; if (v < 126) return "Pre-diabetic"; return "High";
    }
    private String cholStatus(double v) {
        if (v <= 0) return "—";
        if (v < 200) return "Normal"; if (v < 240) return "Borderline High"; return "High";
    }
    private String vitD3Status(double v) {
        if (v <= 0) return "—";
        if (v < 12) return "Severely Deficient"; if (v < 20) return "Deficient";
        if (v < 30) return "Insufficient"; return "Normal";
    }
    private String vitB12Status(double v) {
        if (v <= 0) return "—"; if (v < 150) return "Deficient"; if (v < 200) return "Low"; return "Normal";
    }

    private Color accentColor(String disease, String[] all) {
        Color[] colors = {
            new Color(200, 80, 20), new Color(130, 30, 150), new Color(20, 90, 180),
            new Color(180, 20, 20), new Color(60, 20, 150), new Color(180, 120, 0),
            new Color(0, 110, 100), new Color(160, 30, 30)
        };
        for (int i = 0; i < all.length; i++)
            if (all[i].equals(disease)) return colors[i % colors.length];
        return Theme.TEAL;
    }

    private JLabel loadLogoSmall(int w, int h) {
        try {
            File f = new File("logo.png");
            if (!f.exists()) f = new File("../logo.png");
            if (f.exists()) {
                BufferedImage img = ImageIO.read(f);
                return new JLabel(new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH)));
            }
        } catch (Exception ignored) {}
        JLabel l = new JLabel("M");
        l.setFont(new Font("SansSerif", Font.BOLD, 18));
        l.setForeground(Color.WHITE);
        return l;
    }
}
