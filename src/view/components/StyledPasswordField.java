package view.components;

import view.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * The app's standard password input. Height/border/focus/error behave exactly like
 * {@link StyledTextField}. The show/hide toggle is FlatLaf's built-in reveal button
 * (enabled globally in {@link UITheme#installLookAndFeel()}) - a real vector icon,
 * not an emoji - so no extra composite component is needed.
 */
public class StyledPasswordField extends JPasswordField {

    public StyledPasswordField() {
        setFont(UITheme.FONT_FIELD);
        setForeground(UITheme.TEXT_PRIMARY);
        setMargin(new Insets(6, 10, 6, 10));
        putClientProperty("JComponent.minimumHeight", 42);
    }

    public StyledPasswordField(String placeholder) {
        this();
        setPlaceholder(placeholder);
    }

    public void setPlaceholder(String text) {
        putClientProperty("JTextField.placeholderText", text);
    }

    public void setLeadingIcon(AppIcon.Name icon) {
        putClientProperty("JTextField.leadingIcon", AppIcon.of(icon, 16, UITheme.ICON_COLOR));
    }

    public void setError(boolean error) {
        putClientProperty("JComponent.outline", error ? "error" : null);
    }
}
