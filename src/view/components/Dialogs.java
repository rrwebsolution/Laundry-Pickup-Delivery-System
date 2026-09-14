package view.components;

import view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Styled replacements for JOptionPane's default confirm dialog, matching the app's visual language. */
public final class Dialogs {

    private Dialogs() {
    }

    public static boolean confirmDelete(Component parent, String title, String message) {
        return confirm(parent, title, message, true, UITheme.dangerButton(AppIcon.Name.DELETE, "Delete"));
    }

    public static boolean confirmAction(Component parent, String title, String message, String confirmLabel) {
        AppIcon.Name icon = confirmLabel.equalsIgnoreCase("Logout") ? AppIcon.Name.LOGOUT : AppIcon.Name.CHECK;
        return confirm(parent, title, message, false, UITheme.primaryButton(icon, confirmLabel));
    }

    private static boolean confirm(Component parent, String title, String message, boolean destructive,
            JButton confirmButton) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(false);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setBackground(Color.WHITE);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(28, 28, 24, 28));

        Color iconAccent = destructive ? UITheme.DANGER : UITheme.WARNING;
        JLabel iconLabel = new JLabel(AppIcon.of(destructive ? AppIcon.Name.DELETE : AppIcon.Name.WARNING, 28, iconAccent));
        iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_SECTION_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setBorder(new EmptyBorder(14, 0, 8, 0));

        JLabel messageLabel = new JLabel("<html><body style='width: 280px'>" + message.replace("\n", "<br>")
                + "</body></html>");
        messageLabel.setFont(UITheme.FONT_LABEL);
        messageLabel.setForeground(UITheme.TEXT_SECONDARY);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(iconLabel);
        content.add(titleLabel);
        content.add(messageLabel);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.setBorder(new EmptyBorder(24, 0, 0, 0));
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton cancelButton = UITheme.secondaryButton("Cancel");
        boolean[] result = {false};
        cancelButton.addActionListener(e -> dialog.dispose());
        confirmButton.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });

        buttonRow.add(cancelButton);
        buttonRow.add(confirmButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(content, BorderLayout.CENTER);
        wrapper.add(buttonRow, BorderLayout.SOUTH);
        wrapper.setBorder(new EmptyBorder(0, 0, 20, 24));

        dialog.setContentPane(wrapper);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }
}
