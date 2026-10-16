package com.rfo.gui.view;

import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.RoundedBorder;
import com.rfo.gui.theme.Theme;
import com.rfo.model.Location;
import com.rfo.model.Staff;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * Revoke Location Access: pick a staff member, tick the locations to remove (or
 * Select all), then confirm in the red panel underneath. Only the ticked
 * locations are removed; the rest stay.
 */
public class RevokeAccessView extends JPanel implements PlainScreen {

    public static final String REVOKE = "revoke";
    public static final String KEEP = "keep";
    public static final String STAFF_CHANGED = "staff";

    private final StaffPicker staffBox = new StaffPicker();
    private final JPanel rowsPanel = new JPanel();
    private final JLabel countLabel = new JLabel(" ");
    private final JButton selectAllButton = new JButton("Select all");
    private final JLabel question = new JLabel();
    private final JLabel detail = new JLabel();
    private final JButton keepButton = new JButton("Keep access");
    private final JButton revokeButton = new JButton("Revoke selected");
    private final List<CheckRow> rows = new ArrayList<>();
    private String staffName = "this person";

    public RevokeAccessView() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        FormCard card = new FormCard(640, 24);
        card.add(left(new FormField("Staff member", false, staffBox, null)));
        card.add(Box.createVerticalStrut(18));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("Tick the access to remove");
        title.setFont(Theme.F_LABEL);
        title.setForeground(Theme.TEXT_PRIMARY);
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        countLabel.setFont(Theme.F_SMALL);
        countLabel.setForeground(Theme.TEXT_SECONDARY);
        selectAllButton.putClientProperty("variant", "link");
        selectAllButton.putClientProperty("size", "compact");
        right.add(countLabel);
        right.add(Box.createHorizontalStrut(10));
        right.add(selectAllButton);
        head.add(title, BorderLayout.WEST);
        head.add(right, BorderLayout.EAST);
        card.add(left(head));
        card.add(Box.createVerticalStrut(8));
        rowsPanel.setOpaque(false);
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        card.add(left(rowsPanel));
        add(left(card));
        add(Box.createVerticalStrut(16));

        FormCard confirm = new FormCard(640, 20);
        confirm.setBorder(new RoundedBorder(Theme.R_CARD, Theme.ERR_LINE, new Insets(20, 24, 20, 24), false));
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        JLabel icon = new JLabel(Icons.icon(Icons.Type.WARNING, 22, Theme.ERR_FG));
        icon.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        question.setFont(Theme.F_SECTION);
        question.setForeground(Theme.TEXT_PRIMARY);
        detail.setFont(Theme.F_BODY);
        detail.setForeground(Theme.TEXT_SECONDARY);
        text.add(left(question));
        text.add(Box.createVerticalStrut(4));
        text.add(left(detail));
        top.add(icon, BorderLayout.WEST);
        top.add(text, BorderLayout.CENTER);
        confirm.add(left(top));
        confirm.add(Box.createVerticalStrut(14));

        keepButton.setActionCommand(KEEP);
        revokeButton.putClientProperty("variant", "danger");
        revokeButton.setActionCommand(REVOKE);
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(keepButton);
        buttons.add(Box.createHorizontalStrut(8));
        buttons.add(revokeButton);
        confirm.add(left(buttons));
        add(left(confirm));

