package com.rfo.gui.view;

import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.RoundedBorder;
import com.rfo.gui.theme.Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * Startup tab: the logo, the app name, a big New Session button (or press
 * Enter) and a short note about how tabs work. It only displays; the controller
 * opens the login tab.
 */
public class HomeView extends JPanel {

    /**
     * Kept for older code that still refers to the navy colour through this
     * class.
     */
    public static final Color NAVY = Theme.NAVY_900;

    private final JButton newSessionButton = new JButton("New Session");

    public HomeView() {
        super(new GridBagLayout());
        setBackground(Theme.BG_WINDOW);

        JLabel title = new JLabel("RentalFleetOps");
        title.setFont(Theme.F_DISPLAY);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Rental fleet management system");
        subtitle.setFont(Theme.F_BODY.deriveFont(15f));
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel brand = column();
        LogoTile logo = new LogoTile();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        brand.add(logo);
        brand.add(Box.createVerticalStrut(16));
        brand.add(title);
        brand.add(Box.createVerticalStrut(6));
        brand.add(subtitle);

        newSessionButton.putClientProperty("variant", "primary");
        newSessionButton.setFont(Theme.F_BUTTON.deriveFont(15f));
        newSessionButton.setIcon(Icons.icon(Icons.Type.PLUS, 18, Theme.TEXT_ON_PRIMARY));
        newSessionButton.setIconTextGap(10);
        newSessionButton.setPreferredSize(new Dimension(220, 48));
        newSessionButton.setToolTipText("Open a new login tab");
        newSessionButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel hint = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        hint.setOpaque(false);
        JLabel or = new JLabel("or press");
        or.setFont(Theme.F_BODY.deriveFont(13f));
        or.setForeground(Theme.TEXT_SECONDARY);
        hint.add(or);
        hint.add(new KeyCap("Enter ↵"));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel action = column();
        action.add(newSessionButton);
        action.add(Box.createVerticalStrut(12));
        action.add(hint);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        add(brand, c);
        c.gridy = 1;
        c.insets = new Insets(48, 0, 0, 0);
        add(action, c);

        installEnterKey();
    }

    private static JPanel column() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private JComponent infoCard() {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        card.setBackground(Theme.BG_SURFACE);
        card.setBorder(new RoundedBorder(8, Theme.BORDER_CARD, new Insets(12, 16, 12, 16), false));
        JLabel icon = new JLabel(Icons.icon(Icons.Type.INFO, 18, Theme.INFO_FG));
        JLabel text = new JLabel("<html><div style='width:420px'>Each staff member gets their own tab. "
                + "The coloured initials show who is signed in and their role.</div></html>");
        text.setFont(Theme.F_LABEL.deriveFont(java.awt.Font.PLAIN, 13f));
        text.setForeground(Theme.TEXT_SECONDARY);
        card.add(icon);
        card.add(text);
        return card;
    }

    // Enter starts a session whenever the Home tab is the one showing
    private void installEnterKey() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "newSession");
        getActionMap().put("newSession", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newSessionButton.doClick();
            }
        });
    }

    public void addController(ActionListener controller) {
        newSessionButton.addActionListener(controller);
    }

    /**
     * The navy rounded square with a white car outline and a teal roof line.
     */
    private static class LogoTile extends JComponent {

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(72, 72);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setColor(Theme.NAVY_900);
            g2.fill(new RoundRectangle2D.Float(0, 0, 72, 72, 32, 32));

            g2.translate(14, 14);                        // the car is drawn on a 44 x 44 grid
            g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Color.WHITE);
            Path2D.Float body = new Path2D.Float();
            body.moveTo(8, 27);
            body.lineTo(11.5, 18);
            body.curveTo(12, 16.8, 13, 16, 14.3, 16);
            body.lineTo(29.7, 16);
            body.curveTo(31, 16, 32, 16.8, 32.5, 18);
            body.lineTo(36, 27);
            g2.draw(body);
            g2.draw(new RoundRectangle2D.Float(6, 27, 32, 7, 5, 5));
            Path2D.Float legs = new Path2D.Float();
            legs.moveTo(11, 34);
            legs.lineTo(11, 37);
            legs.moveTo(33, 34);
            legs.lineTo(33, 37);
            g2.draw(legs);
            g2.fill(new Ellipse2D.Float(12, 29.5f, 2, 2));
            g2.fill(new Ellipse2D.Float(30, 29.5f, 2, 2));
            g2.setColor(new Color(0x5EC2B6));
            Path2D.Float roof = new Path2D.Float();
            roof.moveTo(15, 10);
            roof.lineTo(29, 10);
            g2.draw(roof);
            g2.dispose();
        }
    }

    /**
     * A small keyboard key, drawn like the "key badge" in the design.
     */
    private static class KeyCap extends JComponent {

        private final String text;

        KeyCap(String text) {
            this.text = text;
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Theme.F_KEY.deriveFont(12f));
            return new Dimension(fm.stringWidth(text) + 18, 22);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Theme.BG_SURFACE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.setColor(Theme.BORDER_KEY);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.setFont(Theme.F_KEY.deriveFont(12f));
            g2.setColor(Theme.TEXT_PRIMARY);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, 9, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }
}
