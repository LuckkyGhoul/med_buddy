package medbuddy;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Central design tokens — every file pulls colours and fonts from here.
 * Palette: off-white background, teal accent (#00897B), clean grey text.
 */
public class Theme {

    // ── Palette ──────────────────────────────────────────────────────
    public static final Color TEAL         = new Color(0, 137, 123);   // primary
    public static final Color TEAL_DARK    = new Color(0, 105, 92);    // hover / pressed
    public static final Color TEAL_LIGHT   = new Color(224, 242, 241); // chip bg
    public static final Color BG           = new Color(250, 250, 250); // window bg
    public static final Color SURFACE      = Color.WHITE;
    public static final Color BORDER       = new Color(224, 224, 224);
    public static final Color TEXT_PRIMARY  = new Color(33,  33,  33);
    public static final Color TEXT_SECONDARY= new Color(117, 117, 117);
    public static final Color TEXT_ON_TEAL  = Color.WHITE;
    public static final Color RED_ERR      = new Color(198, 40, 40);
    public static final Color GREEN_OK     = new Color(46, 125, 50);
    public static final Color AMBER_WARN   = new Color(230, 119, 0);

    // ── Typography ───────────────────────────────────────────────────
    public static final Font  F_TITLE   = new Font("SansSerif", Font.BOLD,  20);
    public static final Font  F_SECTION = new Font("SansSerif", Font.BOLD,  14);
    public static final Font  F_LABEL   = new Font("SansSerif", Font.BOLD,  12);
    public static final Font  F_BODY    = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font  F_SMALL   = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font  F_MONO    = new Font("Monospaced", Font.PLAIN, 12);

    // ── Spacing ──────────────────────────────────────────────────────
    public static final int  GAP   = 12;
    public static final int  GAP_S =  6;
    public static final int  PAD   = 20;

    // ── Component factories ──────────────────────────────────────────

    /** Teal filled button */
    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(F_LABEL);
        b.setBackground(TEAL);
        b.setForeground(TEXT_ON_TEAL);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(TEAL_DARK); }
            public void mouseExited(java.awt.event.MouseEvent e)  { b.setBackground(TEAL); }
        });
        return b;
    }

    /** Ghost / outline button */
    public static JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setFont(F_LABEL);
        b.setBackground(SURFACE);
        b.setForeground(TEAL);
        b.setBorder(BorderFactory.createLineBorder(TEAL, 1));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Danger / destructive button */
    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        b.setFont(F_SMALL);
        b.setBackground(new Color(255, 235, 235));
        b.setForeground(RED_ERR);
        b.setBorder(BorderFactory.createLineBorder(new Color(239, 154, 154), 1));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Link-style flat button */
    public static JButton linkButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(TEAL);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Rounded text field */
    public static JTextField textField() {
        JTextField f = new JTextField();
        f.setFont(F_BODY);
        f.setBackground(SURFACE);
        f.setForeground(TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return f;
    }

    public static JPasswordField passField() {
        JPasswordField f = new JPasswordField();
        f.setFont(F_BODY);
        f.setBackground(SURFACE);
        f.setForeground(TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return f;
    }

    /** Section card with subtle shadow look */
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD)
        ));
        return p;
    }

    /** Small caption label */
    public static JLabel caption(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_SMALL);
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    /** Section heading */
    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_SECTION);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    /** Field label */
    public static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_LABEL);
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    /** Horizontal rule */
    public static JSeparator separator() {
        JSeparator s = new JSeparator();
        s.setForeground(BORDER);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    /** Standard scroll pane */
    public static JScrollPane scroll(Component c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        s.getVerticalScrollBar().setUnitIncrement(14);
        return s;
    }
}
