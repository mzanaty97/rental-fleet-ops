package com.rfo.gui.theme;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * Styles every JTable: 40 px rows, zebra stripes, a hover row, a soft teal
 * selection, a flat header, right-aligned numbers with thousands separators,
 * and coloured badges for the columns called "Status", "Active" and "Role".
 */
public class StyledTableUI extends BasicTableUI {

    private static final String HOVER_ROW = "rfo.hoverRow";
    private MouseAdapter hoverTracker;

    public static ComponentUI createUI(JComponent c) {
        return new StyledTableUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        table.setRowHeight(Theme.ROW_H);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Theme.BORDER_DIVIDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new CellRenderer());
        table.setDefaultRenderer(Number.class, new CellRenderer());
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setDefaultRenderer(new HeaderRenderer());
            header.setReorderingAllowed(false);
        }
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        hoverTracker = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                setHover(table.rowAtPoint(e.getPoint()));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setHover(-1);
            }
        };
        table.addMouseMotionListener(hoverTracker);
        table.addMouseListener(hoverTracker);
    }

    @Override
    protected void uninstallListeners() {
        table.removeMouseMotionListener(hoverTracker);
        table.removeMouseListener(hoverTracker);
        super.uninstallListeners();
    }

    private void setHover(int row) {
        Object old = table.getClientProperty(HOVER_ROW);
        if (old == null || (Integer) old != row) {
            table.putClientProperty(HOVER_ROW, row);
            table.repaint();
        }
    }

    // ------------------------------------------------------------------ cells
    private static class CellRenderer extends JLabel implements TableCellRenderer, UIResource {

        private String badgeText;
        private Theme.Badge badge;

        CellRenderer() {
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean focus, int row, int column) {
            String columnName = table.getColumnName(column);
            Object hover = table.getClientProperty(HOVER_ROW);
            boolean hovered = hover != null && (Integer) hover == row;

            setBackground(selected ? Theme.BG_SELECTED : hovered ? Theme.BG_HOVER
                    : (row % 2 == 1 ? Theme.BG_ZEBRA : Theme.BG_SURFACE));
            setForeground(Theme.TEXT_PRIMARY);
            setFont(selected ? Theme.F_BODY_B : Theme.F_BODY);

            badge = null;
            badgeText = null;
            String text = value == null ? "" : value.toString();
            if (value instanceof Integer || value instanceof Long) {
                text = NumberFormat.getIntegerInstance().format(value);
                setHorizontalAlignment(SwingConstants.RIGHT);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }

            if (!text.isEmpty()) {
                if ("Status".equals(columnName) || "Role".equals(columnName)) {
                    badgeText = text;
                    badge = Theme.badge(text);
                } else if ("Active".equals(columnName)) {
                    badgeText = text.equalsIgnoreCase("Yes") ? "Active" : "Closed";
                    badge = Theme.badge(badgeText);
                }
            }
            setText(badge != null ? "" : text);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (badge == null) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(Theme.F_BADGE);
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(badgeText);
            int pillW = 10 + 7 + 6 + textW + 10, pillH = 22;
            int x = 16, y = (getHeight() - pillH) / 2;
            g2.setColor(badge.fill);
            g2.fillRoundRect(x, y, pillW, pillH, pillH, pillH);
            g2.setColor(badge.dot);
            g2.fillOval(x + 10, y + (pillH - 7) / 2, 7, 7);
            g2.setColor(badge.text);
            g2.drawString(badgeText, x + 10 + 7 + 6, y + (pillH + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------ header
    private static class HeaderRenderer extends JLabel implements TableCellRenderer, UIResource {

        HeaderRenderer() {
            setOpaque(true);
            setBackground(Theme.BG_SUBTLE);
            setForeground(Theme.TEXT_SECONDARY);
            setFont(Theme.F_LABEL);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_CARD),
                    BorderFactory.createEmptyBorder(0, 16, 0, 16)));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean focus, int row, int column) {
            setText(value == null ? "" : value.toString());
            boolean numeric = Number.class.isAssignableFrom(table.getColumnClass(column));
            setHorizontalAlignment(numeric ? SwingConstants.RIGHT : SwingConstants.LEFT);
            return this;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.height = Theme.HEADER_ROW_H;
            return d;
        }
    }
}
