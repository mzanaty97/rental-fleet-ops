package com.rfo.gui.view;

import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.RoundedBorder;
import com.rfo.gui.theme.Theme;
import com.rfo.model.Location;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * The Change Location screen: the locations the staff member may use, one row
 * each, with a round radio mark. The current location is greyed and cannot be
 * picked. Click a row (or use the up and down arrow keys) to choose another
 * one, then press Confirm location. It only displays; the controller does the
 * switch.
 */
public class ChangeLocationView extends JPanel implements PlainScreen {

    public static final String CONFIRM = "confirm";

    private final JLabel workingAt = new JLabel();
    private final JLabel countLabel = new JLabel();
    private final JPanel rows = new JPanel();
    private final JLabel switchesTo = new JLabel(" ");
    private final JButton confirmButton = new JButton("Confirm location");
    private final List<LocationRow> rowViews = new ArrayList<>();
    private Location selected;

    public ChangeLocationView() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);
        top.add(new JLabel(Icons.icon(Icons.Type.PIN, 16, Theme.PRIMARY)), BorderLayout.WEST);
        workingAt.setFont(Theme.F_BODY);
        workingAt.setForeground(Theme.TEXT_SECONDARY);
        top.add(workingAt, BorderLayout.CENTER);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.setMaximumSize(new Dimension(640, 24));

        // the card: heading line, then one row per location
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.BG_SURFACE);
        card.setBorder(new RoundedBorder(Theme.R_CARD, Theme.BORDER_CARD, new Insets(0, 0, 0, 0), false));
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_CARD),
                javax.swing.BorderFactory.createEmptyBorder(14, 20, 14, 20)));
        JLabel title = new JLabel("Locations you can use");
        title.setFont(Theme.F_SECTION);
        title.setForeground(Theme.TEXT_PRIMARY);
        countLabel.setFont(Theme.F_SMALL);
        countLabel.setForeground(Theme.TEXT_SECONDARY);
        head.add(title, BorderLayout.WEST);
        head.add(countLabel, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        card.add(rows, BorderLayout.CENTER);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setPreferredSize(null);

        JPanel bottom = new JPanel(new BorderLayout(12, 0));
        bottom.setOpaque(false);
        switchesTo.setFont(Theme.F_SMALL);
        switchesTo.setForeground(Theme.TEXT_SECONDARY);
        confirmButton.putClientProperty("variant", "primary");
        confirmButton.setActionCommand(CONFIRM);
        confirmButton.setEnabled(false);
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.add(switchesTo);
        right.add(Box.createHorizontalStrut(12));
        right.add(confirmButton);
        bottom.add(right, BorderLayout.EAST);
        bottom.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(top);
        add(Box.createVerticalStrut(16));
        add(card);
        add(Box.createVerticalStrut(16));
        add(bottom);

        installKeys();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(640, super.getPreferredSize().height);
    }

    private void installKeys() {
        bind(KeyEvent.VK_DOWN, () -> move(1));
        bind(KeyEvent.VK_UP, () -> move(-1));
        bind(KeyEvent.VK_ENTER, () -> {
            if (confirmButton.isEnabled()) {
                confirmButton.doClick();
            }
        });
    }

    private void bind(int key, Runnable action) {
        String name = "key" + key;
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key, 0), name);
        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    // arrow keys step through the rows that can be chosen
    private void move(int step) {
        List<LocationRow> choosable = new ArrayList<>();
        for (LocationRow r : rowViews) {
            if (!r.current) {
                choosable.add(r);
            }
        }
        if (choosable.isEmpty()) {
            return;
        }
        int at = -1;
        for (int i = 0; i < choosable.size(); i++) {
            if (choosable.get(i).location == selected) {
                at = i;
            }
        }
        int next = at == -1 ? (step > 0 ? 0 : choosable.size() - 1) : Math.floorMod(at + step, choosable.size());
        select(choosable.get(next).location);
    }

    /**
     * Fills the list. {@code current} may be null when the staff member has no
     * working location.
     */
    public void setLocations(Location current, List<Location> accessible) {
        selected = null;
        rows.removeAll();
        rowViews.clear();
        if (current != null) {
            workingAt.setText("<html>You are working at <b style='color:#1F2A44'>" + escape(current.getName())
                    + " (" + escape(current.getLocationID()) + ")</b></html>");
        } else {
            workingAt.setText("You have no working location yet. Choose one below.");
        }
        countLabel.setText(accessible.size() + (accessible.size() == 1 ? " location" : " locations"));

        for (int i = 0; i < accessible.size(); i++) {
            Location loc = accessible.get(i);
            LocationRow row = new LocationRow(loc, current != null && loc.getLocationID().equals(current.getLocationID()),
                    i == accessible.size() - 1);
            rowViews.add(row);
            rows.add(row);
        }
        if (accessible.isEmpty()) {
            JLabel none = new JLabel("You have no accessible locations.");
            none.setFont(Theme.F_BODY);
            none.setForeground(Theme.TEXT_SECONDARY);
            none.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
            rows.add(none);
        }
        updateFooter();
        revalidate();
        repaint();
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;");
    }

    private void select(Location location) {
        selected = location;
        updateFooter();
        for (LocationRow r : rowViews) {
            r.repaint();
        }
    }

    private void updateFooter() {
        confirmButton.setEnabled(selected != null);
        switchesTo.setText(selected != null ? "Switches to " + selected.getName() : " ");
    }

    /**
     * The location the user picked, or null when nothing is picked.
     */
    public Location getSelectedLocation() {
        return selected;
    }

    public void addController(ActionListener controller) {
        confirmButton.addActionListener(controller);
    }

    /**
     * One row: radio mark, ID chip, name and, for the current location, a
     * CURRENT pill.
     */
    private class LocationRow extends JComponent {

        private final Location location;
        private final boolean current;
        private final boolean last;
        private boolean hover;

        LocationRow(Location location, boolean current, boolean last) {
            this.location = location;
            this.current = current;
            this.last = last;
            setPreferredSize(new Dimension(640, 60));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            if (!current) {
                setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            }
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!LocationRow.this.current) {
                        select(LocationRow.this.location);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            boolean isSelected = selected == location;

            if (current) {
                g2.setColor(Theme.BG_ZEBRA);
                g2.fillRect(1, 0, w - 2, h);
            } else if (isSelected) {
                g2.setColor(Theme.PRIMARY_TINT);
                g2.fillRect(1, 0, w - 2, h);
            } else if (hover) {
                g2.setColor(Theme.BG_HOVER);
                g2.fillRect(1, 0, w - 2, h);
            }
            if (!last && !isSelected) {
                g2.setColor(Theme.BORDER_DIVIDER);
                g2.fillRect(0, h - 1, w, 1);
            }
            if (isSelected) {
                g2.setColor(Theme.PRIMARY);
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.drawRect(2, 1, w - 5, h - 3);
            }

            // radio mark
            int cy = h / 2;
            if (isSelected) {
                g2.setColor(Theme.PRIMARY);
                g2.fillOval(20, cy - 9, 18, 18);
                g2.setColor(Color.WHITE);
                g2.fillOval(25, cy - 4, 8, 8);
            } else {
                g2.setColor(current ? Theme.BORDER_DASHED : Theme.BORDER_FIELD);
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.drawOval(21, cy - 8, 16, 16);
            }

            // ID chip
            g2.setFont(Theme.F_KEY);
            FontMetrics fm = g2.getFontMetrics();
            String id = location.getLocationID();
            int chipW = fm.stringWidth(id) + 16;
            g2.setColor(isSelected ? Color.WHITE : Theme.BG_SUBTLE);
            g2.fillRoundRect(52, cy - 12, chipW, 24, 8, 8);
            g2.setColor(Theme.TEXT_PRIMARY);
            g2.drawString(id, 60, cy + (fm.getAscent() - fm.getDescent()) / 2);

            // name
            g2.setFont(isSelected ? Theme.F_BODY_B : Theme.F_BODY);
            g2.setColor(current ? Theme.TEXT_SECONDARY : Theme.TEXT_PRIMARY);
            FontMetrics nm = g2.getFontMetrics();
            g2.drawString(location.getName(), 52 + chipW + 14, cy + (nm.getAscent() - nm.getDescent()) / 2);

            // CURRENT pill
            if (current) {
                g2.setFont(Theme.F_BADGE);
                FontMetrics bm = g2.getFontMetrics();
                int pw = bm.stringWidth("CURRENT") + 20;
                g2.setColor(Theme.BG_SUBTLE);
                g2.fillRoundRect(w - 20 - pw, cy - 11, pw, 22, 22, 22);
                g2.setColor(Theme.TEXT_SECONDARY);
                g2.drawString("CURRENT", w - 20 - pw + 10, cy + (bm.getAscent() - bm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }
}
