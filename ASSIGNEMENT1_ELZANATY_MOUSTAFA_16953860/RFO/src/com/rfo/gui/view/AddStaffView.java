package com.rfo.gui.view;

import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.Theme;
import com.rfo.model.StaffRole;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * The Add Staff form: ID, password, first and last name, and a role picked from
 * four chips. The Add button stays off until everything is filled in. The last
 * role chosen is kept after a successful add.
 */
public class AddStaffView extends JPanel implements PlainScreen {

    public static final String ADD = "add";
    private static final int W = 640, PAD = 24, COL_GAP = 20;

    private final JTextField idBox = new JTextField();
    private final JPasswordField passwordBox = new JPasswordField();
    private final JTextField firstBox = new JTextField();
    private final JTextField lastBox = new JTextField();
    private final FormField idField = new FormField("Staff ID", true, idBox, "Letters and digits, up to 10 characters");
    private final FormField passwordField = new FormField("Password", true, passwordBox, "At least 6 characters");
    private final FormField firstField = new FormField("First name", true, firstBox, null);
    private final FormField lastField = new FormField("Last name", true, lastBox, null);
    private final JLabel roleError = new JLabel();
    private final RoleChip[] chips = new RoleChip[StaffRole.values().length];
    private final JButton addButton = new JButton("Add staff");
    private StaffRole role;

    public AddStaffView() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        idBox.setFont(Theme.F_MONO);
        idField.setCounter("0 / 10");

        FormCard card = new FormCard(W, PAD);
        int colW = (W - 2 * PAD - COL_GAP) / 2;
        card.add(row(idField, passwordField, colW));
        card.add(Box.createVerticalStrut(16));
        card.add(row(firstField, lastField, colW));
        card.add(Box.createVerticalStrut(16));

        JLabel roleLabel = new JLabel("<html>Role <span style='color:#B42318'>*</span></html>");
        roleLabel.setFont(Theme.F_LABEL);
        roleLabel.setForeground(Theme.TEXT_PRIMARY);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(roleLabel);
        card.add(Box.createVerticalStrut(6));

