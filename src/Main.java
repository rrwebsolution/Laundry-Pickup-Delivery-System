import view.LoginFrame;
import view.UITheme;

import javax.swing.*;

/** Application entry point: launches the login screen on the Swing event thread. */
public class Main {

    public static void main(String[] args) {
        UITheme.installLookAndFeel();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
