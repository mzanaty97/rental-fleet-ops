package com.rfo.gui.view;

import com.rfo.gui.theme.BadgeLabel;
import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.Theme;
import com.rfo.model.Vehicle;
import com.rfo.model.VehicleCategory;
import com.rfo.model.VehicleStatus;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
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
 * top, and can let the user click a vehicle's status to change it. The view
 * only displays what the controller gives it; it never reads the model itself.
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
    private final List<ActionListener> listeners = new ArrayList<>();
    private final boolean statusEditable;
    private List<VehicleStatus> statusOptions = new ArrayList<>();
    private VehicleStatus chosenStatus;

    /**
     * @param mode which vehicles this screen shows
     * @param showCategoryFilter offer the category drop down
     * @param statusEditable let the user click a status in the table to change
     * it
     */
    public FleetView(Mode mode, boolean showCategoryFilter, boolean statusEditable) {
        super(new BorderLayout());
        setBackground(Color.WHITE);
        this.mode = mode;
        this.statusEditable = statusEditable;

        categoryBox.addItem(ALL_CATEGORIES);
        for (VehicleCategory category : VehicleCategory.values()) {
            categoryBox.addItem(category);
        }

        add(buildFilterBar(showCategoryFilter), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
    }

    private JPanel buildFilterBar(boolean showCategoryFilter) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(Color.WHITE);
        if (showCategoryFilter) {
            bar.add(new JLabel("Category:"));
            bar.add(categoryBox);
        }
        bar.add(countLabel);
        if (statusEditable) {
            JLabel hint = new JLabel("Click a status to change it.");
            hint.setFont(Theme.F_SMALL);
            hint.setForeground(Theme.TEXT_SECONDARY);
            bar.add(hint);
        }
        return bar;
    }

    private JScrollPane buildTable() {
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        if (statusEditable) {
            installStatusClicks();
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    // clicking a status cell opens a small menu of the statuses the user may choose
    private void installStatusClicks() {
        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                table.setCursor(isStatusCell(e.getPoint()) ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && isStatusCell(e.getPoint())) {
                    int row = table.rowAtPoint(e.getPoint());
                    table.setRowSelectionInterval(row, row);
                    showStatusMenu(row);
                }
            }
        });
    }

    private boolean isStatusCell(Point p) {
        int row = table.rowAtPoint(p);
        int col = table.columnAtPoint(p);
        return row >= 0 && col >= 0 && table.convertColumnIndexToModel(col) == STATUS_COLUMN;
    }

    private void showStatusMenu(int row) {
        VehicleStatus current = VehicleStatus.valueOf((String) table.getValueAt(row, STATUS_COLUMN));
        int viewCol = table.convertColumnIndexToView(STATUS_COLUMN);
        Rectangle cell = table.getCellRect(row, viewCol, false);

        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_CARD, 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        menu.setBackground(Color.WHITE);
        JLabel title = new JLabel("Change status to");
        title.setFont(Theme.F_SMALL);
        title.setForeground(Theme.TEXT_SECONDARY);
        title.setBorder(BorderFactory.createEmptyBorder(2, 8, 6, 8));
        title.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        menu.add(title);
        for (VehicleStatus status : statusOptions) {
            StatusItem item = new StatusItem(status, status == current, menu);
            item.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
            menu.add(item);
        }
        menu.show(table, cell.x, cell.y + cell.height);
    }

    /**
     * One row of the status menu: the status as a coloured badge, with a tick
     * on the current one.
     */
    private class StatusItem extends JPanel {

        private final VehicleStatus status;

        StatusItem(VehicleStatus status, boolean current, JPopupMenu menu) {
            super(new BorderLayout(12, 0));
            this.status = status;
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            badge.setOpaque(false);
            badge.add(new BadgeLabel(status.toString(), true));
            add(badge, BorderLayout.CENTER);
            JLabel tick = new JLabel(current ? Icons.icon(Icons.Type.CHECK_CIRCLE, 16, Theme.PRIMARY) : null);
            tick.setPreferredSize(new java.awt.Dimension(16, 16));
            add(tick, BorderLayout.EAST);
            setToolTipText(current ? "Current status" : null);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(Theme.BG_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(Color.WHITE);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    menu.setVisible(false);
                    choose(StatusItem.this.status);
                }
            });
        }

        @Override
        public java.awt.Dimension getPreferredSize() {
            java.awt.Dimension d = super.getPreferredSize();
            return new java.awt.Dimension(Math.max(d.width, 200), 36);
        }
    }

    private void choose(VehicleStatus status) {
        chosenStatus = status;
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, APPLY_COMMAND);
        for (ActionListener l : new ArrayList<>(listeners)) {
            l.actionPerformed(event);
        }
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
     * The statuses the user may choose from in the status menu.
     */
    public void setStatusOptions(List<VehicleStatus> statuses) {
        statusOptions = new ArrayList<>(statuses);
    }

    /**
     * The status the user just picked from the menu.
     */
    public VehicleStatus getSelectedStatus() {
        return chosenStatus;
    }

    public void addController(ActionListener controller) {
        categoryBox.addActionListener(controller);
        listeners.add(controller);
    }
}
