package analyzer;

import javax.swing.*;

/**
 * @author Lukasz Golinski
 */
public class Main {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("Could not set system look and feel: " + ex.getMessage());
            }
            
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
