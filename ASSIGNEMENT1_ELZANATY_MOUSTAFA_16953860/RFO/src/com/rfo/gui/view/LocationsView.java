package com.rfo.gui.view;

import com.rfo.model.Location;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * A table of locations. It serves two menu options: View Locations (every
 * location) and, with a vehicle count column, the table used on the Close
 * Location screen. The controller hands it the rows to show.
 */
public class LocationsView extends JPanel {

    private static final String[] COLUMNS = {"Location ID", "Name", "Active", "Address", "Vehicles"};
    private static final int ID_COLUMN = 0;

    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 4 ? Integer.class : String.class;
        }
    };
    protected final JTable table = new JTable(tableModel);
    private final JLabel countLabel = new JLabel(" ");
    private final JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
    private final JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));

    public LocationsView() {
        super(new BorderLayout());
        setBackground(Color.WHITE);

        topBar.setBackground(Color.WHITE);
        topBar.add(countLabel);
        bottomBar.setBackground(Color.WHITE);

        table.setRowHeight(26);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        scroll.getViewport().setBackground(Color.WHITE);

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    /**
     * Room under the table for a subclass's own controls.
     */
    protected JPanel getBottomBar() {
        return bottomBar;
    }

    /**
     * Replaces the table rows, keeping the selected location selected if it is
     * still listed.
     *
     * @param vehicleCounts vehicles at each location ID
     */
    public void setLocations(List<Location> locations, java.util.Map<String, Integer> vehicleCounts) {
        String selectedID = getSelectedLocationID();

        tableModel.setRowCount(0);
        for (Location l : locations) {
            tableModel.addRow(new Object[]{l.getLocationID(), l.getName(), l.isActive() ? "Yes" : "No",
                l.getAddress().getFullAddress(), vehicleCounts.getOrDefault(l.getLocationID(), 0)});
        }
        countLabel.setText(locations.size() + (locations.size() == 1 ? " location" : " locations"));

        if (selectedID != null) {
            for (int row = 0; row < table.getRowCount(); row++) {
                if (selectedID.equals(table.getValueAt(row, ID_COLUMN))) {
                    table.setRowSelectionInterval(row, row);
                    break;
                }
            }
        }
    }

    /**
     * ID of the selected row, or null when nothing is selected.
     */
    public String getSelectedLocationID() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (String) table.getValueAt(row, ID_COLUMN);
    }
}
