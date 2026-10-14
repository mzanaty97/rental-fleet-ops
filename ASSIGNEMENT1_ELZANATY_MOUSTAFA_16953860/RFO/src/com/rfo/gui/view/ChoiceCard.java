package com.rfo.gui.view;

import com.rfo.gui.theme.Theme;
import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JToggleButton;

/**
 * One choice in a row of cards, with a bold title and a small line under it.
 * The chosen one gets a teal outline and a tick.
 */
public class ChoiceCard extends JToggleButton {

    private final String title;
    private String subtitle;

    public ChoiceCard(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setFocusable(true);
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        setPreferredSize(new java.awt.Dimension(150, 56));
    }

    /**
     * Changes the small line under the title, for example to show a price.
     */
    public void setSubtitle(String text) {
        subtitle = text;
        repaint();
    }

    public String getTitle() {
        return title;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        boolean on = isSelected();
        if (on) {
            g2.setColor(Theme.PRIMARY_TINT);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);
            g2.setColor(Theme.PRIMARY);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, 16, 16);
        } else {
            g2.setColor(Theme.BG_SURFACE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);
            g2.setColor(Theme.BORDER_FIELD);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);
        }
        g2.setFont(Theme.F_BODY_B);
        g2.setColor(Theme.TEXT_PRIMARY);
        g2.drawString((on ? "✓ " : "") + title, 12, 22);
        g2.setFont(Theme.F_SMALL);
        g2.setColor(Theme.TEXT_SECONDARY);
        g2.drawString(subtitle, 12, 42);
        if (isFocusOwner()) {
            g2.setColor(Theme.FOCUS);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(-1, -1, w + 1, h + 1, 18, 18);
        }
        g2.dispose();
    }
}
