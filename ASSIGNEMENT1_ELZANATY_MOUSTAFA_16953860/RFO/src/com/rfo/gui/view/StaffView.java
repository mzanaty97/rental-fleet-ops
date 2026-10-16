package com.rfo.gui.view;

import com.rfo.gui.theme.Theme;
import com.rfo.model.Staff;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * The View Staff table: ID, name, role (as a coloured badge) and current
 * location. Click a header to sort.
 */
public class StaffView extends JPanel {

    private static final String[] COLUMNS = {"Staff ID", "Name", "Role", "Current Location"};

    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel countLabel = new JLabel(" ");

    public StaffView() {
        super(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(Color.WHITE);
        countLabel.setFont(Theme.F_BODY_B);
        JLabel hint = new JLabel("Click a column header to sort.");
        hint.setFont(Theme.F_SMALL);
        hint.setForeground(Theme.TEXT_SECONDARY);
        bar.add(countLabel);
        bar.add(hint);

        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        scroll.getViewport().setBackground(Color.WHITE);
        add(bar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void setStaff(List<Staff> staff) {
        tableModel.setRowCount(0);
        for (Staff s : staff) {
            String where = s.getCurrentLocation() == null ? "-" : s.getCurrentLocation().getName();
            tableModel.addRow(new Object[]{s.getStaffID(), s.getFName() + " " + s.getLName(),
                s.getRole().toString(), where});
        }
        countLabel.setText(staff.size() + (staff.size() == 1 ? " staff member" : " staff"));
    }
}
