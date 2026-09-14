package view.components;

import org.kordamp.ikonli.swing.FontIcon;
import view.UITheme;

import javax.swing.*;
import java.awt.*;

/** A borderless, icon-only button (notification bell, dropdown chevron trigger, etc). */
public class IconButton extends JButton {

    private final FontIcon icon;

    public IconButton(AppIcon.Name iconName, int size, String tooltip) {
        this.icon = AppIcon.of(iconName, size, UITheme.ICON_COLOR);
        setIcon(icon);
        setToolTipText(tooltip);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        putClientProperty("JButton.buttonType", "borderless");
        setMargin(new Insets(8, 10, 8, 10));
    }

    public void setIconColor(Color color) {
        icon.setIconColor(color);
        repaint();
    }
}
