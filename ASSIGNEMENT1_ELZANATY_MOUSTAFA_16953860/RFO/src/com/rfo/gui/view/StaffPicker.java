package com.rfo.gui.view;

import com.rfo.model.Staff;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * A drop down of staff members shown as "ID · First Last". Setting the list
 * keeps the same person selected.
 */
public class StaffPicker extends JComboBox<Staff> {

    private boolean updating;

    public StaffPicker() {
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean selected, boolean focus) {
                java.awt.Component c = super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Staff) {
                    Staff s = (Staff) value;
                    setText(s.getStaffID() + "  ·  " + s.getFName() + " " + s.getLName());
                }
                setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    public void setStaff(List<Staff> staff) {
        String keep = getSelectedStaff() == null ? null : getSelectedStaff().getStaffID();
        updating = true;
        DefaultComboBoxModel<Staff> model = new DefaultComboBoxModel<>();
        Staff pick = null;
        for (Staff s : staff) {
            model.addElement(s);
            if (s.getStaffID().equals(keep)) {
                pick = s;
            }
        }
        setModel(model);
        if (pick != null) {
            setSelectedItem(pick);
        }
        updating = false;
    }

    public boolean isUpdating() {
        return updating;
    }

    public Staff getSelectedStaff() {
        return (Staff) getSelectedItem();
    }
}
