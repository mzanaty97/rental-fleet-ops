package com.rfo.gui.view;

import com.rfo.gui.theme.RoundedBorder;
import com.rfo.gui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * The Show Vehicle Movements screen: type a rego, press Show, and a table lists
 * every movement of that vehicle, newest first. The view only shows the rows
 * the controller gives it.
 */
public class VehicleMovementsView extends JPanel implements FullScreen {

    public static final String SHOW = "showMovements";
    public static final String OPENED = "opened";

    private static final String[] COLUMNS = {"Type", "Movement", "Check-out location", "Check-out date/time",
        "Odo out", "Check-in location", "Check-in date/time", "Odo in", "Status"};
    private static final int[] WIDTHS = {70, 115, 200, 170, 85, 200, 170, 85, 100};

    private final LookupBar lookup = new LookupBar("Rego", 200, "Show", SHOW, "e.g. ABC123");
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);
    private final JScrollPane scroll = new JScrollPane(table);
    private final JPanel card = new JPanel(new BorderLayout());
    private final JLabel title = new JLabel();
    private final JLabel note = new JLabel(" ");

    public VehicleMovementsView() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));

        FormCard form = new FormCard(640, 24);
        lookup.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lookup);
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topRow.setOpaque(false);
        topRow.add(form);
        add(topRow, BorderLayout.NORTH);

        // the result card: a heading line, the table, and a note about expected check-ins
        card.setBackground(Theme.BG_SURFACE);
        card.setBorder(new RoundedBorder(Theme.R_CARD, Theme.BORDER_CARD, new Insets(0, 0, 0, 0), false));
        title.setFont(Theme.F_HEADING);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_CARD),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)));
        note.setFont(Theme.F_SMALL);
        note.setForeground(Theme.TEXT_SECONDARY);
        note.setBorder(BorderFactory.createEmptyBorder(10, 24, 12, 24));

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        // the order is fixed (newest first), so clicking a heading does not re-sort
        table.setAutoCreateRowSorter(false);
        for (int c = 0; c < WIDTHS.length; c++) {
            table.getColumnModel().getColumn(c).setPreferredWidth(WIDTHS[c]);
        }
        // The columns share the width when the window is wide, and the table scrolls sideways when it is narrow.
        scroll.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                fitColumns();
            }
        });
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(600, 260));

        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(note, BorderLayout.SOUTH);
        card.setVisible(false);
        add(card, BorderLayout.CENTER);

        // the rego box is ready for typing as soon as the screen opens
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(lookup::focusBox);
            }
        });
    }

    /**
     * Stretches the columns to the window when it is wide enough, otherwise
     * keeps them full size and scrolls.
     */
    private void fitColumns() {
        int needed = 0;
        for (int w : WIDTHS) {
            needed += w;
        }
        if (scroll.getViewport().getWidth() >= needed) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        } else {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int c = 0; c < WIDTHS.length; c++) {
                table.getColumnModel().getColumn(c).setPreferredWidth(WIDTHS[c]);
                table.getColumnModel().getColumn(c).setWidth(WIDTHS[c]);
            }
        }
    }

    public String getRego() {
        return lookup.getText();
    }

    public void showError(String message) {
        card.setVisible(false);
        lookup.showError(message);
    }

    /**
     * Shows the table.
     *
     * @param heading for example "ABC123 · Toyota Corolla · 4 movements"
     * @param rows one array per row, in column order
     * @param footnote the note under the table, or an empty string
     */
    public void showRows(String heading, List<Object[]> rows, String footnote) {
        lookup.clearError();
        title.setText(heading);
        tableModel.setRowCount(0);
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
        note.setText(footnote.isEmpty() ? " " : footnote);
        card.setVisible(true);
        revalidate();
        repaint();
    }

    public void addController(ActionListener controller) {
        lookup.addController(controller);
        // each time the screen is opened the table is refreshed, so it never shows old movements
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                controller.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, OPENED));
            }
        });
    }
}
