package com.rfo.gui.theme;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;

/**
 * Paints every JButton in the app. The look is chosen with a client property:
 * <pre>
 *   button.putClientProperty("variant", "primary");   // teal, the one main action on a screen
 *   button.putClientProperty("variant", "danger");    // red, always behind a confirm
 *   button.putClientProperty("variant", "quiet");     // no outline, for small actions
 *   button.putClientProperty("variant", "link");      // teal text, no outline, for Back
 *   button.putClientProperty("size", "compact");      // 36 px tall instead of 40
 *   (nothing)                                         // secondary: white with a grey outline
 * </pre> A button whose view called setContentAreaFilled(false) is left
 * unfilled, so small icon buttons such as the tab close button keep working.
 */
public class StyledButtonUI extends BasicButtonUI {

    public static ComponentUI createUI(JComponent c) {
        return new StyledButtonUI();
    }

    @Override
    protected void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        b.setOpaque(false);
        b.setRolloverEnabled(true);
        b.setFocusPainted(false);
        b.setFont(Theme.F_BUTTON);
        if (b.getBorder() == null || b.getBorder() instanceof UIResource) {
            b.setBorder(new PaddingBorder());
        }
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Empty padding that the UI is allowed to replace later (it implements
     * UIResource).
     */
    private static class PaddingBorder extends EmptyBorder implements UIResource {

        PaddingBorder() {
            super(0, 18, 0, 18);
        }
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension d = super.getPreferredSize(c);
        if (c.getBorder() instanceof UIResource) {          // only buttons that use our standard padding
            boolean compact = "compact".equals(c.getClientProperty("size"));
            boolean flat = "quiet".equals(variant(c)) || "link".equals(variant(c));
            d.width = Math.max(d.width, (flat || compact) ? 0 : 112);
            d.height = Math.max(d.height, compact ? 36 : Theme.CONTROL_H);
        }
        return d;
    }

    private static String variant(JComponent c) {
        Object v = c.getClientProperty("variant");
        return v == null ? "secondary" : v.toString();
    }

    @Override
    public void update(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        ButtonModel m = b.getModel();
        String variant = variant(c);
        boolean enabled = b.isEnabled();
        boolean hover = enabled && m.isRollover();
        boolean pressed = enabled && m.isPressed() && m.isArmed();

        Color fill = null, outline = null, text = Theme.TEXT_PRIMARY;
        switch (variant) {
            case "primary":
                fill = pressed ? Theme.PRIMARY_PRESSED : hover ? Theme.PRIMARY_HOVER : Theme.PRIMARY;
                text = Theme.TEXT_ON_PRIMARY;
                break;
            case "danger":
                fill = pressed ? Theme.DANGER_PRESSED : hover ? Theme.DANGER_HOVER : Theme.DANGER;
                text = Theme.TEXT_ON_PRIMARY;
                break;
            case "quiet":
                fill = pressed ? Theme.DISABLED_FILL : hover ? Theme.BG_SUBTLE : null;
                break;
            case "link":   // teal text, no outline: Back buttons
                fill = pressed ? Theme.DISABLED_FILL : hover ? Theme.BG_SUBTLE : null;
                text = pressed ? Theme.PRIMARY_PRESSED : Theme.PRIMARY;
                break;
            default: // secondary
                fill = pressed ? Theme.DISABLED_FILL : hover ? Theme.BG_SUBTLE : Theme.BG_SURFACE;
                outline = Theme.BORDER_FIELD;
        }
        if (!enabled) {
            fill = ("quiet".equals(variant) || "link".equals(variant)) ? null : Theme.DISABLED_FILL;
            outline = null;
            text = Theme.TEXT_DISABLED;
        }

        int w = c.getWidth(), h = c.getHeight();
        RoundRectangle2D body = new RoundRectangle2D.Float(0, 0, w, h, Theme.R_CONTROL * 2f, Theme.R_CONTROL * 2f);
        if (b.isContentAreaFilled() && fill != null) {
            g2.setColor(fill);
            g2.fill(body);
        }
        if (b.isBorderPainted() && b.isContentAreaFilled() && outline != null) {
            g2.setColor(outline);
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, Theme.R_CONTROL * 2f, Theme.R_CONTROL * 2f));
        }
        if (enabled && b.isFocusOwner() && Theme.isKeyboardFocus()) {
            g2.setColor(Theme.FOCUS);
            g2.setStroke(new java.awt.BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, Theme.R_CONTROL * 2f, Theme.R_CONTROL * 2f));
        }

        paintLabel(g2, b, text);
        g2.dispose();
    }

    /**
     * Draws the button text centred (and the icon, if any, to its left).
     */
    private void paintLabel(Graphics2D g2, AbstractButton b, Color text) {
        FontMetrics fm = g2.getFontMetrics(b.getFont());
        Insets in = b.getInsets();
        Rectangle view = new Rectangle(in.left, in.top, b.getWidth() - in.left - in.right, b.getHeight() - in.top - in.bottom);
        Rectangle iconR = new Rectangle(), textR = new Rectangle();
        String label = SwingUtilities.layoutCompoundLabel(b, fm, b.getText(), b.getIcon(),
                b.getVerticalAlignment(), b.getHorizontalAlignment(),
                b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
                view, iconR, textR, b.getText() == null ? 0 : b.getIconTextGap());
        if (b.getIcon() != null) {
            b.getIcon().paintIcon(b, g2, iconR.x, iconR.y);
        }
        if (label != null && !label.isEmpty()) {
            g2.setFont(b.getFont());
            g2.setColor(text);
            BasicGraphicsUtils.drawStringUnderlineCharAt(g2, label, b.getDisplayedMnemonicIndex(),
                    textR.x, textR.y + fm.getAscent());
        }
    }
}
