package com.rfo.gui.theme;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.UIResource;

/**
 * The outline around text fields and combo boxes: a thin rounded line that
 * becomes a 2 px blue ring while the field has the keyboard focus, or a 2 px
 * red ring when a view marks the field as wrong with
 * {@code field.putClientProperty("error", Boolean.TRUE)}.
 */
public class RoundedBorder extends AbstractBorder implements UIResource {

    private final int radius;
    private final Color color;
    private final Insets padding;
    private final boolean focusRing;

    public RoundedBorder(int radius, Color color, Insets padding) {
        this(radius, color, padding, true);
    }

    /**
     * @param focusRing false for cards, which should not light up when a field
     * inside them has focus
     */
    public RoundedBorder(int radius, Color color, Insets padding, boolean focusRing) {
        this.radius = radius;
        this.color = color;
        this.padding = padding;
        this.focusRing = focusRing;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean error = c instanceof JComponent && Boolean.TRUE.equals(((JComponent) c).getClientProperty("error"));
        boolean focused = focusRing && isFocused(c);
        Color line = error ? Theme.ERR_FG : (focused ? Theme.FOCUS : color);
        float width = (error || focused) ? 2f : 1f;

        // The component itself is a plain rectangle, so repaint the four corners in the
        // surrounding colour; that is what makes the outline look rounded.
        Area corners = new Area(new Rectangle2D.Float(x, y, w, h));
        corners.subtract(new Area(new RoundRectangle2D.Float(x, y, w, h, radius * 2f, radius * 2f)));
        Component parent = c.getParent();
        g2.setColor(parent != null ? parent.getBackground() : Theme.BG_WINDOW);
        g2.fill(corners);

        g2.setColor(line);
        g2.setStroke(new BasicStroke(width));
        float half = width / 2f;
        g2.draw(new RoundRectangle2D.Float(x + half, y + half, w - width, h - width, radius * 2f, radius * 2f));
        g2.dispose();
    }

    private static boolean isFocused(Component c) {
        Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        return owner != null && (owner == c || SwingUtilities.isDescendingFrom(owner, c));
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(padding.top, padding.left, padding.bottom, padding.right);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(padding.top, padding.left, padding.bottom, padding.right);
        return insets;
    }
}
