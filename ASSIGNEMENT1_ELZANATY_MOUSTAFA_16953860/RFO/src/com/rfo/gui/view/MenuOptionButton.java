package com.rfo.gui.view;

import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * One row of a menu: a number badge, the option name, a one-line description
 * and a chevron. It draws itself in five looks: rest, hover, "matched" (its
 * number is typed, so Enter opens it), coming soon (dashed, still selectable)
 * and Back (quiet, at the bottom).
 */
public class MenuOptionButton extends JButton {

    public enum Kind {
        NORMAL, SOON, BACK
    }

    private final int number;
    private final String label;
    private final String description;
    private final Kind kind;
    private final int height;
    private boolean matched = false;

    public MenuOptionButton(int number, String label, String description, Kind kind, int height) {
        super();
        this.number = number;
        this.label = label;
        this.description = description;
        this.kind = kind;
        this.height = height;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        getAccessibleContext().setAccessibleName(number + ". " + label);
    }

    /**
     * True while this option's number is the one typed in the number box.
     */
    public void setMatched(boolean matched) {
        if (this.matched != matched) {
            this.matched = matched;
            repaint();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Theme.FORM_MAX_W, height);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(240, height);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Theme.FORM_MAX_W, height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        boolean hover = getModel().isRollover();
        float arc = Theme.R_OPTION * 2f;

        // ---- body
        if (kind == Kind.BACK) {
            if (hover) {
                g2.setColor(Theme.BG_SUBTLE);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
            }
        } else {
            Color fill = matched ? Theme.PRIMARY_TINT : hover ? Theme.BG_HOVER
                    : kind == Kind.SOON ? new Color(0xFBFCFD) : Theme.BG_SURFACE;
            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
            if (matched) {
                g2.setColor(Theme.PRIMARY);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, arc, arc));
            } else if (kind == Kind.SOON) {
                g2.setColor(hover ? Theme.PRIMARY : Theme.BORDER_DASHED);
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[]{4f, 3f}, 0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));
            } else {
                g2.setColor(hover ? Theme.PRIMARY : Theme.BORDER_CARD);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));
            }
        }

        // ---- number badge
        int kx = 16, ky = (h - 28) / 2;
        boolean solid = matched && kind != Kind.BACK;
        g2.setColor(solid ? Theme.PRIMARY : (kind == Kind.NORMAL ? Theme.BG_SUBTLE : Theme.BG_SURFACE));
        g2.fill(new RoundRectangle2D.Float(kx, ky, 28, 28, 12, 12));
        g2.setColor(solid ? Theme.PRIMARY : Theme.BORDER_KEY);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(kx + 0.5f, ky + 0.5f, 27, 27, 12, 12));
        g2.setFont(Theme.F_KEY);
        FontMetrics fm = g2.getFontMetrics();
        String n = String.valueOf(number);
        g2.setColor(solid ? Color.WHITE : Theme.TEXT_PRIMARY);
        g2.drawString(n, kx + (28 - fm.stringWidth(n)) / 2, ky + (28 + fm.getAscent() - fm.getDescent()) / 2);

        // ---- text
        int tx = kx + 28 + 14;
        if (kind == Kind.BACK) {
            Icons.paint(g2, Icons.Type.CHEVRON_LEFT, tx, (h - 16) / 2, 16, Theme.TEXT_SECONDARY, 2f);
            g2.setFont(Theme.F_BODY_B);
            fm = g2.getFontMetrics();
            g2.setColor(Theme.TEXT_SECONDARY);
            g2.drawString(label, tx + 16 + 8, (h + fm.getAscent() - fm.getDescent()) / 2);
        } else {
            g2.setFont(Theme.F_OPTION);
            FontMetrics lf = g2.getFontMetrics();
            g2.setFont(Theme.F_SMALL);
            FontMetrics df = g2.getFontMetrics();
            int block = lf.getHeight() + 2 + (description.isEmpty() ? 0 : df.getHeight());
            int top = (h - block) / 2;
            g2.setFont(Theme.F_OPTION);
            g2.setColor(kind == Kind.SOON ? Theme.TEXT_SECONDARY : Theme.TEXT_PRIMARY);
            g2.drawString(label, tx, top + lf.getAscent());
            if (!description.isEmpty()) {
                g2.setFont(Theme.F_SMALL);
                g2.setColor(Theme.TEXT_SECONDARY);
                g2.drawString(description, tx, top + lf.getHeight() + 2 + df.getAscent());
            }
        }

        // ---- right side
        int right = w - 16;
        if (matched && kind != Kind.BACK) {
            String t = "Enter ↵";
            g2.setFont(Theme.F_KEY.deriveFont(12f));
            FontMetrics pf = g2.getFontMetrics();
            int pw = pf.stringWidth(t) + 16, ph = 24;
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(right - pw, (h - ph) / 2f, pw, ph, 8, 8));
            g2.setColor(Theme.PRIMARY_TINT_BORDER);
            g2.draw(new RoundRectangle2D.Float(right - pw + 0.5f, (h - ph) / 2f + 0.5f, pw - 1, ph - 1, 8, 8));
            g2.setColor(Theme.PRIMARY_TINT_TEXT);
            g2.drawString(t, right - pw + 8, (h + pf.getAscent() - pf.getDescent()) / 2);
        } else if (kind == Kind.SOON) {
            String t = "COMING SOON";
            g2.setFont(Theme.F_BADGE);
            FontMetrics pf = g2.getFontMetrics();
            int pw = pf.stringWidth(t) + 20, ph = 22;
            g2.setColor(Theme.BG_SUBTLE);
            g2.fillRoundRect(right - pw, (h - ph) / 2, pw, ph, ph, ph);
            g2.setColor(Theme.TEXT_SECONDARY);
            g2.drawString(t, right - pw + 10, (h + pf.getAscent() - pf.getDescent()) / 2);
        } else if (kind == Kind.NORMAL) {
            Icons.paint(g2, Icons.Type.CHEVRON_RIGHT, right - 18, (h - 18) / 2, 18, hover ? Theme.PRIMARY : Theme.TEXT_SECONDARY, 2f);
        }

        // ---- keyboard focus ring
        if (isFocusOwner() && Theme.isKeyboardFocus()) {
            g2.setColor(Theme.FOCUS);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, arc, arc));
        }
        g2.dispose();
    }
}
