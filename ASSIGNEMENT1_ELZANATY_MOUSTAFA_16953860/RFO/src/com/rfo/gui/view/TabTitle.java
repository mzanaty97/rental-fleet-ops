package com.rfo.gui.view;

import com.rfo.gui.theme.Avatar;
import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.StyledTabbedPaneUI;
import com.rfo.gui.theme.Theme;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * What a tab looks like. It paints its own background: navy when idle, a
 * lighter navy when the mouse is over it, and the pale window colour with a
 * coloured stripe on top when it is the selected tab (so it looks joined to the
 * page below). There are three kinds: Home (house icon, no close button), a
 * login tab (dashed circle, "New Session") and a session tab (initials circle
 * in the staff member's role colour).
 */
public class TabTitle extends JPanel {

    private final Color accent;
    private final boolean showCloseAlways;
    private final String baseLabel;
    private final TabLabel label;
    private final CloseButton close;

    public static TabTitle home() {
        return new TabTitle("Home", null, Icons.Type.HOME, null, null, Theme.PRIMARY, null, true);
    }

    public static TabTitle login(ActionListener closeAction) {
        return new TabTitle("New Session", null, Icons.Type.DASHED_CIRCLE, null, null, Theme.BORDER_FIELD, closeAction, false);
    }

    /**
     * @param text what the tab shows, @param base the same without any " · 2"
     * suffix
     */
    public static TabTitle session(String text, String base, String initials, Color roleColor, ActionListener closeAction) {
        return new TabTitle(text, base, null, initials, roleColor, roleColor, closeAction, true);
    }

    private TabTitle(String text, String baseLabel, Icons.Type icon, String initials, Color avatarColor,
            Color accent, ActionListener closeAction, boolean showCloseAlways) {
        super();
        this.accent = accent;
        this.showCloseAlways = showCloseAlways;
        this.baseLabel = baseLabel;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(new EmptyBorder(3, 14, 0, closeAction == null ? 14 : 8));

        if (initials != null) {
            add(new Avatar(initials, avatarColor, 20, 9f));
        } else {
            add(new IconBox(icon));
        }
        add(Box.createHorizontalStrut(8));
        label = new TabLabel(text);
        add(label);

        if (closeAction != null) {
            add(Box.createHorizontalStrut(8));
            close = new CloseButton();
            close.addActionListener(closeAction);
            add(close);
        } else {
            close = null;
        }
    }

    /**
     * The text without a " · 2" suffix; used to number a second tab for the
     * same person.
     */
    public String getBaseLabel() {
        return baseLabel;
    }

    public void setTitle(String title) {
        label.setText(title);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = Theme.TAB_H;
        return d;
    }

    // ------------------------------------------------------------------ state
    private JTabbedPane tabs() {
        return (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
    }

    private boolean isSelectedTab() {
        JTabbedPane t = tabs();
        return t != null && t.indexOfTabComponent(this) == t.getSelectedIndex();
    }

    private boolean isHoveredTab() {
        JTabbedPane t = tabs();
        if (t == null || !(t.getUI() instanceof StyledTabbedPaneUI)) {
            return false;
        }
        return t.indexOfTabComponent(this) == ((StyledTabbedPaneUI) t.getUI()).getHoverTab();
    }

    // ------------------------------------------------------------------ painting
    @Override
    protected void paintComponent(Graphics g) {
        boolean selected = isSelectedTab();
        boolean hover = isHoveredTab();
        if (!selected && !hover) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // the tab is a rectangle with rounded top corners; 2 px is left free on each side as the gap between tabs
        int x = 2, w = getWidth() - 4, h = getHeight();
        Area shape = new Area(new RoundRectangle2D.Float(x, 0, w, h + 8, 16, 16));
        shape.intersect(new Area(new Rectangle2D.Float(x, 0, w, h)));
        g2.setColor(selected ? Theme.BG_WINDOW : Theme.NAVY_800);
        g2.fill(shape);
        if (selected) {
            g2.setClip(shape);
            g2.setColor(accent);
            g2.fillRect(x, 0, w, 3);
        }
        g2.dispose();
    }

    /**
     * The tab text; its colour follows the selected state.
     */
    private class TabLabel extends JLabel {

        TabLabel(String text) {
            super(text);
            setFont(Theme.F_LABEL);
        }

        @Override
        public java.awt.Color getForeground() {
            return isSelectedTab() ? Theme.TEXT_PRIMARY : Theme.TEXT_ON_NAVY;
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(Math.min(180, fm.stringWidth(getText()) + 2), fm.getHeight());
        }
    }

    /**
     * The house or dashed circle in front of the Home and login tabs.
     */
    private class IconBox extends JComponent {

        private final Icons.Type type;

        IconBox(Icons.Type type) {
            this.type = type;
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(16, 16);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Icons.paint((Graphics2D) g, type, 0, (getHeight() - 16) / 2, 16, isSelectedTab() ? Theme.TEXT_PRIMARY : Theme.TEXT_ON_NAVY, 2f);
        }
    }

    /**
     * The small x. Always takes its space so the tab width never jumps; login
     * tabs only draw it on hover.
     */
    private class CloseButton extends JButton {

        CloseButton() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFocusable(false);
            setOpaque(false);
            setToolTipText("Close this tab");
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRolloverEnabled(true);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(20, 20);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            boolean selected = isSelectedTab();
            if (!showCloseAlways && !selected && !isHoveredTab()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isRollover()) {
                g2.setColor(selected ? Theme.BG_SUBTLE : new Color(0x3A4868));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }
            Color c = selected ? Theme.TEXT_SECONDARY : Theme.TEXT_ON_NAVY;
            Icons.paint(g2, Icons.Type.CLOSE, (getWidth() - 11) / 2, (getHeight() - 11) / 2, 11, c, 2.5f);
            g2.dispose();
        }
    }
}
