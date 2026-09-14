package view.components;

import view.UITheme;

import javax.swing.*;
import java.awt.*;

/** Small GridBagLayout helper so every Add/Edit dialog shares the same aligned, two-column form look. */
public final class FormLayout {

    private FormLayout() {
    }

    public static JPanel newForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        return form;
    }

    public static int sectionTitle(JPanel form, int row, String text) {
        GridBagConstraints gbc = baseConstraints(row, 0, 2);
        gbc.insets = new Insets(row == 0 ? 0 : 18, 0, 8, 0);
        JLabel label = new JLabel(text);
        label.setFont(UITheme.FONT_SECTION_TITLE);
        label.setForeground(UITheme.TEXT_PRIMARY);
        form.add(label, gbc);
        return row + 1;
    }

    /** A single field spanning the full form width. */
    public static int fullRow(JPanel form, int row, String labelText, JComponent field) {
        GridBagConstraints labelGbc = baseConstraints(row, 0, 2);
        labelGbc.insets = new Insets(row == 0 ? 0 : 12, 0, 4, 0);
        form.add(label(labelText), labelGbc);

        GridBagConstraints fieldGbc = baseConstraints(row + 1, 0, 2);
        fieldGbc.insets = new Insets(0, 0, 0, 0);
        field.putClientProperty("JComponent.minimumHeight", 42);
        form.add(field, fieldGbc);
        return row + 2;
    }

    /** Two fields side-by-side sharing one row, e.g. Contact Number + Email. */
    public static int splitRow(JPanel form, int row, String label1, JComponent field1, String label2,
            JComponent field2) {
        GridBagConstraints l1 = baseConstraints(row, 0, 1);
        l1.insets = new Insets(row == 0 ? 0 : 12, 0, 4, 8);
        form.add(label(label1), l1);

        GridBagConstraints l2 = baseConstraints(row, 1, 1);
        l2.insets = new Insets(row == 0 ? 0 : 12, 8, 4, 0);
        form.add(label(label2), l2);

        GridBagConstraints f1 = baseConstraints(row + 1, 0, 1);
        f1.insets = new Insets(0, 0, 0, 8);
        field1.putClientProperty("JComponent.minimumHeight", 42);
        form.add(field1, f1);

        GridBagConstraints f2 = baseConstraints(row + 1, 1, 1);
        f2.insets = new Insets(0, 8, 0, 0);
        field2.putClientProperty("JComponent.minimumHeight", 42);
        form.add(field2, f2);
        return row + 2;
    }

    private static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.FONT_LABEL_BOLD);
        label.setForeground(UITheme.TEXT_LABEL);
        return label;
    }

    private static GridBagConstraints baseConstraints(int row, int col, int colspan) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.gridwidth = colspan;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = colspan == 2 ? 1.0 : 0.5;
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }
}
