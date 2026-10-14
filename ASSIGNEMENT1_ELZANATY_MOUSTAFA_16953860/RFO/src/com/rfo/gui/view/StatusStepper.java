package com.rfo.gui.view;

import com.rfo.gui.theme.Theme;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/**
 * The three stages of a rental: OPEN, INCOMPLETE, CLOSED. The current one is
 * solid, earlier ones are ticked.
 */
public class StatusStepper extends JComponent {

    private static final String[] NAMES = {"OPEN", "INCOMPLETE", "CLOSED"};
    private static final Color[] SOLID = {new Color(0x1F6FB2), new Color(0xD97706), new Color(0x4A5568)};

    private int stage;
    private final boolean numbered;

    public StatusStepper(int stage, boolean numbered) {
        this.stage = stage;
        this.numbered = numbered;
        setOpaque(false);
    }

    public void setStage(int stage) {
        this.stage = stage;
        repaint();
    }

    private String label(int i) {
        String base = i < stage ? "✓ " + NAMES[i] : i == stage && !numbered ? "● " + NAMES[i] : NAMES[i];
        return numbered ? (i + 1) + " " + base : base;
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(Theme.F_BADGE);
        int w = 0;
        for (int i = 0; i < 3; i++) {
            w += fm.stringWidth(label(i)) + 20;
        }
        return new Dimension(w + 2 * 14 + 6, 24);
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
        g2.setFont(Theme.F_BADGE);
        FontMetrics fm = g2.getFontMetrics();
        int x = 0, y = 1;
        for (int i = 0; i < 3; i++) {
            String text = label(i);
            int w = fm.stringWidth(text) + 20;
            Color fill = i == stage ? SOLID[i] : i < stage ? new Color(0xECEFF3) : Theme.BG_SUBTLE;
            Color ink = i == stage ? Color.WHITE : i < stage ? new Color(0x4A5568) : Theme.TEXT_SECONDARY;
            g2.setColor(fill);
            g2.fillRoundRect(x, y, w, 22, 22, 22);
            g2.setColor(ink);
            g2.drawString(text, x + 10, y + (22 + fm.getAscent() - fm.getDescent()) / 2);
            x += w;
            if (i < 2) {
                g2.setColor(i < stage ? SOLID[i + 1] : Theme.BORDER_KEY);
                g2.fillRect(x + 3, y + 10, 8, 2);
                x += 14;
            }
        }
        g2.dispose();
    }
}
