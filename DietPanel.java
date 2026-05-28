package medbuddy;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class DietPanel extends JPanel {

    public DietPanel() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        UserStore.User u = UserStore.getCurrentUser();
        List<String> conditions = u != null ? u.profile.conditions : java.util.Collections.emptyList();

        // Header
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.PAD, 10));
        hdr.setBackground(Theme.TEAL);
        JLabel title = new JLabel("My Diet Plans");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        hdr.add(title);
        if (!conditions.isEmpty()) {
            JLabel sub = new JLabel("  Based on: " + String.join(", ", conditions));
            sub.setFont(Theme.F_SMALL);
            sub.setForeground(new Color(178, 223, 219));
            hdr.add(sub);
        }
        add(hdr, BorderLayout.NORTH);

        if (conditions.isEmpty()) {
            JLabel noData = new JLabel("No conditions set. Please edit your health profile.", SwingConstants.CENTER);
            noData.setFont(Theme.F_BODY);
            noData.setForeground(Theme.TEXT_SECONDARY);
            add(noData, BorderLayout.CENTER);
            return;
        }

        // Tabs per condition
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.F_LABEL);
        tabs.setBackground(Theme.BG);

        for (String cond : conditions) {
            DietData.DietPlan plan = DietData.getPlan(cond);
            if (plan != null) tabs.addTab("  " + plan.icon + " " + cond + "  ", buildPlanPanel(plan));
        }

        // Cross-condition note
        List<String> warnings = DietData.getCrossWarnings(conditions);
        if (!warnings.isEmpty()) {
            JPanel warnPanel = buildWarningsPanel(warnings);
            tabs.addTab("  ⚠ Conflicts  ", warnPanel);
        }

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildPlanPanel(DietData.DietPlan plan) {
        JPanel p = new JPanel();
        p.setBackground(Theme.BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(Theme.PAD, Theme.PAD + 4, Theme.PAD, Theme.PAD + 4));

        // Title row
        JLabel titleLbl = new JLabel(plan.icon + "  " + plan.disease);
        titleLbl.setFont(Theme.F_TITLE);
        titleLbl.setForeground(Theme.TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(titleLbl);
        p.add(Box.createVerticalStrut(4));
        JLabel desc = Theme.caption(plan.tagline);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(desc);
        p.add(Box.createVerticalStrut(Theme.PAD));
        p.add(Theme.separator());
        p.add(Box.createVerticalStrut(Theme.GAP));

        // Raw foods row
        if (plan.rawFoods != null && plan.rawFoods.length > 0) {
            JLabel rawTitle = Theme.heading("Recommended Raw / Whole Foods");
            rawTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(rawTitle);
            p.add(Box.createVerticalStrut(Theme.GAP_S));
            JPanel rawRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
            rawRow.setOpaque(false);
            rawRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (String f : plan.rawFoods) {
                JLabel chip = new JLabel(f);
                chip.setFont(Theme.F_SMALL);
                chip.setForeground(Theme.TEAL_DARK);
                chip.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.TEAL, 1),
                    BorderFactory.createEmptyBorder(2, 8, 2, 8)
                ));
                chip.setOpaque(true);
                chip.setBackground(Theme.TEAL_LIGHT);
                rawRow.add(chip);
            }
            p.add(rawRow);
            p.add(Box.createVerticalStrut(Theme.PAD));
        }

        // Meals section
        JLabel mealsTitle = Theme.heading("Sample Meal Plan");
        mealsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(mealsTitle);
        p.add(Box.createVerticalStrut(Theme.GAP_S));
        addMealRow(p, "Breakfast", plan.breakfast);
        addMealRow(p, "Lunch", plan.lunch);
        addMealRow(p, "Dinner", plan.dinner);
        addMealRow(p, "Snacks", plan.snacks);
        p.add(Box.createVerticalStrut(Theme.PAD));
        p.add(Theme.separator());
        p.add(Box.createVerticalStrut(Theme.GAP));

        // Two-column: veg meals + foods to avoid
        JPanel cols = new JPanel(new GridLayout(1, 2, Theme.PAD, 0));
        cols.setOpaque(false);
        cols.setAlignmentX(Component.LEFT_ALIGNMENT);
        cols.add(foodList("✓ Vegetarian Meals", plan.vegMeals != null ? plan.vegMeals : new String[0], Theme.GREEN_OK, new Color(232, 245, 233)));
        cols.add(foodList("✗ Foods to Avoid", plan.avoid != null ? plan.avoid : new String[0], Theme.RED_ERR, new Color(255, 235, 238)));
        p.add(cols);

        p.add(Box.createVerticalStrut(Theme.PAD));
        p.add(Theme.separator());
        p.add(Box.createVerticalStrut(Theme.GAP));

        // Tips
        if (plan.tips != null && plan.tips.length > 0) {
            JLabel tipsTitle = Theme.heading("Lifestyle Tips");
            tipsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(tipsTitle);
            p.add(Box.createVerticalStrut(Theme.GAP_S));
            for (String tip : plan.tips) {
                JLabel tl = new JLabel("·  " + tip);
                tl.setFont(Theme.F_BODY);
                tl.setForeground(Theme.TEXT_PRIMARY);
                tl.setAlignmentX(Component.LEFT_ALIGNMENT);
                p.add(tl);
                p.add(Box.createVerticalStrut(4));
            }
        }

        JScrollPane scroll = Theme.scroll(p);
        scroll.setBorder(null);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BG);
        wrapper.add(scroll);
        return wrapper;
    }

    private void addMealRow(JPanel p, String mealName, String[] items) {
        if (items == null || items.length == 0) return;
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(mealName + ":  ");
        lbl.setFont(Theme.F_LABEL);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        row.add(lbl);
        JLabel val = new JLabel(String.join("  |  ", items));
        val.setFont(Theme.F_BODY);
        val.setForeground(Theme.TEXT_PRIMARY);
        row.add(val);
        p.add(row);
        p.add(Box.createVerticalStrut(3));
    }

    private JPanel foodList(String heading, String[] foods, Color accent, Color bg) {
        JPanel card = new JPanel();
        card.setBackground(bg);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accent.brighter(), 1),
            BorderFactory.createEmptyBorder(Theme.GAP, Theme.GAP, Theme.GAP, Theme.GAP)
        ));

        JLabel h = new JLabel(heading);
        h.setFont(Theme.F_LABEL);
        h.setForeground(accent.darker());
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(h);
        card.add(Box.createVerticalStrut(Theme.GAP_S));

        for (String food : foods) {
            JLabel l = new JLabel("  " + food);
            l.setFont(Theme.F_BODY);
            l.setForeground(Theme.TEXT_PRIMARY);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(l);
            card.add(Box.createVerticalStrut(3));
        }
        return card;
    }

    private JPanel buildWarningsPanel(List<String> warnings) {
        JPanel p = new JPanel();
        p.setBackground(Theme.BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(Theme.PAD, Theme.PAD + 4, Theme.PAD, Theme.PAD + 4));

        JLabel h = Theme.heading("Cross-Condition Diet Conflicts");
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(h);
        p.add(Box.createVerticalStrut(Theme.GAP));
        JLabel sub = Theme.caption("These are potential conflicts between diet plans for your conditions. Consult your doctor.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(sub);
        p.add(Box.createVerticalStrut(Theme.PAD));

        for (String w : warnings) {
            JPanel wCard = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.GAP_S, 4));
            wCard.setBackground(new Color(255, 248, 220));
            wCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 183, 77), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
            ));
            wCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            wCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            JLabel wl = new JLabel("⚠  " + w);
            wl.setFont(Theme.F_BODY);
            wl.setForeground(new Color(100, 60, 0));
            wCard.add(wl);
            p.add(wCard);
            p.add(Box.createVerticalStrut(Theme.GAP_S));
        }

        JScrollPane scroll = Theme.scroll(p);
        scroll.setBorder(null);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BG);
        wrapper.add(scroll);
        return wrapper;
    }
}
