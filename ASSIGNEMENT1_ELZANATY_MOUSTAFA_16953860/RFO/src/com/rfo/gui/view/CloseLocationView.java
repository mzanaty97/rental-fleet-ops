package com.rfo.gui.view;

import com.rfo.model.Location;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * The Close Location screen: the locations table plus a bar to pick where the
 * vehicles at the selected location should go, and a Close button.
 */
public class CloseLocationView extends LocationsView {

    public static final String CLOSE_COMMAND = "close";

    private final JComboBox<Location> destinationBox = new JComboBox<>();
    private final JButton closeButton = new JButton("Close location");

    public CloseLocationView() {
        destinationBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Location) {
                    Location l = (Location) value;
                    setText(l.getLocationID() + " - " + l.getName());
                }
                return this;
            }
        });
        closeButton.setActionCommand(CLOSE_COMMAND);

        getBottomBar().add(new JLabel("Move its vehicles to:"));
        getBottomBar().add(destinationBox);
        getBottomBar().add(closeButton);
    }

    /**
     * The active locations a closing location's vehicles can be moved to.
     */
    public void setDestinations(List<Location> locations) {
        Object selected = destinationBox.getSelectedItem();
        destinationBox.removeAllItems();
        for (Location l : locations) {
            destinationBox.addItem(l);
        }
        if (selected != null) {
            destinationBox.setSelectedItem(selected);
        }
    }

    /**
     * ID of the chosen destination, or an empty string when none is chosen.
     */
    public String getSelectedDestinationID() {
        Location l = (Location) destinationBox.getSelectedItem();
        return l == null ? "" : l.getLocationID();
    }

    public void addController(ActionListener controller) {
        closeButton.addActionListener(controller);
    }
}
