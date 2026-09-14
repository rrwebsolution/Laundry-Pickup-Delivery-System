package view.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/** Renders a status column's text as a colored pill badge instead of plain text. */
public class StatusCellRenderer extends DefaultTableCellRenderer {

    private static final Color ALT_ROW = new Color(0xF8, 0xFA, 0xFC);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        String status = value == null ? "" : value.toString();
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrapper.setOpaque(true);
        wrapper.setBackground(isSelected ? view.UITheme.PRIMARY_SOFT : (row % 2 == 0 ? Color.WHITE : ALT_ROW));
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        if (!status.isEmpty()) {
            wrapper.add(StatusColors.badge(status));
        }
        return wrapper;
    }
}
