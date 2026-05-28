package medbuddy;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Initialise H2 database first (creates file if needed, seeds demo user)
        try {
            DatabaseManager.init();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Could not initialise database:\n" + e.getMessage(),
                "MedBuddy — Startup Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Shutdown hook to cleanly close DB connection
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseManager::close));

        // Apply FlatLaf-like minimalist look via Nimbus customisation
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            // Override Nimbus tokens to match our Theme
            UIManager.put("nimbusBase",          new java.awt.Color(0, 137, 123));
            UIManager.put("nimbusBlueGrey",      new java.awt.Color(110, 110, 110));
            UIManager.put("control",             new java.awt.Color(250, 250, 250));
            UIManager.put("text",                new java.awt.Color(33, 33, 33));
            UIManager.put("nimbusFocus",         new java.awt.Color(0, 137, 123, 180));
        } catch (Exception e) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ex) { /* use default */ }
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
