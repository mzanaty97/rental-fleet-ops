package com.rfo.gui.theme;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * Slim 10 px scroll bar: a rounded grey thumb, no arrow buttons, no visible
 * track.
 */
public class StyledScrollBarUI extends BasicScrollBarUI {

    public static ComponentUI createUI(JComponent c) {
        return new StyledScrollBarUI();
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return zeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return zeroButton();
    }

    private JButton zeroButton() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        b.setMinimumSize(new Dimension(0, 0));
        b.setMaximumSize(new Dimension(0, 0));
        return b;
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return new Dimension(10, 10);
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle bounds) {
        // transparent track: nothing to draw
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        if (r.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color color = (isDragging || isThumbRollover()) ? Theme.BORDER_FIELD : Theme.BORDER_DASHED;
        g2.setColor(color);
        g2.fillRoundRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 8, 8);
        g2.dispose();
    }
}
