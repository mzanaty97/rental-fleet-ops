package com.rfo.gui.view;

import com.rfo.gui.theme.Theme;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JToggleButton;

/**
 * A small on/off switch. Click or press Space to flip it.
 */
public class SwitchToggle extends JToggleButton {

    public SwitchToggle() {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setFocusable(true);
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(44, 24);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isSelected() ? Theme.PRIMARY : Theme.BORDER_DASHED);
        g2.fillRoundRect(0, 0, 44, 24, 24, 24);
        g2.setColor(Theme.BG_SURFACE);
        g2.fillOval(isSelected() ? 23 : 3, 3, 18, 18);
        if (isFocusOwner()) {
            g2.setColor(Theme.FOCUS);
            g2.setStroke(new java.awt.BasicStroke(2f));
            g2.drawRoundRect(-1, -1, 45, 25, 26, 26);
        }
        g2.dispose();
    }
}
