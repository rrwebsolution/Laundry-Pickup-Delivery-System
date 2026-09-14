package view.components;

import org.kordamp.ikonli.swing.FontIcon;
import view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A sidebar navigation row: icon + label, with hover/active states and a left accent bar
 * when active. Icon color is explicitly light against the dark sidebar background, and
 * switches to white when active, per the app's contrast rules.
 */
public class SidebarButton extends JButton {

    private final FontIcon icon;
    private boolean active = false;
    private boolean hovering = false;

    public SidebarButton(AppIcon.Name iconName, String label) {
        super(label);
        this.icon = AppIcon.of(iconName, 20, UITheme.SIDEBAR_ICON);
        setIcon(icon);
        setIconTextGap(14);
        setHorizontalAlignment(SwingConstants.LEFT);
        setFont(UITheme.FONT_LABEL_BOLD);
        setForeground(UITheme.SIDEBAR_TEXT);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(0, 18, 0, 10));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        setPreferredSize(new Dimension(220, 46));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovering = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovering = false;
                repaint();
            }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        setForeground(active ? Color.WHITE : UITheme.SIDEBAR_TEXT);
        icon.setIconColor(active ? Color.WHITE : UITheme.SIDEBAR_ICON);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Color bg = active ? UITheme.SIDEBAR_HOVER : (hovering ? UITheme.SIDEBAR_HOVER : UITheme.SIDEBAR);
        g.setColor(bg);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (active) {
            g.setColor(UITheme.PRIMARY);
            g.fillRect(0, 0, 4, getHeight());
        }
        super.paintComponent(g);
    }
}
