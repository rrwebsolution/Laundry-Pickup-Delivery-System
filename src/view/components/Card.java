package view.components;

import view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A rounded, white "card" surface used throughout the app (dashboard tiles,
 * form containers, table wrappers) so every screen shares the same
 * elevation/roundness language instead of flat default JPanels.
 */
public class Card extends JPanel {

    private static final int ARC = 16;
    private Color background = UITheme.CARD_BG;
    private Color borderColor = UITheme.BORDER;
    private boolean shadow = true;

    public Card() {
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));
    }

    public Card(LayoutManager layout) {
        this();
        setLayout(layout);
    }

    public Card withPadding(int top, int left, int bottom, int right) {
        setBorder(new EmptyBorder(top, left, bottom, right));
        return this;
    }

    public Card withBackground(Color color) {
        this.background = color;
        return this;
    }

    public Card withoutShadow() {
        this.shadow = false;
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int shadowGap = shadow ? 3 : 0;

        if (shadow) {
            g2.setColor(new Color(0x0F, 0x17, 0x2A, 18));
            g2.fillRoundRect(1, shadowGap, w - 2, h - shadowGap - 1, ARC, ARC);
        }

        g2.setColor(background);
        g2.fillRoundRect(0, 0, w - 1, h - shadowGap - 2, ARC, ARC);

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w - 2, h - shadowGap - 3, ARC, ARC);
        g2.dispose();
        super.paintComponent(g);
    }
}
