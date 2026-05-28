package medbuddy;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;

public class ResourcesPanel extends JPanel {

    public ResourcesPanel() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        titleBar.setBackground(new Color(0, 128, 128));
        JLabel title = new JLabel("Medical References & Resources");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        titleBar.add(title);
        JLabel sub = new JLabel("  Click any link to open in your browser.");
        sub.setFont(new Font("Arial", Font.PLAIN, 12));
        sub.setForeground(new Color(200, 235, 235));
        titleBar.add(sub);
        add(titleBar, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setBackground(Color.WHITE);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // General
        content.add(categoryHeader("General Medical References"));
        content.add(Box.createVerticalStrut(6));
        content.add(resourcesTable(new String[][]{
            {"WHO — World Health Organization",        "https://www.who.int",            "Global health guidelines and disease factsheets"},
            {"Mayo Clinic",                            "https://www.mayoclinic.org",      "Trusted patient-focused medical info and diet guidance"},
            {"MedlinePlus (NIH)",                      "https://medlineplus.gov",         "National Institutes of Health — diseases and nutrition"},
            {"WebMD — Diet & Nutrition",               "https://www.webmd.com/diet",      "Diet, nutrition and condition-based food guides"},
        }));

        content.add(Box.createVerticalStrut(16));
        content.add(categoryHeader("Disease-Specific Resources"));
        content.add(Box.createVerticalStrut(6));
        content.add(resourcesTable(new String[][]{
            {"American Diabetes Association",          "https://www.diabetes.org",        "Diabetes diet, lifestyle and management guidelines"},
            {"PCOS Awareness Association",             "https://www.pcosaa.org",          "PCOS diet guidelines and lifestyle resources"},
            {"American Thyroid Association",           "https://www.thyroid.org",         "Thyroid diet, treatment and clinical guidelines"},
            {"American Heart Association",             "https://www.heart.org",           "Heart-healthy diet and lifestyle resources"},
            {"Alzheimer's Association",                "https://www.alz.org",             "Brain health, MIND Diet and lifestyle guidance"},
            {"Vitamin D Council",                      "https://www.vitamindcouncil.org", "Vitamin D research, deficiency and dosing guidance"},
            {"Harvard Health — B12",                   "https://www.health.harvard.edu",  "Vitamin B12 deficiency, sources and supplementation"},
            {"National Lipid Association",             "https://www.lipid.org",           "Cholesterol management, diet and clinical guidelines"},
        }));

        content.add(Box.createVerticalStrut(16));
        content.add(categoryHeader("India-Specific Health Resources"));
        content.add(Box.createVerticalStrut(6));
        content.add(resourcesTable(new String[][]{
            {"ICMR — Indian Council of Medical Research","https://www.icmr.gov.in",       "Indian dietary guidelines and RDA standards"},
            {"AIIMS Patient Resources",                "https://www.aiims.edu",           "All India Institute of Medical Sciences guidelines"},
            {"NIN — National Institute of Nutrition",  "https://www.nin.res.in",          "Indian food composition tables and dietary guidelines"},
        }));

        content.add(Box.createVerticalStrut(20));

        // Disclaimer box
        JTextArea disc = new JTextArea(
            "DISCLAIMER:  MedBuddy is an educational project tool only. " +
            "It is NOT a substitute for professional medical advice, diagnosis, or treatment. " +
            "Always consult a qualified doctor or registered dietitian before making any changes " +
            "to your diet or treatment plan."
        );
        disc.setFont(new Font("Arial", Font.ITALIC, 11));
        disc.setForeground(new Color(100, 60, 0));
        disc.setBackground(new Color(255, 248, 220));
        disc.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 160, 0)),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        disc.setEditable(false);
        disc.setLineWrap(true); disc.setWrapStyleWord(true);
        disc.setAlignmentX(Component.LEFT_ALIGNMENT);
        disc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        content.add(disc);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JLabel categoryHeader(String text) {
        JLabel l = new JLabel("  " + text.toUpperCase());
        l.setFont(new Font("Arial", Font.BOLD, 12));
        l.setForeground(Color.WHITE);
        l.setBackground(new Color(60, 100, 140));
        l.setOpaque(true);
        l.setBorder(BorderFactory.createEmptyBorder(5, 6, 5, 6));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return l;
    }

    private JPanel resourcesTable(String[][] rows) {
        String[] cols = {"Website / Organization", "URL", "Description"};
        JTable table = new JTable(rows, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(90, 130, 160));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setRowHeight(26);
        table.setGridColor(new Color(220, 220, 220));
        table.getColumnModel().getColumn(0).setPreferredWidth(240);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(360);
        table.setSelectionBackground(new Color(200, 230, 230));

        // Render URL column as blue clickable-looking text
        table.getColumnModel().getColumn(1).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                l.setForeground(sel ? Color.WHITE : new Color(0, 80, 200));
                l.setFont(new Font("Arial", Font.PLAIN, 12));
                return l;
            }
        });

        // Alternate row colors
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(245, 250, 255));
                if (c == 1 && !sel) comp.setForeground(new Color(0, 80, 200));
                else if (!sel) comp.setForeground(Color.DARK_GRAY);
                return comp;
            }
        });

        // Click to open URL
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String url = rows[row][1];
                    int choice = JOptionPane.showConfirmDialog(
                        ResourcesPanel.this,
                        "Open in browser?\n" + url,
                        "Open Website",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    if (choice == JOptionPane.YES_OPTION) {
                        try { Desktop.getDesktop().browse(new URI(url)); }
                        catch (Exception ex) {
                            JOptionPane.showMessageDialog(ResourcesPanel.this,
                                "Could not open browser.\nURL: " + url,
                                "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        int tableH = rows.length * 26 + 28;
        scroll.setPreferredSize(new Dimension(900, tableH));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, tableH));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(scroll);
        return wrap;
    }
}
