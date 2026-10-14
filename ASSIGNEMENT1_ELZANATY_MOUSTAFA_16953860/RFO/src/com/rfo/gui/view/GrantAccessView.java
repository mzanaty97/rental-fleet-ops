package com.rfo.gui.view;

import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.Theme;
import com.rfo.model.Location;
import com.rfo.model.Staff;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

/**
 * Grant Location Access: pick a staff member, see the locations they can
 * already use, then pick one more from the active locations they cannot use
 * yet. The view only shows lists it is given.
 */
public class GrantAccessView extends JPanel implements PlainScreen {

    public static final String GRANT = "grant";
    public static final String STAFF_CHANGED = "staff";

    private final StaffPicker staffBox = new StaffPicker();
    private final ChipPanel alreadyChips = new ChipPanel(592, false);
    private final JComboBox<Location> locationBox = new JComboBox<>();
    private final JLabel locationHelp = new JLabel();
    private final JButton grantButton = new JButton("Grant access");

    public GrantAccessView() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        locationBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean selected, boolean focus) {
                Component c = super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Location) {
                    Location l = (Location) value;
                    setText(l.getLocationID() + "  ·  " + l.getName());
                }
                setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        FormCard card = new FormCard(640, 24);
        card.add(left(new FormField("Staff member", false, staffBox, "Use the arrow keys or type the first letter to change person")));
        card.add(Box.createVerticalStrut(18));
        JLabel already = new JLabel("Can already use");
        already.setFont(Theme.F_LABEL);
        already.setForeground(Theme.TEXT_PRIMARY);
        card.add(left(already));
        card.add(Box.createVerticalStrut(6));
        card.add(left(alreadyChips));
        card.add(Box.createVerticalStrut(18));
        card.add(left(divider()));
        card.add(Box.createVerticalStrut(18));
        card.add(left(new FormField("Add access to", false, locationBox, null)));
        locationHelp.setFont(Theme.F_SMALL);
        locationHelp.setForeground(Theme.TEXT_SECONDARY);
        card.add(left(locationHelp));
        card.add(Box.createVerticalStrut(18));

        grantButton.putClientProperty("variant", "primary");
        grantButton.setIcon(Icons.icon(Icons.Type.PLUS, 16, Theme.TEXT_ON_PRIMARY));
        grantButton.setActionCommand(GRANT);
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(grantButton);
        card.add(left(buttons));
        add(left(card));
    }

    static JPanel divider() {
        JPanel d = new JPanel();
        d.setBackground(Theme.BORDER_DIVIDER);
        d.setPreferredSize(new Dimension(10, 1));
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return d;
    }

    private static <T extends javax.swing.JComponent> T left(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    public void setStaff(List<Staff> staff) {
        staffBox.setStaff(staff);
    }

    public Staff getSelectedStaff() {
        return staffBox.getSelectedStaff();
    }

    public boolean isUpdating() {
        return staffBox.isUpdating();
    }

    public void setCurrentAccess(List<Location> locations) {
        List<String[]> chips = new ArrayList<>();
        for (Location l : locations) {
            chips.add(new String[]{l.getLocationID(), l.getName()});
        }
        alreadyChips.setChips(chips);
    }

    public void setGrantable(List<Location> locations) {
        DefaultComboBoxModel<Location> model = new DefaultComboBoxModel<>();
        for (Location l : locations) {
            model.addElement(l);
        }
        locationBox.setModel(model);
        boolean any = !locations.isEmpty();
        locationBox.setEnabled(any);
        grantButton.setEnabled(any);
        locationHelp.setText(any ? "Only active locations they cannot use yet are listed"
                : "This person can already use every active location");
    }

    public Location getSelectedLocation() {
        return (Location) locationBox.getSelectedItem();
    }

    public void addController(ActionListener controller) {
        grantButton.addActionListener(controller);
        staffBox.addActionListener(e -> {
            if (!staffBox.isUpdating()) {
                controller.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, STAFF_CHANGED));
            }
        });
    }
}
