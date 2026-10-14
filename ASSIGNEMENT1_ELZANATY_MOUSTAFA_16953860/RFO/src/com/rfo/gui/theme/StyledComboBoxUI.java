package com.rfo.gui.theme;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

/**
 * Drop down with the same rounded outline as text fields, a small chevron and a
 * clean list.
 */
public class StyledComboBoxUI extends BasicComboBoxUI {

    public static ComponentUI createUI(JComponent c) {
        return new StyledComboBoxUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        if (comboBox.getBorder() == null || comboBox.getBorder() instanceof UIResource) {
            comboBox.setBorder(new RoundedBorder(Theme.R_CONTROL, Theme.BORDER_FIELD, new Insets(1, 1, 1, 1)));
        }
        comboBox.setOpaque(true);
    }

    @Override
    protected JButton createArrowButton() {
        return new ChevronButton();
    }

    @Override
    protected ListCellRenderer<Object> createRenderer() {
        return new ItemRenderer();
    }

    @Override
    protected ComboPopup createPopup() {
        return new BasicComboPopup(comboBox) {
            @Override
            protected void configurePopup() {
                super.configurePopup();
                setBorder(BorderFactory.createLineBorder(Theme.BORDER_CARD, 1));
            }
        };
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension d = super.getPreferredSize(c);
        d.height = Math.max(d.height, Theme.CONTROL_H);
        return d;
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        Dimension d = super.getMinimumSize(c);
        d.height = Math.max(d.height, Theme.CONTROL_H);
        return d;
    }

    /**
     * The closed combo shows the chosen item on white (or grey when disabled),
     * never in "selected" colours.
     */
    @Override
    public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
        g.setColor(comboBox.isEnabled() ? Theme.BG_SURFACE : Theme.BG_WINDOW);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
        @SuppressWarnings("unchecked")
        ListCellRenderer<Object> renderer = comboBox.getRenderer();
        Component c = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, false, false);
        c.setFont(comboBox.getFont());
        c.setForeground(comboBox.isEnabled() ? Theme.TEXT_PRIMARY : Theme.TEXT_DISABLED);
        c.setBackground(comboBox.isEnabled() ? Theme.BG_SURFACE : Theme.BG_WINDOW);
        currentValuePane.paintComponent(g, c, comboBox, bounds.x, bounds.y, bounds.width, bounds.height, false);
    }

    /**
     * One row of the list: 34 px tall, 12 px side padding, soft teal when
     * selected.
     */
    private static class ItemRenderer extends DefaultListCellRenderer implements UIResource {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, false);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setFont(Theme.F_BODY);
            setBackground(isSelected ? Theme.BG_SELECTED : Theme.BG_SURFACE);
            setForeground(Theme.TEXT_PRIMARY);
            return this;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.height = Math.max(d.height, 34);
            return d;
        }
    }

    /**
     * The arrow button on the right: no frame, just a grey chevron.
     */
    private static class ChevronButton extends JButton implements UIResource {

        ChevronButton() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFocusable(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder());
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(34, Theme.CONTROL_H);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEnabled() ? Theme.TEXT_SECONDARY : Theme.TEXT_DISABLED);
            g2.setStroke(new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            int cx = getWidth() / 2 - 1, cy = getHeight() / 2;
            g2.drawLine(cx - 5, cy - 2, cx, cy + 3);
            g2.drawLine(cx, cy + 3, cx + 5, cy - 2);
            g2.dispose();
        }
    }
}
