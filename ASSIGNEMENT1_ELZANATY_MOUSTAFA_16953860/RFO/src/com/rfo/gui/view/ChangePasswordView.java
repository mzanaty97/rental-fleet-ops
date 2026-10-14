package com.rfo.gui.view;

import com.rfo.gui.theme.Theme;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * The Change Password form: current password, then the new one twice. Each box
 * can show its own red error. Enter moves to the next box and Enter in the last
 * one saves. It only collects what is typed.
 */
public class ChangePasswordView extends JPanel implements PlainScreen {

    public static final String SAVE = "save";

    private final JPasswordField currentBox = new JPasswordField();
    private final JPasswordField newBox = new JPasswordField();
    private final JPasswordField confirmBox = new JPasswordField();
    private final FormField currentField = new FormField("Current password", false, currentBox, null);
    private final FormField newField = new FormField("New password", false, newBox, "At least 6 characters. Use something you have not used before.");
    private final FormField confirmField = new FormField("Confirm new password", false, confirmBox, null);
    private final JButton saveButton = new JButton("Save password");
    private final JButton clearButton = new JButton("Clear");

    public ChangePasswordView() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        FormCard card = new FormCard(480, 24);
        JPanel divider = new JPanel();
        divider.setBackground(Theme.BORDER_DIVIDER);
        divider.setPreferredSize(new Dimension(10, 1));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        saveButton.putClientProperty("variant", "primary");
        saveButton.setActionCommand(SAVE);
        clearButton.addActionListener(e -> {
            clearForm();
            currentBox.requestFocusInWindow();
        });
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(clearButton);
        buttons.add(Box.createHorizontalStrut(8));
        buttons.add(saveButton);

        card.add(left(currentField));
        card.add(Box.createVerticalStrut(16));
        card.add(left(divider));
        card.add(Box.createVerticalStrut(16));
        card.add(left(newField));
        card.add(Box.createVerticalStrut(16));
        card.add(left(confirmField));
        card.add(Box.createVerticalStrut(20));
        card.add(left(buttons));

        JLabel hint = new JLabel("Enter moves to the next field. Enter in the last field saves.");
        hint.setFont(Theme.F_SMALL);
        hint.setForeground(Theme.TEXT_SECONDARY);

        add(left(card));
        add(Box.createVerticalStrut(16));
        add(left(hint));

        // Enter moves along, and saves from the last box
        currentBox.addActionListener(e -> newBox.requestFocusInWindow());
        newBox.addActionListener(e -> confirmBox.requestFocusInWindow());
        confirmBox.addActionListener(e -> saveButton.doClick());

        // typing in a box clears its own error
        hookClear(currentBox, currentField);
        hookClear(newBox, newField);
        hookClear(confirmBox, confirmField);
    }

    private static <T extends javax.swing.JComponent> T left(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private static void hookClear(JPasswordField box, FormField field) {
        box.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                field.clearError();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                field.clearError();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                field.clearError();
            }
        });
    }

    public String getCurrentPassword() {
        return new String(currentBox.getPassword());
    }

    public String getNewPassword() {
        return new String(newBox.getPassword());
    }

    public String getConfirmPassword() {
        return new String(confirmBox.getPassword());
    }

    public void showCurrentError(String message) {
        currentField.setError(message);
        currentBox.requestFocusInWindow();
    }

    public void showNewError(String message) {
        newField.setError(message);
        newBox.requestFocusInWindow();
    }

    public void showConfirmError(String message) {
        confirmField.setError(message);
        confirmBox.requestFocusInWindow();
    }

    public void clearForm() {
        currentBox.setText("");
        newBox.setText("");
        confirmBox.setText("");
        currentField.clearError();
        newField.clearError();
        confirmField.clearError();
    }

    public void addController(ActionListener controller) {
        saveButton.addActionListener(controller);
    }
}
