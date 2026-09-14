package view.components;

import view.UITheme;

/** The app's standard dropdown: same visible border/focus/height language as the text inputs. */
public class StyledComboBox<T> extends javax.swing.JComboBox<T> {

    public StyledComboBox() {
        setFont(UITheme.FONT_FIELD);
        putClientProperty("JComponent.minimumHeight", 42);
    }

    public void setError(boolean error) {
        putClientProperty("JComponent.outline", error ? "error" : null);
    }
}
