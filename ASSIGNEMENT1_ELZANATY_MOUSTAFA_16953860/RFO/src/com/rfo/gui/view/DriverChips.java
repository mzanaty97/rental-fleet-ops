package com.rfo.gui.view;

import com.rfo.gui.theme.Theme;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * A row of driver ID pills. Click a pill to remove that driver.
 */
public class DriverChips extends JPanel {

    private final List<Integer> ids = new ArrayList<>();
    private final Runnable onChange;

    public DriverChips(Runnable onChange) {
        super();
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));
        this.onChange = onChange;
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, ids.isEmpty() ? 0 : 30);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    public List<Integer> getIds() {
        return new ArrayList<>(ids);
    }

    public boolean contains(int id) {
        return ids.contains(id);
    }

    public boolean isEmpty() {
        return ids.isEmpty();
    }

    public void add(int id) {
        ids.add(id);
        rebuild();
    }

    public void clear() {
        ids.clear();
        rebuild();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (java.awt.Component c : getComponents()) {
            c.setEnabled(enabled);
        }
    }

    private void rebuild() {
        removeAll();
        for (Integer id : ids) {
            add(new Chip(id));
            add(javax.swing.Box.createHorizontalStrut(8));
        }
        add(javax.swing.Box.createHorizontalGlue());
        revalidate();
        repaint();
        onChange.run();
    }

    private class Chip extends JComponent {

        private final int id;

        Chip(int id) {
            this.id = id;
            setToolTipText("Click to remove this driver");
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled()) {
                        ids.remove(Integer.valueOf(Chip.this.id));
                        rebuild();
                    }
                }
            });
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Theme.F_KEY);
            return new Dimension(12 + fm.stringWidth(String.valueOf(id)) + 8 + 14 + 10, 30);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(isEnabled() ? Theme.PRIMARY_TINT : Theme.BG_SUBTLE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.setColor(isEnabled() ? Theme.PRIMARY_TINT_BORDER : Theme.BORDER_DIVIDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.setFont(Theme.F_KEY);
            g2.setColor(isEnabled() ? Theme.PRIMARY_TINT_TEXT : Theme.TEXT_SECONDARY);
            FontMetrics fm = g2.getFontMetrics();
            String text = String.valueOf(id);
            g2.drawString(text, 12, (h + fm.getAscent() - fm.getDescent()) / 2);
            int x = 12 + fm.stringWidth(text) + 10, y = h / 2;
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x, y - 4, x + 8, y + 4);
            g2.drawLine(x + 8, y - 4, x, y + 4);
            g2.dispose();
        }
    }
}
