package view.components;

import view.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * The app's standard single-line text input: a clearly visible neutral border that turns
 * primary-blue on focus and danger-red when marked invalid, at a comfortable 40px height.
 * All border/focus/error rendering is delegated to FlatLaf's own component painting via
 * standard UIManager/client-property keys (see {@link UITheme#installLookAndFeel()}), so
 * rounding, icons, and focus animation stay consistent with the rest of the app.
 */
public class StyledTextField extends JTextField {

    public StyledTextField() {
        setFont(UITheme.FONT_FIELD);
        setForeground(UITheme.TEXT_PRIMARY);
        setMargin(new Insets(6, 10, 6, 10));
        putClientProperty("JComponent.minimumHeight", 42);
    }

    public StyledTextField(String placeholder) {
        this();
        setPlaceholder(placeholder);
    }

    public void setPlaceholder(String text) {
        putClientProperty("JTextField.placeholderText", text);
    }

    public void setLeadingIcon(AppIcon.Name icon) {
        putClientProperty("JTextField.leadingIcon", AppIcon.of(icon, 16, UITheme.ICON_COLOR));
    }

    /** Toggles the red "invalid" outline (FlatLaf's built-in error outline) without hiding the border. */
    public void setError(boolean error) {
        putClientProperty("JComponent.outline", error ? "error" : null);
    }
}
