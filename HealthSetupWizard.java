package medbuddy;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class HealthSetupWizard extends JFrame {

    private JPanel mainPanel;
    private CardLayout cards = new CardLayout();
    private int numConditions = 1;
    private List<String> selectedConditions = new ArrayList<>();
    private JCheckBox[] condBoxes;

    private JTextField weightF, heightF, sugarF, cholF, vitD3F, vitB12F;

    public HealthSetupWizard() {
        setTitle("MedBuddy — Health Profile Setup");
        setSize(660, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(Theme.BG);

        mainPanel = new JPanel(cards);
        mainPanel.setBackground(Theme.BG);
        mainPanel.add(buildStep1(), "STEP1");
        mainPanel.add(buildStep2(), "STEP2");
        mainPanel.add(buildStep3(), "STEP3");
        add(mainPanel);
    }

    // ── Step 1: How many conditions ──────────────────────────────────
    private JPanel buildStep1() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG);

        p.add(stepHeader("Step 1 of 3  —  How Many Conditions?"), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setBackground(Theme.SURFACE);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(36, 32, 24, 32));

        JLabel q = Theme.heading("How many health conditions do you currently have?");
        q.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(q);

        center.add(Box.createVerticalStrut(8));
        JLabel note = Theme.caption("For multiple conditions, we show a combined diet plan and highlight any conflicts.");
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(note);

        center.add(Box.createVerticalStrut(28));

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        radioPanel.setOpaque(false);
        radioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ButtonGroup bg = new ButtonGroup();
        String[] opts = {"1", "2", "3", "4 or more"};
        JRadioButton[] radios = new JRadioButton[opts.length];
        for (int i = 0; i < opts.length; i++) {
            JRadioButton rb = new JRadioButton(opts[i]);
            rb.setFont(Theme.F_BODY);
            rb.setOpaque(false);
            if (i == 0) rb.setSelected(true);
            bg.add(rb);
            radioPanel.add(rb);
            radios[i] = rb;
        }
        center.add(radioPanel);
        center.add(Box.createVerticalGlue());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        JButton next = Theme.primaryButton("Next →");
        next.addActionListener(e -> {
            numConditions = 1;
            if (radios[1].isSelected()) numConditions = 2;
            else if (radios[2].isSelected()) numConditions = 3;
            else if (radios[3].isSelected()) numConditions = 4;
            mainPanel.remove(mainPanel.getComponent(1));
            mainPanel.add(buildStep2(), "STEP2", 1);
            cards.show(mainPanel, "STEP2");
        });
        btnRow.add(next);
        center.add(btnRow);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    // ── Step 2: Pick conditions ───────────────────────────────────────
    private JPanel buildStep2() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG);
        p.add(stepHeader("Step 2 of 3  —  Select Your Conditions"), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setBackground(Theme.SURFACE);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(24, 32, 16, 32));

        JLabel heading = Theme.heading("Select " + numConditions + " condition" + (numConditions > 1 ? "s" : "") + ":");
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(heading);
        center.add(Box.createVerticalStrut(Theme.GAP));

        String[] diseases = DietData.getAllDiseases();
        condBoxes = new JCheckBox[diseases.length];
        JPanel cbGrid = new JPanel(new GridLayout(0, 2, Theme.GAP, Theme.GAP_S));
        cbGrid.setOpaque(false);
        cbGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < diseases.length; i++) {
            DietData.DietPlan plan = DietData.getPlan(diseases[i]);
            String icon = plan != null ? plan.icon + "  " : "";
            condBoxes[i] = new JCheckBox(icon + diseases[i]);
            condBoxes[i].setFont(Theme.F_BODY);
            condBoxes[i].setOpaque(false);
            cbGrid.add(condBoxes[i]);
        }
        center.add(cbGrid);
        center.add(Box.createVerticalStrut(Theme.GAP));

        JLabel errLbl = new JLabel(" ");
        errLbl.setFont(Theme.F_SMALL);
        errLbl.setForeground(Theme.RED_ERR);
        errLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(errLbl);

        center.add(Box.createVerticalGlue());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.GAP, 0));
        btnRow.setOpaque(false);
        JButton back = Theme.ghostButton("← Back");
        JButton next = Theme.primaryButton("Next →");
        back.addActionListener(e -> cards.show(mainPanel, "STEP1"));
        next.addActionListener(e -> {
            selectedConditions.clear();
            for (JCheckBox cb : condBoxes) if (cb.isSelected()) selectedConditions.add(
                cb.getText().replaceAll("^[^a-zA-Z]+", "").trim()
            );
            if (selectedConditions.isEmpty()) { errLbl.setText("Please select at least one condition."); return; }
            if (selectedConditions.size() != numConditions) {
                errLbl.setText("Please select exactly " + numConditions + " condition(s)."); return;
            }
            cards.show(mainPanel, "STEP3");
        });
        btnRow.add(back);
        btnRow.add(next);
        center.add(btnRow);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    // ── Step 3: Metrics ───────────────────────────────────────────────
    private JPanel buildStep3() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG);
        p.add(stepHeader("Step 3 of 3  —  Enter Your Health Metrics"), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setBackground(Theme.SURFACE);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(20, 32, 16, 32));

        JLabel heading = Theme.heading("Enter your current health values:");
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(heading);
        center.add(Box.createVerticalStrut(4));
        JLabel sub = Theme.caption("Leave blank if unknown — you can update these later.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(sub);
        center.add(Box.createVerticalStrut(Theme.GAP));

        weightF = Theme.textField(); heightF = Theme.textField();
        sugarF  = Theme.textField(); cholF   = Theme.textField();
        vitD3F  = Theme.textField(); vitB12F = Theme.textField();

        // Pre-fill from existing profile
        UserStore.User u = UserStore.getCurrentUser();
        if (u != null) {
            if (u.profile.weightKg > 0)         weightF.setText(String.valueOf(u.profile.weightKg));
            if (u.profile.heightCm > 0)         heightF.setText(String.valueOf(u.profile.heightCm));
            if (u.profile.sugarLevel > 0)       sugarF.setText(String.valueOf(u.profile.sugarLevel));
            if (u.profile.cholesterolLevel > 0) cholF.setText(String.valueOf(u.profile.cholesterolLevel));
            if (u.profile.vitaminD3 > 0)        vitD3F.setText(String.valueOf(u.profile.vitaminD3));
            if (u.profile.vitaminB12 > 0)       vitB12F.setText(String.valueOf(u.profile.vitaminB12));
        }

        JPanel grid = new JPanel(new GridLayout(3, 2, Theme.GAP, Theme.GAP_S));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        grid.add(metricField("Weight (kg)",          "e.g. 65",  weightF));
        grid.add(metricField("Height (cm)",          "e.g. 162", heightF));
        grid.add(metricField("Blood Sugar (mg/dL)",  "e.g. 110", sugarF));
        grid.add(metricField("Cholesterol (mg/dL)",  "e.g. 195", cholF));
        grid.add(metricField("Vitamin D3 (ng/mL)",   "e.g. 18",  vitD3F));
        grid.add(metricField("Vitamin B12 (pg/mL)",  "e.g. 180", vitB12F));
        center.add(grid);

        center.add(Box.createVerticalStrut(Theme.GAP));
        JLabel errLbl = new JLabel(" ");
        errLbl.setFont(Theme.F_SMALL);
        errLbl.setForeground(Theme.RED_ERR);
        errLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(errLbl);

        center.add(Box.createVerticalGlue());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.GAP, 0));
        btnRow.setOpaque(false);
        JButton back = Theme.ghostButton("← Back");
        JButton finish = Theme.primaryButton("Save & Go to Dashboard →");
        back.addActionListener(e -> cards.show(mainPanel, "STEP2"));
        finish.addActionListener(e -> {
            UserStore.HealthProfile profile = new UserStore.HealthProfile();
            try {
                profile.weightKg         = parseOpt(weightF);
                profile.heightCm         = parseOpt(heightF);
                profile.sugarLevel       = parseOpt(sugarF);
                profile.cholesterolLevel = parseOpt(cholF);
                profile.vitaminD3        = parseOpt(vitD3F);
                profile.vitaminB12       = parseOpt(vitB12F);
            } catch (NumberFormatException ex) {
                errLbl.setText("Please enter valid numeric values for all filled fields.");
                return;
            }
            profile.conditions.addAll(selectedConditions);
            // Preserve existing progress log
            if (UserStore.getCurrentUser() != null) {
                profile.progressLog.addAll(UserStore.getCurrentUser().profile.progressLog);
            }
            UserStore.saveHealthProfile(profile);
            dispose();
            new DashboardFrame().setVisible(true);
        });
        btnRow.add(back);
        btnRow.add(finish);
        center.add(btnRow);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private JPanel stepHeader(String text) {
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.PAD, 10));
        hdr.setBackground(Theme.TEAL);
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 14));
        l.setForeground(Color.WHITE);
        hdr.add(l);
        return hdr;
    }

    private JPanel metricField(String label, String hint, JTextField field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = Theme.fieldLabel(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        // placeholder hint as tooltip
        field.setToolTipText(hint);
        p.add(l);
        p.add(Box.createVerticalStrut(3));
        p.add(field);
        return p;
    }

    private double parseOpt(JTextField f) {
        String s = f.getText().trim();
        if (s.isEmpty()) return 0;
        return Double.parseDouble(s);
    }
}
