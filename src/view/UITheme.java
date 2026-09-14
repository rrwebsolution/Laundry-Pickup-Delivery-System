package view;

import com.formdev.flatlaf.FlatLightLaf;
import view.components.AppIcon;
import view.components.SearchField;
import view.components.StyledComboBox;
import view.components.StyledPasswordField;
import view.components.StyledTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Central design system for the whole app: palette, typography, spacing, and
 * factory methods for buttons/fields/tables so every screen looks and
 * behaves the same way. Also responsible for installing FlatLaf at startup.
 */
public final class UITheme {

    // ---- Palette -----------------------------------------------------
    public static final Color PRIMARY = new Color(0x25, 0x63, 0xEB);
    public static final Color PRIMARY_HOVER = new Color(0x1D, 0x4E, 0xD8);
    public static final Color PRIMARY_SOFT = new Color(0xEF, 0xF6, 0xFF);
    public static final Color SECONDARY = new Color(0x06, 0xB6, 0xD4);
    public static final Color BACKGROUND = new Color(0xF8, 0xFA, 0xFC);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color SIDEBAR = new Color(0x0F, 0x17, 0x2A);
    public static final Color SIDEBAR_HOVER = new Color(0x1E, 0x29, 0x3B);
    public static final Color SIDEBAR_ACTIVE = new Color(0x25, 0x63, 0xEB);
    public static final Color SIDEBAR_TEXT = new Color(0xCB, 0xD5, 0xE1);
    public static final Color SIDEBAR_TEXT_MUTED = new Color(0x64, 0x74, 0x8B);
    /** Icon color used against the dark sidebar background - must stay light for visibility. */
    public static final Color SIDEBAR_ICON = new Color(0xCB, 0xD5, 0xE1);
    public static final Color TEXT_PRIMARY = new Color(0x0F, 0x17, 0x2A);
    public static final Color TEXT_SECONDARY = new Color(0x64, 0x74, 0x8B);
    /** Slightly softer than TEXT_PRIMARY - used for form field labels. */
    public static final Color TEXT_LABEL = new Color(0x33, 0x41, 0x55);
    public static final Color SUCCESS = new Color(0x22, 0xC5, 0x5E);
    public static final Color WARNING = new Color(0xF5, 0x9E, 0x0B);
    public static final Color DANGER = new Color(0xEF, 0x44, 0x44);
    public static final Color DANGER_HOVER = new Color(0xDC, 0x26, 0x26);
    /** Decorative border for cards/tables/separators (not form inputs - see INPUT_BORDER). */
    public static final Color BORDER = new Color(0xE2, 0xE8, 0xF0);
    /** Clearly visible neutral border for form inputs in their normal (non-focused) state. */
    public static final Color INPUT_BORDER = new Color(0x94, 0xA3, 0xB8);
    /** Default icon color on light backgrounds (buttons, form fields, table cells). */
    public static final Color ICON_COLOR = new Color(0x47, 0x55, 0x69);

    // ---- Spacing -------------------------------------------------------
    public static final int SPACE_SM = 8;
    public static final int SPACE_MD = 16;
    public static final int SPACE_LG = 24;
    public static final int SPACE_SECTION = 32;

    // ---- Typography ------------------------------------------------------
    private static final String FAMILY = resolveFontFamily();

    public static final Font FONT_PAGE_TITLE = new Font(FAMILY, Font.BOLD, 24);
    public static final Font FONT_SECTION_TITLE = new Font(FAMILY, Font.BOLD, 16);
    public static final Font FONT_LABEL_BOLD = new Font(FAMILY, Font.BOLD, 13);
    public static final Font FONT_LABEL = new Font(FAMILY, Font.PLAIN, 13);
    public static final Font FONT_FIELD = new Font(FAMILY, Font.PLAIN, 13);
    public static final Font FONT_HELPER = new Font(FAMILY, Font.PLAIN, 12);
    public static final Font FONT_CARD_VALUE = new Font(FAMILY, Font.BOLD, 28);

    private UITheme() {
    }

    private static String resolveFontFamily() {
        Set<String> available = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        if (available.contains("Inter")) {
            return "Inter";
        }
        if (available.contains("Segoe UI")) {
            return "Segoe UI";
        }
        return Font.SANS_SERIF;
    }

