package view.components;

import view.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/** Default cell renderer that gives every table subtle alternating row colors. */
public class ZebraRowRenderer extends DefaultTableCellRenderer {

    private static final Color ALT_ROW = new Color(0xF8, 0xFA, 0xFC);

    public ZebraRowRenderer() {
        setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setForeground(UITheme.TEXT_PRIMARY);
        if (isSelected) {
            label.setBackground(UITheme.PRIMARY_SOFT);
        } else {
            label.setBackground(row % 2 == 0 ? Color.WHITE : ALT_ROW);
        }
        return label;
    }
}