        selectAllButton.addActionListener(e -> {
            boolean all = !allTicked();
            for (CheckRow r : rows) {
                r.setTicked(all);
            }
            updateSummary();
        });
        updateSummary();
    }

    private static <T extends JComponent> T left(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private boolean allTicked() {
        for (CheckRow r : rows) {
            if (!r.ticked) {
                return false;
            }
        }
        return !rows.isEmpty();
    }

    public void setStaff(List<Staff> staff) {
        staffBox.setStaff(staff);
    }

    public Staff getSelectedStaff() {
        return staffBox.getSelectedStaff();
    }

    /**
     * Lists the person's locations. Ticks that are still valid are kept, so a
     * refresh does not undo them.
     */
    public void setAccess(Staff staff, List<Location> locations) {
        List<String> keep = new ArrayList<>();
        for (Location l : getSelectedLocations()) {
            keep.add(l.getLocationID());
        }
        rows.clear();
        rowsPanel.removeAll();
        for (Location l : locations) {
            CheckRow row = new CheckRow(l);
            row.setTicked(keep.contains(l.getLocationID()));
            rows.add(row);
            rowsPanel.add(row);
        }
        if (locations.isEmpty()) {
            JLabel none = new JLabel("This person has no location access to remove.");
            none.setFont(Theme.F_BODY);
            none.setForeground(Theme.TEXT_SECONDARY);
            rowsPanel.add(none);
        }
        staffName = staff == null ? "this person" : staff.getFName() + " " + staff.getLName();
        rowsPanel.revalidate();
        rowsPanel.repaint();
        updateSummary();
    }

    public List<Location> getSelectedLocations() {
        List<Location> list = new ArrayList<>();
        for (CheckRow r : rows) {
            if (r.ticked) {
                list.add(r.location);
            }
        }
        return list;
    }

    private void updateSummary() {
        int n = getSelectedLocations().size();
        countLabel.setText(n + " of " + rows.size() + " selected");
        selectAllButton.setText(allTicked() ? "Clear all" : "Select all");
        selectAllButton.setEnabled(!rows.isEmpty());
        if (n == 0) {
            question.setText(rows.isEmpty() ? staffName + " has no location access." : "Choose the access to remove");
            detail.setText(rows.isEmpty() ? " " : "Tick one or more locations above. The others stay as they are.");
        } else {
            question.setText("Remove access to " + n + (n == 1 ? " location" : " locations") + " for " + staffName + "?");
            detail.setText(n == rows.size() ? "They will not be able to change to any location until access is granted again."
                    : "Their other locations stay as they are.");
        }
        revokeButton.setEnabled(n > 0);
        keepButton.setEnabled(n > 0);
    }

    public void clearSelection() {
        for (CheckRow r : rows) {
            r.setTicked(false);
        }
        updateSummary();
    }

    public void addController(ActionListener controller) {
        keepButton.addActionListener(controller);
        revokeButton.addActionListener(controller);
        staffBox.addActionListener(e -> {
            if (!staffBox.isUpdating()) {
                controller.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, STAFF_CHANGED));
            }
        });
    }

    /**
     * One location with a tick box. Click or press Space to tick it.
     */
    private class CheckRow extends JComponent {

        private final Location location;
        private boolean ticked;
        private boolean hover;

        CheckRow(Location location) {
            this.location = location;
            setFocusable(true);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                    toggle();
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
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        toggle();
                    }
                }
            });
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    repaint();
                }
            });
        }

        void toggle() {
            ticked = !ticked;
            repaint();
            updateSummary();
        }

        void setTicked(boolean value) {
            ticked = value;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(592, 40);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, 40);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (ticked) {
                g2.setColor(Theme.ERR_BG);
                g2.fillRoundRect(0, 2, w, h - 4, 8, 8);
            } else if (hover) {
                g2.setColor(Theme.BG_HOVER);
                g2.fillRoundRect(0, 2, w, h - 4, 8, 8);
            }
            int bx = 10, by = (h - 18) / 2;
            if (ticked) {
                g2.setColor(Theme.DANGER);
                g2.fillRoundRect(bx, by, 18, 18, 5, 5);
                g2.setColor(Theme.TEXT_ON_PRIMARY);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(bx + 4, by + 9, bx + 8, by + 13);
                g2.drawLine(bx + 8, by + 13, bx + 14, by + 5);
            } else {
                g2.setColor(Theme.BG_SURFACE);
                g2.fillRoundRect(bx, by, 18, 18, 5, 5);
                g2.setColor(Theme.BORDER_FIELD);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(bx, by, 17, 17, 5, 5);
            }
            FontMetrics idFm = g2.getFontMetrics(Theme.F_KEY);
            int base = (h + idFm.getAscent() - idFm.getDescent()) / 2;
            g2.setFont(Theme.F_KEY);
            g2.setColor(ticked ? Theme.ERR_DARK : Theme.TEXT_PRIMARY);
            g2.drawString(location.getLocationID(), 40, base);
            g2.setFont(Theme.F_BODY);
            g2.drawString(location.getName(), 40 + idFm.stringWidth(location.getLocationID()) + 12, base);
            if (isFocusOwner()) {
                g2.setColor(Theme.FOCUS);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 3, w - 3, h - 7, 8, 8);
            }
            g2.dispose();
        }
    }
}
