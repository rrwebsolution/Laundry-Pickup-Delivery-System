package view.components;

import view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** A small, auto-dismissing notification banner so CRUD feedback doesn't require a modal click-through. */
public final class Toast {

    public enum Type { SUCCESS, ERROR, WARNING }

    private Toast() {
    }

    public static void success(Component owner, String message) {
        show(owner, message, Type.SUCCESS);
    }

    public static void error(Component owner, String message) {
        show(owner, message, Type.ERROR);
    }

    public static void warning(Component owner, String message) {
        show(owner, message, Type.WARNING);
    }

    public static void show(Component owner, String message, Type type) {
        Window ownerWindow = owner == null ? null : SwingUtilities.getWindowAncestor(owner);
        JWindow window = new JWindow(ownerWindow);
        window.setFocusableWindowState(false);
        window.setType(Window.Type.POPUP);

        Color accent;
        AppIcon.Name iconName;
        switch (type) {
            case SUCCESS:
                accent = UITheme.SUCCESS;
                iconName = AppIcon.Name.CHECK_CIRCLE;
                break;
            case WARNING:
                accent = UITheme.WARNING;
                iconName = AppIcon.Name.WARNING;
                break;
            default:
                accent = UITheme.DANGER;
                iconName = AppIcon.Name.ERROR;
        }

        JPanel content = new JPanel(new BorderLayout(10, 0));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(14, 16, 14, 18)));

        JLabel iconLabel = new JLabel(AppIcon.of(iconName, 18, accent));

        JLabel textLabel = new JLabel("<html><body style='width: 260px'>" + escape(message) + "</body></html>");
        textLabel.setFont(UITheme.FONT_LABEL);
        textLabel.setForeground(UITheme.TEXT_PRIMARY);

        content.add(iconLabel, BorderLayout.WEST);
        content.add(textLabel, BorderLayout.CENTER);
        content.setBorder(BorderFactory.createCompoundBorder(
                content.getBorder(),
                BorderFactory.createLineBorder(UITheme.BORDER, 1)));

        window.setContentPane(content);
        window.pack();

        Rectangle bounds = ownerWindow != null ? ownerWindow.getBounds()
                : GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int x = bounds.x + bounds.width - window.getWidth() - 28;
        int y = bounds.y + bounds.height - window.getHeight() - 36;
        window.setLocation(Math.max(x, 0), Math.max(y, 0));
        window.setOpacity(0f);
        window.setVisible(true);

        Timer fadeIn = new Timer(15, null);
        fadeIn.addActionListener(e -> {
            float opacity = window.getOpacity() + 0.12f;
            if (opacity >= 1f) {
                window.setOpacity(1f);
                fadeIn.stop();
            } else {
                window.setOpacity(opacity);
            }
        });
        fadeIn.start();

        Timer dismiss = new Timer(2600, e -> fadeOutAndDispose(window));
        dismiss.setRepeats(false);
        dismiss.start();
    }

    private static void fadeOutAndDispose(JWindow window) {
        Timer fadeOut = new Timer(15, null);
        fadeOut.addActionListener(e -> {
            float opacity = window.getOpacity() - 0.12f;
            if (opacity <= 0f) {
                ((Timer) e.getSource()).stop();
                window.dispose();
            } else {
                window.setOpacity(opacity);
            }
        });
        fadeOut.start();
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
