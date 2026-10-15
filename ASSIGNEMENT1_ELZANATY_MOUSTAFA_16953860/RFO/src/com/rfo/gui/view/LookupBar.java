package com.rfo.gui.view;

import com.rfo.gui.theme.Theme;
import java.awt.Component;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A search row used by the lookup screens: a labelled text box, a button, and a
 * small help line. Pressing Enter in the box presses the button. Errors show
 * under the box, like on the form screens.
 */
public class LookupBar extends JPanel {

    private final JTextField box = new JTextField();
    private final FormField field;
    private final JButton button;

    public LookupBar(String label, int boxWidth, String buttonText, String command, String help) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        box.setFont(Theme.F_MONO);
        field = new FormField(label, false, box, null);
        field.setFixedWidth(boxWidth);
        button = new JButton(buttonText);
        button.putClientProperty("variant", "primary");
        button.setActionCommand(command);

        JPanel buttonBox = new JPanel();
        buttonBox.setOpaque(false);
        buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.Y_AXIS));
        buttonBox.add(Box.createVerticalStrut(22));
        buttonBox.add(button);
        JPanel helpBox = new JPanel();
        helpBox.setOpaque(false);
        helpBox.setLayout(new BoxLayout(helpBox, BoxLayout.Y_AXIS));
        helpBox.add(Box.createVerticalStrut(34));
        JLabel helpLabel = new JLabel(help);
        helpLabel.setFont(Theme.F_SMALL);
        helpLabel.setForeground(Theme.TEXT_SECONDARY);
        helpBox.add(helpLabel);

        field.setAlignmentY(Component.TOP_ALIGNMENT);
        buttonBox.setAlignmentY(Component.TOP_ALIGNMENT);
        helpBox.setAlignmentY(Component.TOP_ALIGNMENT);
        add(field);
        add(Box.createHorizontalStrut(12));
        add(buttonBox);
        add(Box.createHorizontalStrut(12));
        add(helpBox);

        box.addActionListener(e -> button.doClick());
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

    public String getText() {
        return box.getText().trim();
    }

    public void showError(String message) {
        field.setError(message);
        box.requestFocusInWindow();
    }

    public void clearError() {
        field.clearError();
    }

    public void focusBox() {
        box.requestFocusInWindow();
    }

    public void addController(ActionListener controller) {
        button.addActionListener(controller);
    }
}
