package com.rfo.gui.view;

import com.rfo.gui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

/**
 * The small list of vehicles that drops down under a plate box when Enter is
 * pressed on an empty box. Each row is {plate, make and model, category,
 * odometer}. Enter or a click picks one; Esc closes it.
 */
public final class VehiclePicker {

    private VehiclePicker() {
    }

    public static void show(JComponent anchor, String heading, List<String[]> rows, Consumer<String> onPick) {
        DefaultListModel<String[]> model = new DefaultListModel<>();
        for (String[] r : rows) {
            model.addElement(r);
        }
        JList<String[]> list = new JList<>(model);
        list.setSelectedIndex(0);
        list.setFixedCellHeight(34);
        list.setCellRenderer(new ListCellRenderer<String[]>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends String[]> l, String[] v, int i,
                    boolean selected, boolean focus) {
                JLabel row = new JLabel("<html><span style='font-family:monospace'><b>" + v[0] + "</b></span>"
                        + " &nbsp; " + v[1] + " &nbsp;<span style='color:#4F5D75'>" + v[2] + " · " + v[3] + "</span></html>");
                row.setFont(Theme.F_BODY);
                row.setOpaque(true);
                row.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                row.setBackground(selected ? Theme.PRIMARY_TINT : Theme.BG_SURFACE);
                row.setForeground(Theme.TEXT_PRIMARY);
                return row;
            }
        });
        JPopupMenu popup = new JPopupMenu();
        popup.setFocusable(true);
        JLabel head = new JLabel(heading + " Pick one, or press Esc to type a plate.");
        head.setFont(Theme.F_SMALL);
        head.setForeground(Theme.TEXT_SECONDARY);
        head.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JScrollPane scroll = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_DIVIDER));
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Theme.BG_SURFACE);
        box.add(head, BorderLayout.NORTH);
        box.add(scroll, BorderLayout.CENTER);
        box.setPreferredSize(new Dimension(540, Math.min(rows.size(), 7) * 34 + 2 + head.getPreferredSize().height));
        popup.add(box);

        Runnable choose = () -> {
            String[] picked = list.getSelectedValue();
            popup.setVisible(false);
            if (picked != null) {
                onPick.accept(picked[0]);
            }
        };
        bind(list, KeyEvent.VK_ENTER, choose);
        bind(list, KeyEvent.VK_ESCAPE, () -> {
            popup.setVisible(false);
            anchor.requestFocusInWindow();
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                list.setSelectedIndex(list.locationToIndex(e.getPoint()));
                choose.run();
            }
        });
        // the list is a separate little window, so close it when the screen under it is no longer on show
        // (for example the user pressed Back), otherwise it stays floating over the next screen
        java.awt.event.HierarchyListener closer = e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && !anchor.isShowing()) {
                popup.setVisible(false);
            }
        };
        anchor.addHierarchyListener(closer);
        popup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                anchor.removeHierarchyListener(closer);
            }

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
                anchor.removeHierarchyListener(closer);
            }
        });
        popup.show(anchor, 0, anchor.getHeight() + 2);
        SwingUtilities.invokeLater(() -> list.requestFocusInWindow());
    }

    private static void bind(JComponent c, int keyCode, Runnable action) {
        String name = "rfoPick" + keyCode;
        c.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(keyCode, 0), name);
        c.getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }
}
