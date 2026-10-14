package com.rfo.gui.view;

import com.rfo.model.Vehicle;
import com.rfo.model.VehicleCategory;
import com.rfo.model.VehicleStatus;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * A vehicle table screen. The same view serves three Fleet menu options: it
 * always shows one kind of vehicles (its Mode), can offer a category filter on
 * top, and can offer a status bar at the bottom for changing the status of the
 * selected vehicle. The view only displays what the controller gives it; it
 * never reads the model itself.
 */
public class FleetView extends JPanel {

    /**
     * Which vehicles the table shows.
     */
    public enum Mode {
        AVAILABLE("Available"),
        SERVICE_DUE("Service due"),
        ALL("All vehicles");

        private final String label;

        Mode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static final String APPLY_COMMAND = "apply";

    private static final String ALL_CATEGORIES = "All categories";
    private static final String[] COLUMNS = {"Category", "Plate", "Make / Model",
        "Odometer (km)", "Location", "Status", "Overdue (km)"};
    private static final int PLATE_COLUMN = 1;
    private static final int STATUS_COLUMN = 5;

    private final Mode mode;
    private final JComboBox<Object> categoryBox = new JComboBox<>();
    private final JLabel countLabel = new JLabel(" ");
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return (column == 3 || column == 6) ? Integer.class : String.class;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JComboBox<VehicleStatus> statusBox = new JComboBox<>();
    private final JButton applyButton = new JButton("Apply status");

    /**
     * @param mode which vehicles this screen shows
     * @param showCategoryFilter offer the category drop down
     * @param showStatusBar offer the bar for changing a vehicle's status
     */
    public FleetView(Mode mode, boolean showCategoryFilter, boolean showStatusBar) {
        super(new BorderLayout());
        setBackground(Color.WHITE);
        this.mode = mode;

        categoryBox.addItem(ALL_CATEGORIES);
        for (VehicleCategory category : VehicleCategory.values()) {
            categoryBox.addItem(category);
        }

        add(buildFilterBar(showCategoryFilter), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildStatusBar(showStatusBar), BorderLayout.SOUTH);

        applyButton.setActionCommand(APPLY_COMMAND);
    }

    private JPanel buildFilterBar(boolean showCategoryFilter) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(Color.WHITE);
        if (showCategoryFilter) {
            bar.add(new JLabel("Category:"));
            bar.add(categoryBox);
        }
        bar.add(countLabel);
        return bar;
    }

    private JScrollPane buildTable() {
        table.setRowHeight(26);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        // picking a row pre-selects that vehicle's current status in the box below
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                statusBox.setSelectedItem(VehicleStatus.valueOf(
                        (String) table.getValueAt(table.getSelectedRow(), STATUS_COLUMN)));
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private JPanel buildStatusBar(boolean showStatusBar) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(Color.WHITE);

        if (showStatusBar) {
            bar.add(new JLabel("Set selected vehicle to:"));
            bar.add(statusBox);
            bar.add(applyButton);
        }
        return bar;
    }

    /**
     * Replaces the table rows, keeping the selected vehicle selected if it is
     * still listed.
     */
    public void setVehicles(List<Vehicle> vehicles) {
        String selectedPlate = getSelectedPlate();

        tableModel.setRowCount(0);
        for (Vehicle v : vehicles) {
            String location = v.getCurrentLocation() != null ? v.getCurrentLocation().getName() : "-";
            Integer overdue = v.isDueForService() ? Integer.valueOf(v.getOverdueKm()) : null;
            tableModel.addRow(new Object[]{v.getCategory().toString(), v.getPlateNumber(),
                v.getMake() + " " + v.getModel(), v.getCurrentOdometer(), location,
                v.getStatus().toString(), overdue});
        }
        countLabel.setText(vehicles.size() + (vehicles.size() == 1 ? " vehicle" : " vehicles"));

        if (selectedPlate != null) {
            for (int row = 0; row < table.getRowCount(); row++) {
                if (selectedPlate.equals(table.getValueAt(row, PLATE_COLUMN))) {
                    table.setRowSelectionInterval(row, row);
                    break;
                }
            }
        }
    }

    public Mode getSelectedMode() {
        return mode;
    }

    /**
     * The chosen category, or null for "All categories".
     */
    public VehicleCategory getSelectedCategory() {
        Object selected = categoryBox.getSelectedItem();
        return selected instanceof VehicleCategory ? (VehicleCategory) selected : null;
    }

    /**
     * Plate of the selected row, or null when nothing is selected.
     */
    public String getSelectedPlate() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (String) table.getValueAt(row, PLATE_COLUMN);
    }

    /**
     * The statuses the user may choose from in the status bar.
     */
    public void setStatusOptions(List<VehicleStatus> statuses) {
        statusBox.removeAllItems();
        for (VehicleStatus status : statuses) {
            statusBox.addItem(status);
        }
    }

    public VehicleStatus getSelectedStatus() {
        return (VehicleStatus) statusBox.getSelectedItem();
    }

    public void addController(ActionListener controller) {
        categoryBox.addActionListener(controller);
        applyButton.addActionListener(controller);
    }
}
