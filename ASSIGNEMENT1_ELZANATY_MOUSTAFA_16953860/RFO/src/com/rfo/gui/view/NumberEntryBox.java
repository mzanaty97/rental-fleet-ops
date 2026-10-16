package com.rfo.gui.view;

import com.rfo.gui.theme.Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * The small box under a menu that shows the number being typed before Enter is
 * pressed. Quiet and dashed when nothing is typed, dark navy with white digits
 * while typing, and ringed in red when the number does not exist.
 */
public class NumberEntryBox extends JComponent {

    private String typed = "";
    private boolean error = false;

    public NumberEntryBox() {
        setOpaque(false);
    }

    public void setState(String typed, boolean error) {
        this.typed = typed;
        this.error = error;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(64, Theme.BUTTON_LARGE_H);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
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
        int w = getWidth(), h = getHeight();
        float arc = Theme.R_CONTROL * 2f;

        if (typed.isEmpty()) {
            g2.setColor(Theme.BG_SUBTLE);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
            g2.setColor(Theme.BORDER_DASHED);
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[]{4f, 3f}, 0f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));
            g2.setFont(Theme.F_NUMERAL);
            drawCentered(g2, "_", Theme.TEXT_DISABLED, null);
        } else {
            g2.setColor(Theme.NAVY_900);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
            if (error) {
                g2.setColor(Theme.ERR_FG);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, arc, arc));
            }
            g2.setFont(Theme.F_NUMERAL);
            drawCentered(g2, typed, Color.WHITE, error ? null : new Color(0x7FC8BF));
        }
        g2.dispose();
    }

    /**
     * Draws the text centred; {@code cursor} (a colour) adds a trailing
     * underscore like a blinking cursor.
     */
    private void drawCentered(Graphics2D g2, String text, Color color, Color cursor) {
        FontMetrics fm = g2.getFontMetrics();
        int total = fm.stringWidth(text) + (cursor != null ? fm.stringWidth("_") : 0);
        int x = (getWidth() - total) / 2;
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.setColor(color);
        g2.drawString(text, x, y);
        if (cursor != null) {
            g2.setColor(cursor);
            g2.drawString("_", x + fm.stringWidth(text), y);
        }
    }
}
