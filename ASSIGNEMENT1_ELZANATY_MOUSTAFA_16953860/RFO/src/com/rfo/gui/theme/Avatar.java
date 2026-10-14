package com.rfo.gui.theme;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/**
 * A filled circle with the person's initials, coloured by role. Used in the tab
 * and the session header.
 */
public class Avatar extends JComponent {

    private final String initials;
    private final Color fill;
    private final int size;
    private final float fontSize;

    public Avatar(String initials, Color fill, int size, float fontSize) {
        this.initials = initials;
        this.fill = fill;
        this.size = size;
        this.fontSize = fontSize;
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(size, size);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int x = (getWidth() - size) / 2, y = (getHeight() - size) / 2;
        g2.setColor(fill);
        g2.fillOval(x, y, size, size);
        g2.setColor(Color.WHITE);
        g2.setFont(Theme.F_BADGE.deriveFont(Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(initials, x + (size - fm.stringWidth(initials)) / 2,
                y + (size + fm.getAscent() - fm.getDescent()) / 2);
        g2.dispose();
    }
}
