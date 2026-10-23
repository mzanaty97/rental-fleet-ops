package com.rfo.gui.view;

import com.rfo.gui.theme.Avatar;
import com.rfo.gui.theme.Theme;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * The Query Drivers screen: type a driver ID, press Find driver, and a card
 * shows the driver's name, licence number, email, phone and address. The view
 * only shows what the controller gives it.
 */
public class DriverView extends JPanel implements PlainScreen {

    public static final String FIND = "findDriver";

    private final LookupBar lookup = new LookupBar("Driver ID", 180, "Find driver", FIND, "Type the driver's ID.");
    private final FormCard card = new FormCard(560, 0);
    private final JPanel avatarBox = new JPanel();
    private final JLabel nameLabel = new JLabel();
    private final JLabel idLabel = new JLabel();
    private final JLabel[] values = {new JLabel(), new JLabel(), new JLabel(), new JLabel()};

    public DriverView() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        FormCard form = new FormCard(560, 24);
        lookup.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lookup);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(form);
        add(Box.createVerticalStrut(16));

        // header of the result card: avatar with initials, name, driver ID
        JPanel head = new JPanel(new java.awt.BorderLayout(16, 0));
        head.setOpaque(false);
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_CARD),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));
        // a fixed square for the round avatar so it is not squashed
        avatarBox.setOpaque(false);
        avatarBox.setLayout(new java.awt.GridBagLayout());
        avatarBox.setPreferredSize(new Dimension(52, 52));
        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        nameLabel.setFont(Theme.F_HEADING);
        nameLabel.setForeground(Theme.TEXT_PRIMARY);
        idLabel.setFont(Theme.F_MONO);
        idLabel.setForeground(Theme.TEXT_SECONDARY);
        names.add(nameLabel);
        names.add(idLabel);
        JPanel namesBox = new JPanel(new java.awt.GridBagLayout());
        namesBox.setOpaque(false);
        java.awt.GridBagConstraints left = new java.awt.GridBagConstraints();
        left.anchor = java.awt.GridBagConstraints.WEST;
        left.weightx = 1;
        namesBox.add(names, left);
        head.add(avatarBox, java.awt.BorderLayout.WEST);
        head.add(namesBox, java.awt.BorderLayout.CENTER);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(head);

        String[] keys = {"Licence number", "Email", "Phone", "Address"};
        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBorder(BorderFactory.createEmptyBorder(4, 24, 8, 24));
        for (int i = 0; i < keys.length; i++) {
            // the row takes the card's full width and only as much height as it needs
            JPanel kv = new JPanel() {
                @Override
                public Dimension getMaximumSize() {
                    return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
                }
            };
            kv.setOpaque(false);
            kv.setLayout(new BoxLayout(kv, BoxLayout.Y_AXIS));
            kv.setBorder(i < keys.length - 1
                    ? BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_DIVIDER),
                            BorderFactory.createEmptyBorder(12, 0, 12, 0))
                    : BorderFactory.createEmptyBorder(12, 0, 12, 0));
            JLabel k = new JLabel(keys[i]);
            k.setFont(Theme.F_SMALL);
            k.setForeground(Theme.TEXT_SECONDARY);
            values[i].setFont(i == 0 ? Theme.F_KEY : Theme.F_BODY_B);
            values[i].setForeground(Theme.TEXT_PRIMARY);
            kv.add(k);
            kv.add(values[i]);
            kv.setAlignmentX(Component.LEFT_ALIGNMENT);
            rows.add(kv);
        }
        rows.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(rows);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setVisible(false);
        add(card);

        // the ID box is ready for typing as soon as the screen opens
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(lookup::focusBox);
            }
        });
    }

    public String getDriverText() {
        return lookup.getText();
    }

    public void showError(String message) {
        card.setVisible(false);
        lookup.showError(message);
    }

    /**
     * Shows the driver card. Every value is already text.
     */
    public void showDriver(String initials, String name, String driverId, String licence, String email,
            String phone, String address) {
        lookup.clearError();
        avatarBox.removeAll();
        avatarBox.add(new Avatar(initials, Theme.NAVY_900, 52, 18f));
        nameLabel.setText(name);
        idLabel.setText("Driver " + driverId);
        values[0].setText(licence);
        values[1].setText(email);
        values[2].setText(phone);
        values[3].setText(address);
        card.setVisible(true);
        revalidate();
        repaint();
    }

    public void addController(ActionListener controller) {
        lookup.addController(controller);
    }
}
