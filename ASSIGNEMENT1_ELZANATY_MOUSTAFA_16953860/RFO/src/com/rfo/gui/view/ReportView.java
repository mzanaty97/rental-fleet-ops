package com.rfo.gui.view;

import com.rfo.gui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * One table screen that serves all four reports. The controller gives it the
 * column names, an optional drop down (a location or a staff member), and the
 * rows. When there are no rows it shows a message instead of an empty table. A
 * column called "Status" is drawn as a coloured badge by the table style.
 */
public class ReportView extends JPanel {

    public static final String PICKED = "picked";
    public static final String OPENED = "opened";

    private static final String TABLE = "table";
    private static final String EMPTY = "empty";

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JComboBox<String> picker = new JComboBox<>();
    private final JLabel pickerLabel = new JLabel();
    private final JLabel summary = new JLabel(" ");
    private final JLabel hint = new JLabel(" ");
    private final JLabel emptyTitle = new JLabel();
    private final JLabel emptySub = new JLabel();
    private final CardLayout cards = new CardLayout();
    private final JPanel body = new JPanel(cards);
    private boolean filling;

    /**
     * @param columns the column names
     * @param numberColumns the indexes of columns that hold whole numbers
     * (sorted and aligned as numbers)
     * @param pickerText the label for the drop down, or null when the report
     * has none
     */
    public ReportView(String[] columns, int[] numberColumns, String pickerText) {
        super(new BorderLayout());
        setBackground(Color.WHITE);

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                for (int n : numberColumns) {
                    if (n == column) {
                        return Integer.class;
                    }
                }
                return String.class;
            }
        };
        table = new JTable(tableModel);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(Color.WHITE);
        if (pickerText != null) {
            pickerLabel.setText(pickerText);
            pickerLabel.setFont(Theme.F_LABEL);
            bar.add(pickerLabel);
            bar.add(picker);
        }
        summary.setFont(Theme.F_BODY_B);
        hint.setFont(Theme.F_SMALL);
        hint.setForeground(Theme.TEXT_SECONDARY);
        bar.add(summary);
        bar.add(hint);

        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel empty = new JPanel(new GridBagLayout());
        empty.setBackground(Color.WHITE);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        emptyTitle.setFont(Theme.F_SECTION);
        emptyTitle.setForeground(Theme.TEXT_PRIMARY);
        emptyTitle.setAlignmentX(CENTER_ALIGNMENT);
        emptySub.setFont(Theme.F_SMALL.deriveFont(13f));
        emptySub.setForeground(Theme.TEXT_SECONDARY);
        emptySub.setAlignmentX(CENTER_ALIGNMENT);
        text.add(emptyTitle);
        text.add(javax.swing.Box.createVerticalStrut(8));
        text.add(emptySub);
        empty.add(text);

        body.add(scroll, TABLE);
        body.add(empty, EMPTY);
        add(bar, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    /**
     * Fills the drop down and selects one entry. Does not tell the controller.
     */
    public void setPickerItems(List<String> labels, int selected) {
        filling = true;
        picker.removeAllItems();
        for (String label : labels) {
            picker.addItem(label);
        }
        if (selected >= 0 && selected < labels.size()) {
            picker.setSelectedIndex(selected);
        }
        filling = false;
    }

    /**
     * The index of the chosen drop down entry, or -1.
     */
    public int getPickedIndex() {
        return picker.getSelectedIndex();
    }

    /**
     * Replaces the rows. With no rows the table gives way to the empty message.
     *
     * @param rows one array per row, in column order
     * @param summaryText for example "3 vehicles"
     * @param hintText a small note next to the summary
     * @param emptyMessage the heading shown when there are no rows
     * @param emptyNote the line under the heading
     */
    public void setRows(List<Object[]> rows, String summaryText, String hintText, String emptyMessage,
            String emptyNote) {
        tableModel.setRowCount(0);
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
        summary.setText(summaryText);
        hint.setText(hintText);
        emptyTitle.setText(emptyMessage);
        emptySub.setText(emptyNote);
        cards.show(body, rows.isEmpty() ? EMPTY : TABLE);
    }

    public void addController(ActionListener controller) {
        // each time the screen is opened, ask the controller to refresh and say what it found
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                controller.actionPerformed(new java.awt.event.ActionEvent(this,
                        java.awt.event.ActionEvent.ACTION_PERFORMED, OPENED));
            }
        });
        picker.addActionListener(e -> {
            if (!filling) {
                controller.actionPerformed(new java.awt.event.ActionEvent(this,
                        java.awt.event.ActionEvent.ACTION_PERFORMED, PICKED));
            }
        });
    }
}
