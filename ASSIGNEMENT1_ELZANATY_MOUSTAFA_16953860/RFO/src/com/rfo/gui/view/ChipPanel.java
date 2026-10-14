package com.rfo.gui.view;

import com.rfo.gui.theme.Theme;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

/**
 * A wrapping row of small pills, each showing a bold location ID and its name.
 * Used for "Can already use" (grey) and "Access that will be removed" (red). It
 * draws the pills itself and wraps at a fixed width.
 */
public class ChipPanel extends JComponent {

    private static final int CHIP_H = 28, GAP = 6, PAD = 10;

    private final int width;
    private final boolean danger;
    private final List<String[]> chips = new ArrayList<>();

    public ChipPanel(int width, boolean danger) {
        this.width = width;
        this.danger = danger;
        setOpaque(false);
    }

    /**
     * Each entry is {id, name}.
     */
    public void setChips(List<String[]> newChips) {
        chips.clear();
        chips.addAll(newChips);
        revalidate();
        repaint();
    }

    public int getChipCount() {
        return chips.size();
    }

    private int chipWidth(FontMetrics idFm, FontMetrics nameFm, String[] chip) {
        return PAD + idFm.stringWidth(chip[0]) + 6 + nameFm.stringWidth(chip[1]) + PAD;
    }

    private int rowsNeeded(FontMetrics idFm, FontMetrics nameFm) {
        int rows = 1, x = 0;
        for (String[] chip : chips) {
            int w = chipWidth(idFm, nameFm, chip);
            if (x > 0 && x + w > width) {
                rows++;
                x = 0;
            }
            x += w + GAP;
        }
        return rows;
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics idFm = getFontMetrics(Theme.F_KEY), nameFm = getFontMetrics(Theme.F_BODY);
        int rows = chips.isEmpty() ? 1 : rowsNeeded(idFm, nameFm);
        return new Dimension(width, rows * CHIP_H + (rows - 1) * GAP);
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
        FontMetrics idFm = g2.getFontMetrics(Theme.F_KEY), nameFm = g2.getFontMetrics(Theme.F_BODY);
        if (chips.isEmpty()) {
            g2.setFont(Theme.F_BODY);
            g2.setColor(Theme.TEXT_SECONDARY);
            g2.drawString("None", 0, (CHIP_H + nameFm.getAscent() - nameFm.getDescent()) / 2);
            g2.dispose();
            return;
        }
        int x = 0, y = 0;
        for (String[] chip : chips) {
            int w = chipWidth(idFm, nameFm, chip);
            if (x > 0 && x + w > width) {
                x = 0;
                y += CHIP_H + GAP;
            }
            g2.setColor(danger ? Theme.ERR_BG : Theme.BG_SUBTLE);
            g2.fillRoundRect(x, y, w, CHIP_H, CHIP_H, CHIP_H);
            g2.setColor(danger ? Theme.ERR_DARK : Theme.TEXT_PRIMARY);
            int base = y + (CHIP_H + nameFm.getAscent() - nameFm.getDescent()) / 2;
            g2.setFont(Theme.F_KEY);
            g2.drawString(chip[0], x + PAD, base);
            g2.setFont(Theme.F_BODY);
            g2.drawString(chip[1], x + PAD + idFm.stringWidth(chip[0]) + 6, base);
            x += w + GAP;
        }
        g2.dispose();
    }
}
