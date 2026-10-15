package com.rfo.gui.view;

import com.rfo.gui.theme.Theme;
import com.rfo.model.Location;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * The Amend Rental screen: type a movement ID or plate, press Find rental, and
 * a card shows the open rental's current return and estimate. Under it the user
 * types a new return date (and time) and picks a return location, then presses
 * Amend rental. The view only shows what the controller gives it.
 */
public class AmendRentalView extends JPanel implements PlainScreen {

    public static final String FIND = "findRental";
    public static final String AMEND = "amendRental";
    public static final String CONFIRM = "confirmAmend";
    public static final String CANCEL = "cancelAmend";

    private final LookupBar lookup = new LookupBar("Movement ID or plate number", 290, "Find rental", FIND,
            "Open rentals only.");
    private final FormCard card = new FormCard(640, 0);
    private final JLabel titleLabel = new JLabel();
    private final JLabel subLabel = new JLabel();
    private final JPanel rows = new JPanel();

    private final JTextField dateBox = new JTextField();
    private final FormField dateField = new FormField("New return date and time", true, dateBox,
            "e.g. 18102026 1000 (time is optional)");
    private final JComboBox<Location> locationBox = new JComboBox<>();
    private final JPanel confirmCard = new JPanel();
    private final JPanel confirmGrid = new JPanel();
    private final JLabel confirmNewTotal = new JLabel("-");
    private final JLabel confirmWas = new JLabel(" ");
    private final JButton confirmButton = new JButton("Confirm amendment");
    private final JButton cancelButton = new JButton("Cancel");
    private final JButton editButton = new JButton("Edit details");
    private final JButton amendButton = new JButton("Review amendment");

