package com.rfo.gui.theme;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * A small pill with coloured text, used for roles, statuses and "COMING SOON".
 * The text itself is the lookup key into {@link Theme#badge(String)}, so the
 * colour is never the only signal.
 */
public class BadgeLabel extends JComponent {

    private String text;
    private final boolean dot;

    public BadgeLabel(String text, boolean dot) {
        this.text = text;
        this.dot = dot;
        setOpaque(false);
    }

    public void setText(String text) {
        this.text = text;
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(Theme.F_BADGE);
        int w = 10 + fm.stringWidth(text) + 10 + (dot ? 13 : 0);
        return new Dimension(w, 22);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Theme.Badge badge = Theme.badge(text);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int h = getHeight(), w = getWidth();
        int top = (h - 22) / 2;
        g2.setColor(badge.fill);
        g2.fillRoundRect(0, top, w, 22, 22, 22);
        g2.setFont(Theme.F_BADGE);
        FontMetrics fm = g2.getFontMetrics();
        int x = 10;
        if (dot) {
            g2.setColor(badge.dot);
            g2.fillOval(x, top + 7, 7, 7);
            x += 13;
        }
        g2.setColor(badge.text);
        g2.drawString(text, x, top + (22 + fm.getAscent() - fm.getDescent()) / 2);
        g2.dispose();
    }
}
