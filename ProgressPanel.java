package medbuddy;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProgressPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable            logTable;
    private SimpleLineChart   chart;

    private JTextField dateF, weightF, sugarF, cholF, vitD3F, vitB12F;
    private JTextArea  notesF;
    private JLabel     statusLbl;

    public ProgressPanel() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        // Header strip
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.PAD, 10));
        hdr.setBackground(Theme.TEAL);
        JLabel title = new JLabel("Track Progress");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        hdr.add(title);
        JLabel sub = new JLabel("  Log your metrics and watch your health improve over time.");
        sub.setFont(Theme.F_SMALL);
        sub.setForeground(new Color(178, 223, 219));
        hdr.add(sub);
        add(hdr, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(300);
        split.setResizeWeight(0);
        split.setBorder(null);
        split.setDividerSize(6);
        split.setLeftComponent(buildForm());
        split.setRightComponent(buildRight());
        add(split, BorderLayout.CENTER);

        loadTable();
    }

    // ── Entry form ───────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setBackground(Theme.SURFACE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER),
            BorderFactory.createEmptyBorder(Theme.PAD, Theme.PAD, Theme.PAD, Theme.PAD)
        ));

        JLabel heading = Theme.heading("New Entry");
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(heading);

        form.add(Box.createVerticalStrut(4));
        JLabel todayLbl = Theme.caption("Today: " +
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        todayLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(todayLbl);

        form.add(Box.createVerticalStrut(Theme.GAP));
        form.add(Theme.separator());
        form.add(Box.createVerticalStrut(Theme.GAP));

        dateF   = Theme.textField(); dateF.setText(
            LocalDate.now().format(DateTimeFormatter.ofPattern("MMM yyyy")));
        weightF = Theme.textField();
        sugarF  = Theme.textField();
        cholF   = Theme.textField();
        vitD3F  = Theme.textField();
        vitB12F = Theme.textField();

        Dimension fs = new Dimension(Integer.MAX_VALUE, 32);
        for (JTextField f : new JTextField[]{dateF, weightF, sugarF, cholF, vitD3F, vitB12F})
            f.setMaximumSize(fs);

        addRow(form, "Date / Period *",      dateF);
        addRow(form, "Weight (kg)",          weightF);
        addRow(form, "Blood Sugar (mg/dL)",  sugarF);
        addRow(form, "Cholesterol (mg/dL)",  cholF);
        addRow(form, "Vitamin D3 (ng/mL)",   vitD3F);
        addRow(form, "Vitamin B12 (pg/mL)",  vitB12F);

        form.add(Box.createVerticalStrut(Theme.GAP));
        JLabel notesLbl = Theme.fieldLabel("Notes");
        notesLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(notesLbl);
        form.add(Box.createVerticalStrut(3));
        notesF = new JTextArea(3, 14);
        notesF.setFont(Theme.F_BODY);
        notesF.setLineWrap(true);
        notesF.setWrapStyleWord(true);
        notesF.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        JScrollPane notesScroll = new JScrollPane(notesF);
        notesScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        notesScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(notesScroll);

        form.add(Box.createVerticalStrut(Theme.GAP));

        statusLbl = new JLabel(" ");
        statusLbl.setFont(Theme.F_SMALL);
        statusLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(statusLbl);

        form.add(Box.createVerticalStrut(Theme.GAP_S));

        JButton addBtn = Theme.primaryButton("Save Entry");
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        addBtn.addActionListener(e -> saveEntry());
        form.add(addBtn);

        form.add(Box.createVerticalStrut(Theme.GAP_S));

        JButton clearBtn = Theme.ghostButton("Clear Form");
        clearBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        clearBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        clearBtn.addActionListener(e -> clearForm());
        form.add(clearBtn);

        form.add(Box.createVerticalGlue());
        return form;
    }

    // ── Right panel: chart + table ───────────────────────────────────
    private JPanel buildRight() {
        JPanel right = new JPanel(new BorderLayout(0, Theme.GAP));
        right.setBackground(Theme.BG);
        right.setBorder(BorderFactory.createEmptyBorder(Theme.GAP, Theme.GAP, Theme.GAP, Theme.GAP));

        // Chart
        chart = new SimpleLineChart();
        chart.setPreferredSize(new Dimension(0, 200));
        chart.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        right.add(chart, BorderLayout.NORTH);

        // Table
        String[] cols = {"Period", "Weight (kg)", "Sugar (mg/dL)", "Chol (mg/dL)", "Vit D3", "Vit B12", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        logTable = new JTable(tableModel);
        logTable.setFont(Theme.F_BODY);
        logTable.setRowHeight(26);
        logTable.setGridColor(Theme.BORDER);
        logTable.setShowVerticalLines(false);
        logTable.setSelectionBackground(Theme.TEAL_LIGHT);
        logTable.getTableHeader().setFont(Theme.F_LABEL);
        logTable.getTableHeader().setBackground(new Color(240, 240, 240));
        logTable.getTableHeader().setForeground(Theme.TEXT_SECONDARY);
        logTable.setIntercellSpacing(new Dimension(0, 1));

        JScrollPane tableScroll = Theme.scroll(logTable);
        right.add(tableScroll, BorderLayout.CENTER);

        // Delete button row
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        JButton delBtn = Theme.dangerButton("Delete Selected Entry");
        delBtn.addActionListener(e -> deleteSelected());
        bottom.add(delBtn);
        right.add(bottom, BorderLayout.SOUTH);

        return right;
    }

    // ── Data ops ─────────────────────────────────────────────────────
    private void loadTable() {
        tableModel.setRowCount(0);
        UserStore.User u = UserStore.getCurrentUser();
        if (u == null) return;
        for (UserStore.ProgressEntry e : u.profile.progressLog) {
            tableModel.addRow(new Object[]{
                e.date,
                e.weight > 0 ? e.weight : "—",
                e.sugar > 0 ? e.sugar : "—",
                e.cholesterol > 0 ? e.cholesterol : "—",
                e.vitaminD3 > 0 ? e.vitaminD3 : "—",
                e.vitaminB12 > 0 ? e.vitaminB12 : "—",
                e.notes
            });
        }
        chart.refresh();
    }

    private void saveEntry() {
        String date = dateF.getText().trim();
        if (date.isEmpty()) {
            statusLbl.setForeground(Theme.RED_ERR);
            statusLbl.setText("Date / Period is required.");
            return;
        }

        double w = parseOpt(weightF), s = parseOpt(sugarF),
               c = parseOpt(cholF), d = parseOpt(vitD3F), b = parseOpt(vitB12F);

        if (w < 0 || s < 0 || c < 0 || d < 0 || b < 0) {
            statusLbl.setForeground(Theme.RED_ERR);
            statusLbl.setText("Numeric fields must be valid positive numbers.");
            return;
        }

        UserStore.ProgressEntry entry = new UserStore.ProgressEntry(date, w, s, c, d, b, notesF.getText().trim());
        UserStore.addProgressEntry(entry);

        statusLbl.setForeground(Theme.GREEN_OK);
        statusLbl.setText("Entry saved to database ✓");
        loadTable();
        clearForm();
    }

    private void deleteSelected() {
        int row = logTable.getSelectedRow();
        if (row < 0) {
            statusLbl.setForeground(Theme.AMBER_WARN);
            statusLbl.setText("Select a row to delete.");
            return;
        }
        UserStore.User u = UserStore.getCurrentUser();
        if (u == null) return;
        if (row < u.profile.progressLog.size()) {
            UserStore.ProgressEntry e = u.profile.progressLog.get(row);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete entry for \"" + e.date + "\"?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                UserStore.deleteProgressEntry(e);
                statusLbl.setForeground(Theme.RED_ERR);
                statusLbl.setText("Entry deleted.");
                loadTable();
            }
        }
    }

    private void clearForm() {
        weightF.setText(""); sugarF.setText(""); cholF.setText("");
        vitD3F.setText(""); vitB12F.setText(""); notesF.setText("");
        dateF.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM yyyy")));
    }

    private double parseOpt(JTextField f) {
        String s = f.getText().trim();
        if (s.isEmpty()) return 0;
        try { double v = Double.parseDouble(s); return v < 0 ? -1 : v; }
        catch (NumberFormatException e) { return -1; }
    }

    private void addRow(JPanel p, String label, JComponent field) {
        JLabel l = Theme.fieldLabel(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(3));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(field);
        p.add(Box.createVerticalStrut(Theme.GAP_S));
    }

    // ── Inline line chart ─────────────────────────────────────────────
    private class SimpleLineChart extends JPanel {

        SimpleLineChart() {
            setBackground(Theme.SURFACE);
        }

        void refresh() { repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            UserStore.User u = UserStore.getCurrentUser();
            if (u == null || u.profile.progressLog.size() < 2) {
                g2.setColor(Theme.TEXT_SECONDARY);
                g2.setFont(Theme.F_SMALL);
                g2.drawString("Add at least 2 entries to see the trend chart.", 20, getHeight() / 2);
                return;
            }

            List<UserStore.ProgressEntry> log = u.profile.progressLog;
            int n = log.size();
            int W = getWidth(), H = getHeight();
            int pad = 50, top = 20, bot = 30;

            // Find weight range
            double minW = Double.MAX_VALUE, maxW = -Double.MAX_VALUE;
            for (UserStore.ProgressEntry e : log) {
                if (e.weight > 0) { minW = Math.min(minW, e.weight); maxW = Math.max(maxW, e.weight); }
            }
            if (minW == Double.MAX_VALUE) { minW = 0; maxW = 100; }
            double range = Math.max(maxW - minW, 5);
            minW -= range * 0.1; maxW += range * 0.1;

            // Grid
            g2.setColor(new Color(238, 238, 238));
            for (int i = 0; i <= 4; i++) {
                int y = top + (int) ((H - top - bot) * i / 4.0);
                g2.drawLine(pad, y, W - 10, y);
            }

            // Axes
            g2.setColor(Theme.BORDER);
            g2.drawLine(pad, top, pad, H - bot);
            g2.drawLine(pad, H - bot, W - 10, H - bot);

            // Weight line
            g2.setColor(Theme.TEAL);
            g2.setStroke(new BasicStroke(2.5f));
            int[] xs = new int[n], ys = new int[n];
            for (int i = 0; i < n; i++) {
                xs[i] = pad + (int) ((W - pad - 10) * i / (double) (n - 1));
                double w = log.get(i).weight > 0 ? log.get(i).weight : minW;
                ys[i] = H - bot - (int) ((H - top - bot) * (w - minW) / (maxW - minW));
            }
            for (int i = 0; i < n - 1; i++) g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);

            // Dots + labels
            g2.setFont(Theme.F_SMALL);
            for (int i = 0; i < n; i++) {
                g2.setColor(Theme.TEAL);
                g2.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
                g2.setColor(Theme.TEXT_SECONDARY);
                String lbl = log.get(i).date;
                if (lbl.length() > 8) lbl = lbl.substring(0, 8);
                g2.drawString(lbl, xs[i] - 12, H - bot + 14);
            }

            // Y-axis label
            g2.setColor(Theme.TEXT_SECONDARY);
            g2.setFont(Theme.F_SMALL);
            g2.drawString("Weight (kg)", 2, top + 10);

            // Chart title
            g2.setFont(Theme.F_LABEL);
            g2.setColor(Theme.TEXT_PRIMARY);
            g2.drawString("Weight Trend", pad + 8, top + 14);
        }
    }
}
