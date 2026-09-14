package view.components;

import view.UITheme;

import javax.swing.*;
import java.awt.*;

/** Renders a table cell's text with a small fixed leading icon (e.g. a location pin for an address column). */
public class IconTextCellRenderer extends ZebraRowRenderer {

    private final AppIcon.Name iconName;

    public IconTextCellRenderer(AppIcon.Name iconName) {
        this.iconName = iconName;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setIcon(AppIcon.of(iconName, 14, UITheme.ICON_COLOR));
        label.setIconTextGap(6);
        return label;
    }
}