    /** Installs FlatLaf with app-wide rounding, spacing, and color overrides. Call once before any UI is built. */
    public static void installLookAndFeel() {
        try {
            UIManager.put("Component.accentColor", PRIMARY);
            UIManager.put("Component.focusColor", PRIMARY_SOFT);

            // Input border visibility: normal / focused / error / warning outlines.
            // (These are the exact keys FlatLaf's own text field / combo box / spinner UIs
            // paint from - overriding them fixes visibility everywhere at once instead of
            // fighting each component's border individually.)
            UIManager.put("Component.borderColor", INPUT_BORDER);
            UIManager.put("Component.disabledBorderColor", new Color(0xCB, 0xD5, 0xE1));
            UIManager.put("Component.focusedBorderColor", PRIMARY);
            UIManager.put("Component.borderWidth", 1);
            UIManager.put("Component.error.borderColor", DANGER);
            UIManager.put("Component.error.focusedBorderColor", DANGER);
            UIManager.put("Component.warning.borderColor", WARNING);
            UIManager.put("Component.warning.focusedBorderColor", WARNING);

            UIManager.put("Component.arc", 10);
            UIManager.put("Button.arc", 12);
            UIManager.put("Button.default.background", PRIMARY);
            UIManager.put("Button.default.foreground", Color.WHITE);
            UIManager.put("CheckBox.arc", 6);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("ScrollBar.width", 12);
            UIManager.put("ScrollBar.track", BACKGROUND);

            // Placeholder text must stay readable, not near-invisible.
            UIManager.put("TextField.placeholderForeground", TEXT_SECONDARY);
            UIManager.put("PasswordField.placeholderForeground", TEXT_SECONDARY);
            UIManager.put("FormattedTextField.placeholderForeground", TEXT_SECONDARY);

            // Built-in, real vector show/hide-password toggle (no emoji, no custom composite needed).
            UIManager.put("PasswordField.showRevealButton", true);
            UIManager.put("PasswordField.showCapsLock", true);

            UIManager.put("Table.showHorizontalLines", false);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 0));
            UIManager.put("Table.rowHeight", 36);
            UIManager.put("Table.selectionBackground", PRIMARY_SOFT);
            UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
            UIManager.put("TableHeader.height", 40);
            UIManager.put("TableHeader.background", Color.WHITE);
            UIManager.put("TableHeader.foreground", TEXT_SECONDARY);
            UIManager.put("Table.background", Color.WHITE);
            UIManager.put("Table.gridColor", BORDER);
            UIManager.put("PopupMenu.borderColor", BORDER);
            UIManager.put("MenuItem.selectionBackground", PRIMARY_SOFT);
            UIManager.put("MenuItem.selectionForeground", TEXT_PRIMARY);
            UIManager.put("ToolTip.background", SIDEBAR);
            UIManager.put("ToolTip.foreground", Color.WHITE);
            UIManager.put("defaultFont", FONT_FIELD);

            FlatLightLaf.setup();
        } catch (Exception ignored) {
            // Fall back to the platform default look and feel.
        }
    }

    // ---- Buttons ---------------------------------------------------------

    public static JButton primaryButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        return button;
    }

    public static JButton primaryButton(AppIcon.Name icon, String text) {
        JButton button = primaryButton(text);
        button.setIcon(AppIcon.of(icon, 16, Color.WHITE));
        button.setIconTextGap(8);
        return button;
    }

    public static JButton secondaryButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(Color.WHITE);
        button.setForeground(TEXT_PRIMARY);
        button.putClientProperty("JButton.borderColor", INPUT_BORDER);
        button.putClientProperty("JButton.focusedBorderColor", PRIMARY);
        return button;
    }

    public static JButton secondaryButton(AppIcon.Name icon, String text) {
        JButton button = secondaryButton(text);
        button.setIcon(AppIcon.of(icon, 16, ICON_COLOR));
        button.setIconTextGap(8);
        return button;
    }

    public static JButton dangerButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(DANGER);
        button.setForeground(Color.WHITE);
        return button;
    }

    public static JButton dangerButton(AppIcon.Name icon, String text) {
        JButton button = dangerButton(text);
        button.setIcon(AppIcon.of(icon, 16, Color.WHITE));
        button.setIconTextGap(8);
        return button;
    }

    public static JButton successButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(SUCCESS);
        button.setForeground(Color.WHITE);
        return button;
    }

    private static JButton baseButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_LABEL_BOLD);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(9, 18, 9, 18));
        button.putClientProperty("JButton.arc", 10);
        return button;
    }

    // ---- Inputs ------------------------------------------------------------

    public static StyledTextField textField() {
        return new StyledTextField();
    }

    public static JTextField placeholder(JTextField field, String text) {
        field.putClientProperty("JTextField.placeholderText", text);
        return field;
    }

    public static StyledPasswordField passwordField() {
        return new StyledPasswordField();
    }

    public static <T> StyledComboBox<T> comboBox() {
        return new StyledComboBox<>();
    }

    public static SearchField searchField(String placeholder) {
        return new SearchField(placeholder);
    }

    // ---- Typography helpers ------------------------------------------------

    public static JLabel pageTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_PAGE_TITLE);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SECTION_TITLE);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static JLabel subtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    public static JLabel helperText(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_HELPER);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    public static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL_BOLD);
        label.setForeground(TEXT_LABEL);
        return label;
    }

    // ---- Tables --------------------------------------------------------------

    public static void styleTable(JTable table) {
        table.setRowHeight(38);
        table.setFont(FONT_LABEL);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(PRIMARY_SOFT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        table.setRowMargin(0);
        table.getTableHeader().setFont(FONT_LABEL_BOLD);
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setForeground(TEXT_SECONDARY);
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        table.setBackground(Color.WHITE);
    }
}
