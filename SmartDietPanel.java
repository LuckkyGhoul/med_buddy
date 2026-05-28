package medbuddy;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * SmartDietPanel — ML-powered diet recommendations + 7-day meal planner.
 * Replaces the static DietPanel as the primary "Diet Plans" tab.
 */
public class SmartDietPanel extends JPanel {

    private JPanel contentArea;
    private JLabel scoreLabel;
    private JLabel tierLabel;

    public SmartDietPanel() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        // ── Header ────────────────────────────────────────────────────
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(Theme.TEAL);
        hdr.setBorder(BorderFactory.createEmptyBorder(10, Theme.PAD, 10, Theme.PAD));

        JPanel hdrLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        hdrLeft.setOpaque(false);
        JLabel icon = new JLabel("🤖  ");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 18));
        JLabel title = new JLabel("Smart Diet Planner");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("   ML-powered · Based on your current progress");
        sub.setFont(Theme.F_SMALL);
        sub.setForeground(new Color(178, 223, 219));
        hdrLeft.add(icon);
        hdrLeft.add(title);
        hdrLeft.add(sub);
        hdr.add(hdrLeft, BorderLayout.WEST);

        JButton refreshBtn = new JButton("↻  Refresh Analysis");
        refreshBtn.setFont(Theme.F_SMALL);
        refreshBtn.setBackground(Theme.TEAL_DARK);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshContent());
        hdr.add(refreshBtn, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        // ── Content area (scrollable) ────────────────────────────────
        contentArea = new JPanel();
        contentArea.setLayout(new BoxLayout(contentArea, BoxLayout.Y_AXIS));
        contentArea.setBackground(Theme.BG);
        contentArea.setBorder(BorderFactory.createEmptyBorder(Theme.PAD, Theme.PAD, Theme.PAD, Theme.PAD));

        JScrollPane scroll = Theme.scroll(contentArea);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        add(scroll, BorderLayout.CENTER);

        refreshContent();
    }

    // ── Main render ───────────────────────────────────────────────────

    private void refreshContent() {
        contentArea.removeAll();
        UserStore.User user = UserStore.getCurrentUser();

        if (user == null) {
            contentArea.add(centeredMsg("Please log in to see your personalised diet plan."));
            contentArea.revalidate();
            contentArea.repaint();
            return;
        }

        MLDietEngine.Recommendation rec = MLDietEngine.analyse(user);

        // 1. Health Score card
        contentArea.add(buildScoreCard(rec));
        contentArea.add(gap(Theme.PAD));

        // 2. Metric status row
        if (!rec.metricStatuses.isEmpty()) {
            contentArea.add(sectionHeading("📊  Your Current Metrics"));
            contentArea.add(gap(Theme.GAP_S));
            contentArea.add(buildMetricsRow(rec.metricStatuses));
            contentArea.add(gap(Theme.PAD));
        }

        // 3. Personal Insights
        if (!rec.personalInsights.isEmpty()) {
            contentArea.add(sectionHeading("💡  Personalised Insights"));
            contentArea.add(gap(Theme.GAP_S));
            contentArea.add(buildInsightsPanel(rec.personalInsights));
            contentArea.add(gap(Theme.PAD));
        }

        // 4. 7-Day Meal Plan
        contentArea.add(sectionHeading("📅  Your 7-Day Personalised Meal Plan"));
        contentArea.add(gap(Theme.GAP_S));
        contentArea.add(buildWeeklyPlanPanel(rec.weeklyPlan));
        contentArea.add(gap(Theme.PAD));

        // 5. Disclaimer
        JLabel disc = Theme.caption("⚠  This plan is generated from your logged metrics and health conditions. It is not a substitute for medical advice. Consult your doctor before making major dietary changes.");
        disc.setAlignmentX(Component.LEFT_ALIGNMENT);
        disc.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 183, 77), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        disc.setOpaque(true);
        disc.setBackground(new Color(255, 248, 220));
        contentArea.add(disc);

        contentArea.revalidate();
        contentArea.repaint();
    }

    // ── Score Card ────────────────────────────────────────────────────

    private JPanel buildScoreCard(MLDietEngine.Recommendation rec) {
        JPanel card = new JPanel(new BorderLayout(Theme.PAD, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(Theme.PAD, Theme.PAD, Theme.PAD, Theme.PAD)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Score circle (left)
        JPanel scoreCircle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int score = (int)(100 - rec.overallScore);
                Color col = score >= 75 ? Theme.GREEN_OK : score >= 45 ? Theme.AMBER_WARN : Theme.RED_ERR;
                int sz = Math.min(getWidth(), getHeight()) - 8;
                int x = (getWidth() - sz) / 2, y = (getHeight() - sz) / 2;
                g2.setColor(new Color(245, 245, 245));
                g2.fillOval(x, y, sz, sz);
                g2.setColor(col);
                g2.setStroke(new BasicStroke(6));
                int arc = (int)(score / 100.0 * 360);
                g2.drawArc(x + 3, y + 3, sz - 6, sz - 6, 90, -arc);
                g2.setColor(col);
                g2.setFont(new Font("SansSerif", Font.BOLD, 22));
                String txt = score + "";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(txt, getWidth() / 2 - fm.stringWidth(txt) / 2, getHeight() / 2 + 7);
                g2.setFont(Theme.F_SMALL);
                g2.setColor(Theme.TEXT_SECONDARY);
                String sub = "/ 100";
                g2.drawString(sub, getWidth() / 2 - g2.getFontMetrics().stringWidth(sub) / 2, getHeight() / 2 + 20);
            }
        };
        scoreCircle.setBackground(Color.WHITE);
        scoreCircle.setPreferredSize(new Dimension(90, 90));
        card.add(scoreCircle, BorderLayout.WEST);

        // Text (right)
        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));

        Color tierColor = rec.tier == MLDietEngine.Tier.MAINTENANCE ? Theme.GREEN_OK
                        : rec.tier == MLDietEngine.Tier.THERAPEUTIC  ? Theme.AMBER_WARN : Theme.RED_ERR;
        JLabel tierLbl = new JLabel(rec.tierLabel);
        tierLbl.setFont(Theme.F_TITLE);
        tierLbl.setForeground(tierColor);
        tierLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.add(tierLbl);
        txt.add(Box.createVerticalStrut(4));

        JLabel reason = Theme.caption(rec.tierReason);
        reason.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.add(reason);
        txt.add(Box.createVerticalStrut(8));

        JLabel scoreLbl = Theme.caption("Health Score: " + (int)(100 - rec.overallScore) + "/100  ·  Based on " + rec.metricStatuses.size() + " metrics  ·  " + rec.weeklyPlan.size() + "-day plan generated");
        scoreLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.add(scoreLbl);

        card.add(txt, BorderLayout.CENTER);
        return card;
    }

    // ── Metrics Row ───────────────────────────────────────────────────

    private JPanel buildMetricsRow(List<MLDietEngine.MetricStatus> statuses) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.GAP, Theme.GAP_S));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (MLDietEngine.MetricStatus ms : statuses) {
            Color accent = ms.color == MLDietEngine.MetricStatus.Color.GREEN ? Theme.GREEN_OK
                         : ms.color == MLDietEngine.MetricStatus.Color.AMBER  ? Theme.AMBER_WARN : Theme.RED_ERR;
            Color bg = ms.color == MLDietEngine.MetricStatus.Color.GREEN ? new Color(232, 245, 233)
                     : ms.color == MLDietEngine.MetricStatus.Color.AMBER  ? new Color(255, 248, 220) : new Color(255, 235, 238);

            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(bg);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 1),
                BorderFactory.createEmptyBorder(Theme.GAP_S, Theme.PAD, Theme.GAP_S, Theme.PAD)
            ));

            JLabel name = new JLabel(ms.name);
            name.setFont(Theme.F_LABEL);
            name.setForeground(Theme.TEXT_SECONDARY);
            name.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(name);

            JLabel val = new JLabel(ms.value);
            val.setFont(new Font("SansSerif", Font.BOLD, 16));
            val.setForeground(accent.darker());
            val.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(val);

            JLabel st = new JLabel(ms.status);
            st.setFont(Theme.F_SMALL);
            st.setForeground(accent);
            st.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(st);

            card.add(Box.createVerticalStrut(4));

            // Tooltip for advice
            card.setToolTipText("<html><b>Advice:</b> " + ms.advice + "</html>");
            row.add(card);
        }

        // Tip label
        JLabel tip = Theme.caption("  ⓘ Hover over a metric card for dietary advice");
        row.add(tip);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(row, BorderLayout.WEST);
        return wrapper;
    }

    // ── Insights ──────────────────────────────────────────────────────

    private JPanel buildInsightsPanel(List<String> insights) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (String insight : insights) {
            JLabel l = new JLabel("<html>" + insight + "</html>");
            l.setFont(Theme.F_BODY);
            l.setForeground(Theme.TEXT_PRIMARY);
            l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, Theme.TEAL),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));
            l.setOpaque(true);
            l.setBackground(new Color(224, 242, 241));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            p.add(l);
            p.add(Box.createVerticalStrut(Theme.GAP_S));
        }
        return p;
    }

    // ── 7-Day Plan ────────────────────────────────────────────────────

    private JPanel buildWeeklyPlanPanel(List<MLDietEngine.DayMealPlan> weeklyPlan) {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setFont(Theme.F_LABEL);
        tabs.setBackground(Theme.BG);

        String[] dayEmojis = {"🌱", "🌿", "☘", "🌾", "🍃", "🌻", "🌞"};
        for (int i = 0; i < weeklyPlan.size(); i++) {
            MLDietEngine.DayMealPlan day = weeklyPlan.get(i);
            tabs.addTab(" " + dayEmojis[i] + " " + day.day.substring(0, 3) + " ", buildDayCard(day));
        }

        // Highlight today's tab
        int todayIdx = java.time.LocalDate.now().getDayOfWeek().getValue() - 1; // Mon=0
        if (todayIdx >= 0 && todayIdx < tabs.getTabCount()) {
            tabs.setSelectedIndex(todayIdx);
            tabs.setBackgroundAt(todayIdx, Theme.TEAL_LIGHT);
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(tabs, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildDayCard(MLDietEngine.DayMealPlan day) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(Theme.PAD, Theme.PAD, Theme.PAD, Theme.PAD));

        // Day title
        JLabel dayTitle = new JLabel("🗓  " + day.day + " — Full Day Plan");
        dayTitle.setFont(Theme.F_TITLE);
        dayTitle.setForeground(Theme.TEAL_DARK);
        dayTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(dayTitle);
        p.add(Box.createVerticalStrut(Theme.PAD));
        p.add(Theme.separator());
        p.add(Box.createVerticalStrut(Theme.GAP));

        addMealSection(p, "🌅  Breakfast (7:00 – 8:30 AM)", day.breakfast, new Color(255, 249, 230));
        addMealSection(p, "🍎  Morning Snack (10:30 – 11:00 AM)", day.morningSnack, new Color(232, 245, 233));
        addMealSection(p, "🍱  Lunch (1:00 – 2:00 PM)", day.lunch, new Color(224, 242, 241));
        addMealSection(p, "🥒  Afternoon Snack (4:00 – 4:30 PM)", day.afternoonSnack, new Color(232, 245, 233));
        addMealSection(p, "🌙  Dinner (7:00 – 8:00 PM)", day.dinner, new Color(235, 235, 255));

        // Evening tip
        p.add(Box.createVerticalStrut(Theme.GAP));
        JLabel tipBox = new JLabel("<html>" + day.eveningTip + "</html>");
        tipBox.setFont(Theme.F_BODY);
        tipBox.setForeground(Theme.TEAL_DARK);
        tipBox.setOpaque(true);
        tipBox.setBackground(Theme.TEAL_LIGHT);
        tipBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.TEAL, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        tipBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        tipBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        p.add(tipBox);

        JScrollPane scroll = Theme.scroll(p);
        scroll.setBorder(null);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(scroll);
        return wrapper;
    }

    private void addMealSection(JPanel parent, String title, String content, Color bg) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(Theme.GAP_S, Theme.GAP, Theme.GAP_S, Theme.GAP)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.F_LABEL);
        titleLbl.setForeground(Theme.TEXT_SECONDARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLbl);

        card.add(Box.createVerticalStrut(4));

        JLabel contentLbl = new JLabel("<html><b>" + content + "</b></html>");
        contentLbl.setFont(Theme.F_BODY);
        contentLbl.setForeground(Theme.TEXT_PRIMARY);
        contentLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(contentLbl);

        parent.add(card);
        parent.add(Box.createVerticalStrut(Theme.GAP_S));
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private JLabel sectionHeading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.F_SECTION);
        l.setForeground(Theme.TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private Component gap(int h) {
        return Box.createVerticalStrut(h);
    }

    private JLabel centeredMsg(String msg) {
        JLabel l = new JLabel(msg, SwingConstants.CENTER);
        l.setFont(Theme.F_BODY);
        l.setForeground(Theme.TEXT_SECONDARY);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }
}