    public AmendRentalView() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        FormCard form = new FormCard(640, 24);
        lookup.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lookup);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(form);
        add(Box.createVerticalStrut(16));

        // header of the rental card
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_CARD),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));
        titleLabel.setFont(Theme.F_HEADING);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        subLabel.setFont(Theme.F_SMALL);
        subLabel.setForeground(Theme.TEXT_SECONDARY);
        names.add(titleLabel);
        names.add(subLabel);
        head.add(names, BorderLayout.CENTER);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(head);

        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBorder(BorderFactory.createEmptyBorder(4, 24, 4, 24));
        rows.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(rows);

        // the change form
        dateBox.setFont(Theme.F_MONO);
        dateField.setFixedWidth(300);
        locationBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean selected, boolean focus) {
                Component c = super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Location) {
                    Location l = (Location) value;
                    setText(l.getName() + " (" + l.getLocationID() + ")");
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
        FormField locationField = new FormField("Return location", true, locationBox, "Active locations only");
        locationField.setFixedWidth(300);
        amendButton.putClientProperty("variant", "primary");
        amendButton.setActionCommand(AMEND);

        JPanel change = new JPanel();
        change.setOpaque(false);
        change.setLayout(new BoxLayout(change, BoxLayout.Y_AXIS));
        change.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_DIVIDER),
                BorderFactory.createEmptyBorder(16, 24, 20, 24)));
        dateField.setAlignmentX(Component.LEFT_ALIGNMENT);
        locationField.setAlignmentX(Component.LEFT_ALIGNMENT);
        amendButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        change.add(dateField);
        change.add(Box.createVerticalStrut(8));
        change.add(locationField);
        change.add(Box.createVerticalStrut(14));
        change.add(amendButton);
        change.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(change);

        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setVisible(false);
        add(card);

        buildConfirmCard();
        add(confirmCard);

        dateBox.addActionListener(e -> amendButton.doClick());
        dateBox.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                dateField.clearError();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                dateField.clearError();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                dateField.clearError();
            }
        });

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(lookup::focusBox);
            }
        });
    }

    // the check-with-the-customer card, in the same style as the Checkout confirm card
    private void buildConfirmCard() {
        confirmCard.setLayout(new BoxLayout(confirmCard, BoxLayout.Y_AXIS));
        confirmCard.setBackground(Theme.BG_SURFACE);
        confirmCard.setBorder(new com.rfo.gui.theme.RoundedBorder(Theme.R_CARD, Theme.BORDER_CARD,
                new java.awt.Insets(0, 0, 0, 0), false));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_DIVIDER),
                BorderFactory.createEmptyBorder(18, 24, 18, 24)));
        JPanel headText = new JPanel();
        headText.setOpaque(false);
        headText.setLayout(new BoxLayout(headText, BoxLayout.Y_AXIS));
        JLabel please = new JLabel("Please check with the customer");
        please.setFont(Theme.F_SMALL);
        please.setForeground(Theme.TEXT_SECONDARY);
        JLabel heading = new JLabel("Confirm amendment");
        heading.setFont(Theme.F_HEADING);
        heading.setForeground(Theme.TEXT_PRIMARY);
        headText.add(please);
        headText.add(heading);
        head.add(headText, BorderLayout.CENTER);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmCard.add(head);

        confirmGrid.setOpaque(false);
        confirmGrid.setLayout(new BoxLayout(confirmGrid, BoxLayout.Y_AXIS));
        confirmGrid.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        confirmGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmCard.add(confirmGrid);

        JPanel totalRow = new JPanel(new BorderLayout(12, 0));
        totalRow.setBackground(Theme.PRIMARY_TINT);
        totalRow.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        JPanel totalText = new JPanel();
        totalText.setOpaque(false);
        totalText.setLayout(new BoxLayout(totalText, BoxLayout.Y_AXIS));
        JLabel totalKey = new JLabel("New estimated total cost");
        totalKey.setFont(Theme.F_SMALL);
        totalKey.setForeground(Theme.PRIMARY_TINT_TEXT);
        confirmWas.setFont(Theme.F_SMALL);
        confirmWas.setForeground(Theme.PRIMARY_TINT_TEXT);
        totalText.add(totalKey);
        totalText.add(confirmWas);
        confirmNewTotal.setFont(Theme.F_DISPLAY.deriveFont(28f));
        confirmNewTotal.setForeground(Theme.PRIMARY_TINT_TEXT);
        totalRow.add(totalText, BorderLayout.WEST);
        totalRow.add(confirmNewTotal, BorderLayout.EAST);
        totalRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmCard.add(totalRow);

        JPanel foot = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        foot.setBackground(Theme.BG_ZEBRA);
        foot.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_DIVIDER),
                BorderFactory.createEmptyBorder(16, 14, 16, 14)));
        editButton.putClientProperty("variant", "link");
        editButton.addActionListener(e -> showForm());
        cancelButton.setActionCommand(CANCEL);
        cancelButton.setPreferredSize(new Dimension(120, 44));
        confirmButton.putClientProperty("variant", "primary");
        confirmButton.setActionCommand(CONFIRM);
        confirmButton.setPreferredSize(new Dimension(200, 44));
        foot.add(editButton);
        foot.add(cancelButton);
        foot.add(confirmButton);
        foot.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmCard.add(foot);

        confirmCard.setPreferredSize(null);
        confirmCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmCard.setVisible(false);
    }

    /**
     * Shows the confirm card. changes are label/from/to triples (all text); the
     * totals are the new and the old estimate. The rental is only changed when
     * the user presses Confirm amendment.
     */
    public void showConfirm(String[][] changes, String newTotal, String oldTotal) {
        confirmGrid.removeAll();
        for (String[] c : changes) {
            JPanel kv = new JPanel() {
                @Override
                public Dimension getMaximumSize() {
                    return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
                }
            };
            kv.setOpaque(false);
            kv.setLayout(new BoxLayout(kv, BoxLayout.Y_AXIS));
            kv.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
            JLabel k = new JLabel(c[0]);
            k.setFont(Theme.F_SMALL);
            k.setForeground(Theme.TEXT_SECONDARY);
            JLabel was = new JLabel("Now: " + c[1]);
            was.setFont(Theme.F_BODY);
            was.setForeground(Theme.TEXT_SECONDARY);
            JLabel will = new JLabel("Change to: " + c[2]);
            will.setFont(Theme.F_BODY_B);
            will.setForeground(Theme.TEXT_PRIMARY);
            kv.add(k);
            kv.add(was);
            kv.add(will);
            kv.setAlignmentX(Component.LEFT_ALIGNMENT);
            confirmGrid.add(kv);
        }
        confirmNewTotal.setText(newTotal);
        confirmWas.setText("Was " + oldTotal);
        card.setVisible(false);
        confirmCard.setVisible(true);
        revalidate();
        repaint();
        SwingUtilities.invokeLater(confirmButton::requestFocusInWindow);
    }

    /**
     * Back from the confirm card to the rental card, keeping what was typed.
     */
    public void showForm() {
        confirmCard.setVisible(false);
        card.setVisible(true);
        revalidate();
        repaint();
        SwingUtilities.invokeLater(dateBox::requestFocusInWindow);
    }

    public String getLookupText() {
        return lookup.getText();
    }

    public String getDateText() {
        return dateBox.getText().trim();
    }

    public Location getChosenLocation() {
        return (Location) locationBox.getSelectedItem();
    }

    public void showLookupError(String message) {
        card.setVisible(false);
        confirmCard.setVisible(false);
        lookup.showError(message);
    }

    public void showDateError(String message) {
        dateField.setError(message);
        dateBox.requestFocusInWindow();
    }

    /**
     * The active locations to return to. A choice the user already made is
     * kept.
     */
    public void setLocations(List<Location> locations) {
        Location keep = (Location) locationBox.getSelectedItem();
        DefaultComboBoxModel<Location> model = new DefaultComboBoxModel<>();
        for (Location l : locations) {
            model.addElement(l);
        }
        locationBox.setModel(model);
        if (keep != null && locations.contains(keep)) {
            locationBox.setSelectedItem(keep);
        }
    }

    /**
     * Shows the open rental. details are label/value pairs, all already text.
     * The new date box is cleared and the location list is set to the rental's
     * current return location (when it is still active).
     */
    public void showRental(String title, String subtitle, String[][] details, Location currentReturn) {
        lookup.clearError();
        dateField.clearError();
        titleLabel.setText(title);
        subLabel.setText(subtitle);
        rows.removeAll();
        for (String[] d : details) {
            rows.add(row(d[0], d[1]));
        }
        dateBox.setText("");
        if (currentReturn != null) {
            for (int i = 0; i < locationBox.getItemCount(); i++) {
                if (locationBox.getItemAt(i).getLocationID().equals(currentReturn.getLocationID())) {
                    locationBox.setSelectedIndex(i);
                }
            }
        }
        confirmCard.setVisible(false);
        card.setVisible(true);
        revalidate();
        repaint();
        SwingUtilities.invokeLater(dateBox::requestFocusInWindow);
    }

    /**
     * Back to the empty screen (for example after the rental was closed in
     * another tab).
     */
    public void reset() {
        card.setVisible(false);
        confirmCard.setVisible(false);
        dateBox.setText("");
        dateField.clearError();
        revalidate();
        repaint();
    }

    // a label on the left, its value on the right, with a divider line
    private JPanel row(String label, String value) {
        JPanel row = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        row.setOpaque(false);
        row.setLayout(new BorderLayout());
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_DIVIDER),
                BorderFactory.createEmptyBorder(9, 0, 9, 0)));
        JLabel k = new JLabel(label);
        k.setFont(Theme.F_SMALL);
        k.setForeground(Theme.TEXT_SECONDARY);
        JLabel v = new JLabel(value);
        v.setFont(Theme.F_BODY_B);
        v.setForeground(Theme.TEXT_PRIMARY);
        row.add(k, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    public void addController(ActionListener controller) {
        lookup.addController(controller);
        amendButton.addActionListener(controller);
        confirmButton.addActionListener(controller);
        cancelButton.addActionListener(controller);
    }
}
