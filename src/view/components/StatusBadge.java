package view.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** A small rounded "pill" label used to render statuses consistently across every table/screen. */
public class StatusBadge extends JLabel {

    private Color fill;

    public StatusBadge(String text, Color fill, Color foreground) {
        super(text);
        this.fill = fill;
        setFont(getFont().deriveFont(Font.BOLD, 12f));
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(new EmptyBorder(4, 12, 4, 12));
        setForeground(foreground);
        setOpaque(false);
    }

    public void update(String text, Color fill, Color foreground) {
        setText(text);
        this.fill = fill;
        setForeground(foreground);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, 24));
    }
}
