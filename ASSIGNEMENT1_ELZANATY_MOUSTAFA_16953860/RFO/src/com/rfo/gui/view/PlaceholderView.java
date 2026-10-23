package com.rfo.gui.view;

import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.Theme;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * Screen for a menu option that has not been built yet: a friendly dashed card
 * and a Back button.
 */
public class PlaceholderView extends JPanel {

    public PlaceholderView(String name) {
        super(new GridBagLayout());
        setOpaque(false);

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), Theme.R_CARD * 2, Theme.R_CARD * 2));
                g2.setColor(Theme.BORDER_DASHED);
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[]{4f, 3f}, 0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, Theme.R_CARD * 2, Theme.R_CARD * 2));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 36, 32, 36));

        JComponent disc = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(64, 64);
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PRIMARY_TINT);
                g2.fillOval(0, 0, 64, 64);
                Icons.paint(g2, Icons.Type.CLOCK, 16, 16, 32, Theme.PRIMARY, 1.6f);
                g2.dispose();
            }
        };
        disc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel(name + " is on its way", SwingConstants.CENTER);
        title.setFont(Theme.F_HEADING);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel text = new JLabel("<html><div style='text-align:center;width:360px'>This screen is not available yet. "
                + "Nothing has been changed. You can go back and use another option.</div></html>", SwingConstants.CENTER);
        text.setFont(Theme.F_BODY);
        text.setForeground(Theme.TEXT_SECONDARY);
        text.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton back = new JButton("Back");
        back.putClientProperty("variant", "primary");
        back.setIcon(Icons.icon(Icons.Type.CHEVRON_LEFT, 16, Theme.TEXT_ON_PRIMARY));
        back.setIconTextGap(8);
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> {
            Component frame = SwingUtilities.getAncestorOfClass(ScreenFrame.class, this);
            if (frame != null) {
                ((ScreenFrame) frame).goBack();
            }
        });
        JLabel hint = new JLabel("or press Backspace", SwingConstants.CENTER);
        hint.setFont(Theme.F_SMALL);
        hint.setForeground(Theme.TEXT_SECONDARY);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(disc);
        card.add(Box.createVerticalStrut(20));
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(text);
        card.add(Box.createVerticalStrut(20));
        card.add(back);
        card.add(Box.createVerticalStrut(10));
        card.add(hint);
        add(card);
    }
}
