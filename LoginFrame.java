package medbuddy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class LoginFrame extends JFrame {

    private JPanel     cards;
    private CardLayout cardLayout = new CardLayout();

    public LoginFrame() {
        setTitle("MedBuddy — Med Meal");
        setSize(820, 540);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Theme.BG);

        cards = new JPanel(cardLayout);
        cards.setBackground(Theme.BG);
        cards.add(buildLoginPanel(),    "LOGIN");
        cards.add(buildRegisterPanel(), "REGISTER");
        add(cards);
    }

    // ── Login panel ──────────────────────────────────────────────────
    private JPanel buildLoginPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);

        // Left teal sidebar
        JPanel left = buildSidebar(300);
        root.add(left, BorderLayout.WEST);

        // Right form
        JPanel right = new JPanel();
        right.setBackground(Theme.SURFACE);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createEmptyBorder(60, 50, 50, 50));

        JLabel title = new JLabel("Sign In");
        title.setFont(Theme.F_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(title);

        right.add(Box.createVerticalStrut(4));
        JLabel sub = Theme.caption("Welcome back — enter your credentials.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(sub);

        right.add(Box.createVerticalStrut(32));

        // Fields
        JTextField    userField = Theme.textField();
        JPasswordField passField = Theme.passField();
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        right.add(fieldBlock("Username", userField));
        right.add(Box.createVerticalStrut(Theme.GAP));
        right.add(fieldBlock("Password", passField));
        right.add(Box.createVerticalStrut(Theme.GAP_S));

        JLabel errorLbl = new JLabel(" ");
        errorLbl.setFont(Theme.F_SMALL);
        errorLbl.setForeground(Theme.RED_ERR);
        errorLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(errorLbl);

        right.add(Box.createVerticalStrut(Theme.GAP));

        JButton loginBtn = Theme.primaryButton("Sign In");
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        right.add(loginBtn);

        right.add(Box.createVerticalStrut(20));

        JPanel switchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        switchRow.setOpaque(false);
        switchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel noAcc = new JLabel("Don't have an account? ");
        noAcc.setFont(Theme.F_SMALL);
        noAcc.setForeground(Theme.TEXT_SECONDARY);
        JButton regLink = Theme.linkButton("Register here");
        switchRow.add(noAcc);
        switchRow.add(regLink);
        right.add(switchRow);

        right.add(Box.createVerticalStrut(8));
        JLabel demoHint = Theme.caption("Demo: username=demo  password=demo123");
        demoHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(demoHint);

        root.add(right, BorderLayout.CENTER);

        // Actions
        Runnable doLogin = () -> {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            if (u.isEmpty() || p.isEmpty()) { errorLbl.setText("Please fill in all fields."); return; }
            if (UserStore.login(u, p)) {
                UserStore.User user = UserStore.getCurrentUser();
                dispose();
                if (user.profile.conditions.isEmpty()) new HealthSetupWizard().setVisible(true);
                else                                   new DashboardFrame().setVisible(true);
            } else {
                errorLbl.setText("Wrong username or password.");
                passField.setText("");
            }
        };

        loginBtn.addActionListener(e -> doLogin.run());
        userField.addActionListener(e -> doLogin.run());
        passField.addActionListener(e -> doLogin.run());
        regLink.addActionListener(e -> cardLayout.show(cards, "REGISTER"));

        return root;
    }

    // ── Register panel ───────────────────────────────────────────────
    private JPanel buildRegisterPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);

        JPanel left = buildSidebar(220);
        root.add(left, BorderLayout.WEST);

        JPanel right = new JPanel();
        right.setBackground(Theme.SURFACE);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createEmptyBorder(36, 46, 36, 46));

        JLabel title = new JLabel("Create Account");
        title.setFont(Theme.F_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(title);

        right.add(Box.createVerticalStrut(4));
        JLabel sub = Theme.caption("After registering you will set up your health profile.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(sub);

        right.add(Box.createVerticalStrut(24));

        JTextField    uf  = Theme.textField();
        JTextField    ef  = Theme.textField();
        JPasswordField pf  = Theme.passField();
        JPasswordField p2f = Theme.passField();
        JTextField    af  = Theme.textField();

        String[] gList = {"Select Gender", "Male", "Female", "Other"};
        JComboBox<String> gBox = new JComboBox<>(gList);
        gBox.setFont(Theme.F_BODY);
        gBox.setBackground(Theme.SURFACE);
        gBox.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));

        // 2-column grid
        JPanel grid = new JPanel(new GridLayout(3, 2, Theme.GAP, Theme.GAP_S));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        grid.add(fieldBlock("Username *", uf));
        grid.add(fieldBlock("Email *", ef));
        grid.add(fieldBlock("Password *", pf));
        grid.add(fieldBlock("Confirm Password *", p2f));
        grid.add(fieldBlock("Age *", af));
        grid.add(fieldBlock("Gender *", gBox));

        right.add(grid);
        right.add(Box.createVerticalStrut(Theme.GAP));

        JLabel errLbl = new JLabel(" ");
        errLbl.setFont(Theme.F_SMALL);
        errLbl.setForeground(Theme.RED_ERR);
        errLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(errLbl);

        right.add(Box.createVerticalStrut(Theme.GAP_S));

        JButton regBtn = Theme.primaryButton("Register & Continue →");
        regBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        regBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        right.add(regBtn);

        right.add(Box.createVerticalStrut(14));
        JPanel switchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        switchRow.setOpaque(false);
        switchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel hasAcc = new JLabel("Already have an account? ");
        hasAcc.setFont(Theme.F_SMALL);
        hasAcc.setForeground(Theme.TEXT_SECONDARY);
        JButton loginLink = Theme.linkButton("Sign in");
        switchRow.add(hasAcc);
        switchRow.add(loginLink);
        right.add(switchRow);

        root.add(right, BorderLayout.CENTER);

        regBtn.addActionListener(e -> {
            String u   = uf.getText().trim(), em = ef.getText().trim();
            String pw  = new String(pf.getPassword()), pw2 = new String(p2f.getPassword());
            String ageS = af.getText().trim();
            String gen = (String) gBox.getSelectedItem();

            if (u.isEmpty() || em.isEmpty() || pw.isEmpty()) { errLbl.setText("Please fill all required fields."); return; }
            if (u.length() < 3)           { errLbl.setText("Username must be ≥ 3 characters."); return; }
            if (!em.contains("@"))        { errLbl.setText("Enter a valid email address."); return; }
            if (pw.length() < 6)          { errLbl.setText("Password must be ≥ 6 characters."); return; }
            if (!pw.equals(pw2))          { errLbl.setText("Passwords do not match."); return; }
            int age = 0;
            try { age = Integer.parseInt(ageS); } catch (Exception ex) {}
            if (age < 1 || age > 120)     { errLbl.setText("Enter a valid age (1–120)."); return; }
            if ("Select Gender".equals(gen)) { errLbl.setText("Please select your gender."); return; }

            if (UserStore.register(u, em, pw, age, gen)) {
                UserStore.login(u, pw);
                dispose();
                new HealthSetupWizard().setVisible(true);
            } else {
                errLbl.setText("Username already taken. Please choose another.");
            }
        });

        loginLink.addActionListener(e -> cardLayout.show(cards, "LOGIN"));
        return root;
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private JPanel buildSidebar(int width) {
        JPanel left = new JPanel();
        left.setBackground(Theme.TEAL);
        left.setPreferredSize(new Dimension(width, 540));
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(50, 28, 40, 28));

        JLabel logoLbl = loadLogo(80, 80);
        logoLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        left.add(logoLbl);
        left.add(Box.createVerticalStrut(16));

        JLabel appName = new JLabel("MED MEAL");
        appName.setFont(new Font("SansSerif", Font.BOLD, 22));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);
        left.add(appName);

        JLabel tagline = new JLabel("Disease-Based Diet Plan");
        tagline.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tagline.setForeground(new Color(178, 223, 219));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        left.add(tagline);

        left.add(Box.createVerticalStrut(28));
        left.add(Theme.separator());
        left.add(Box.createVerticalStrut(20));

        String[] features = {"Diabetes & PCOS", "Thyroid & Heart", "Alzheimer's",
                             "Vitamin D3 & B12", "High Cholesterol", "Progress Tracker"};
        for (String f : features) {
            JLabel fl = new JLabel("· " + f);
            fl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            fl.setForeground(new Color(178, 223, 219));
            fl.setAlignmentX(Component.LEFT_ALIGNMENT);
            left.add(fl);
            left.add(Box.createVerticalStrut(6));
        }
        return left;
    }

    /** Stacked label + field block */
    private JPanel fieldBlock(String labelText, JComponent field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = Theme.fieldLabel(labelText);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(l);
        p.add(Box.createVerticalStrut(3));
        p.add(field);
        return p;
    }

    private JLabel loadLogo(int w, int h) {
        try {
            File f = new File("logo.png");
            if (!f.exists()) f = new File("../logo.png");
            if (f.exists()) {
                BufferedImage img = ImageIO.read(f);
                return new JLabel(new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH)));
            }
        } catch (Exception ignored) {}
        JLabel l = new JLabel("M");
        l.setFont(new Font("SansSerif", Font.BOLD, 36));
        l.setForeground(Color.WHITE);
        return l;
    }
}