        JPanel chipRow = new JPanel(new GridLayout(1, 4, 8, 0));
        chipRow.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        StaffRole[] roles = StaffRole.values();
        for (int i = 0; i < roles.length; i++) {
            chips[i] = new RoleChip(roles[i]);
            group.add(chips[i]);
            chipRow.add(chips[i]);
            final StaffRole r = roles[i];
            chips[i].addActionListener(e -> {
                role = r;
                roleError.setVisible(false);
                updateAddEnabled();
            });
        }
        chipRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        chipRow.setPreferredSize(new Dimension(W - 2 * PAD, 40));
        chipRow.setMaximumSize(new Dimension(W - 2 * PAD, 40));
        card.add(chipRow);
        card.add(Box.createVerticalStrut(6));
        JLabel roleHelp = new JLabel("Role controls which menu options they see. The last role chosen is kept for quick entry.");
        roleHelp.setFont(Theme.F_SMALL);
        roleHelp.setForeground(Theme.TEXT_SECONDARY);
        roleHelp.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(roleHelp);
        roleError.setFont(Theme.F_SMALL);
        roleError.setForeground(Theme.ERR_FG);
        roleError.setIcon(Icons.icon(Icons.Type.ERROR_CIRCLE, 14, Theme.ERR_FG));
        roleError.setIconTextGap(6);
        roleError.setVisible(false);
        roleError.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Box.createVerticalStrut(6));
        card.add(roleError);

        card.add(Box.createVerticalStrut(16));
        JPanel divider = new JPanel();
        divider.setBackground(Theme.BORDER_DIVIDER);
        divider.setPreferredSize(new Dimension(10, 1));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(divider);
        card.add(Box.createVerticalStrut(16));

        addButton.putClientProperty("variant", "primary");
        addButton.setIcon(Icons.icon(Icons.Type.PLUS, 16, Theme.TEXT_ON_PRIMARY));
        addButton.setActionCommand(ADD);
        addButton.setEnabled(false);
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(addButton);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(buttons);

        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(card);

        // Enter walks along the boxes and adds from the last one
        idBox.addActionListener(e -> passwordBox.requestFocusInWindow());
        passwordBox.addActionListener(e -> firstBox.requestFocusInWindow());
        firstBox.addActionListener(e -> lastBox.requestFocusInWindow());
        lastBox.addActionListener(e -> {
            if (addButton.isEnabled()) {
                addButton.doClick();
            }
        });

        hook(idBox, idField);
        hook(passwordBox, passwordField);
        hook(firstBox, firstField);
        hook(lastBox, lastField);
    }

    private static JPanel row(FormField left, FormField right, int colW) {
        JPanel row = new JPanel(new GridLayout(1, 2, COL_GAP, 0));
        row.setOpaque(false);
        row.add(left);
        row.add(right);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(W - 2 * PAD, Integer.MAX_VALUE));
        return row;
    }

    private void hook(javax.swing.text.JTextComponent box, FormField field) {
        box.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }

            private void changed() {
                field.clearError();
                idField.setCounter(idBox.getText().trim().length() + " / 10");
                updateAddEnabled();
            }
        });
    }

    private void updateAddEnabled() {
        addButton.setEnabled(!idBox.getText().trim().isEmpty() && passwordBox.getPassword().length > 0
                && !firstBox.getText().trim().isEmpty() && !lastBox.getText().trim().isEmpty() && role != null);
    }

    public String getStaffId() {
        return idBox.getText().trim();
    }

    public String getPassword() {
        return new String(passwordBox.getPassword());
    }

    public String getFirstName() {
        return firstBox.getText().trim();
    }

    public String getLastName() {
        return lastBox.getText().trim();
    }

    /**
     * The chosen role, or null when none is chosen yet.
     */
    public StaffRole getRole() {
        return role;
    }

    public void showIdError(String message) {
        idField.setError(message);
        idBox.requestFocusInWindow();
    }

    public void showPasswordError(String message) {
        passwordField.setError(message);
        passwordBox.requestFocusInWindow();
    }

    public void showFirstNameError(String message) {
        firstField.setError(message);
        firstBox.requestFocusInWindow();
    }

    public void showRoleError(String message) {
        roleError.setText(message);
        roleError.setVisible(true);
    }

    /**
     * Empties the text boxes for the next person. The role stays chosen.
     */
    public void clearForm() {
        idBox.setText("");
        passwordBox.setText("");
        firstBox.setText("");
        lastBox.setText("");
        idBox.requestFocusInWindow();
    }

    public void addController(ActionListener controller) {
        addButton.addActionListener(controller);
    }

    /**
     * One role chip: a coloured dot and the role name; the chosen one gets a
     * teal outline and a tick.
     */
    private static class RoleChip extends JToggleButton {

        private final StaffRole chipRole;

        RoleChip(StaffRole chipRole) {
            this.chipRole = chipRole;
            setFocusable(true);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            getAccessibleContext().setAccessibleName(chipRole.toString());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            boolean on = isSelected();
            if (on) {
                g2.setColor(Theme.PRIMARY_TINT);
                g2.fillRoundRect(0, 0, w - 1, h - 1, Theme.R_OPTION * 2, Theme.R_OPTION * 2);
                g2.setColor(Theme.PRIMARY);
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, Theme.R_OPTION * 2, Theme.R_OPTION * 2);
            } else {
                g2.setColor(Theme.BG_SURFACE);
                g2.fillRoundRect(0, 0, w - 1, h - 1, Theme.R_OPTION * 2, Theme.R_OPTION * 2);
                g2.setColor(Theme.BORDER_FIELD);
                g2.drawRoundRect(0, 0, w - 1, h - 1, Theme.R_OPTION * 2, Theme.R_OPTION * 2);
            }
            g2.setColor(Theme.roleSolid(chipRole.toString()));
            g2.fillOval(10, h / 2 - 5, 10, 10);
            String text = (on ? "✓ " : "") + chipRole;
            java.awt.Font f = chipRole == StaffRole.FLEET_OPERATOR ? Theme.F_SMALL.deriveFont(java.awt.Font.BOLD) : Theme.F_LABEL;
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Theme.TEXT_PRIMARY);
            g2.drawString(text, 26, (h + fm.getAscent() - fm.getDescent()) / 2);
            if (isFocusOwner()) {
                g2.setColor(Theme.FOCUS);
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.drawRoundRect(-1, -1, w + 1, h + 1, Theme.R_OPTION * 2 + 2, Theme.R_OPTION * 2 + 2);
            }
            g2.dispose();
        }
    }
}
