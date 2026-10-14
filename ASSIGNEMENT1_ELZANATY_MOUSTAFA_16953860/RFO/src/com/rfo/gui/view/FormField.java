package com.rfo.gui.view;

import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * One labelled form row: the label above, the input, a line of help text and a
 * red error line. An error also puts the red ring on the input. The red star
 * marks a required field and the small counter on the right (for example "7 /
 * 10") is optional.
 */
public class FormField extends JPanel {

    private final JComponent input;
    private final JLabel counter = new JLabel();
    private final JLabel help = new JLabel();
    private final JLabel error = new JLabel();

    public FormField(String label, boolean required, JComponent input, String helpText) {
        this.input = input;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(required
                ? "<html>" + label + " <span style='color:#B42318'>*</span></html>" : label);
        name.setFont(Theme.F_LABEL);
        name.setForeground(Theme.TEXT_PRIMARY);
        name.setLabelFor(input);
        counter.setFont(Theme.F_SMALL);
        counter.setForeground(Theme.TEXT_SECONDARY);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(name, BorderLayout.WEST);
        top.add(counter, BorderLayout.EAST);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(top);
        add(Box.createVerticalStrut(6));

        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension pref = input.getPreferredSize();
        input.setPreferredSize(new Dimension(pref.width, 40));
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        add(input);

        help.setFont(Theme.F_SMALL);
        help.setForeground(Theme.TEXT_SECONDARY);
        help.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (helpText != null && !helpText.isEmpty()) {
            help.setText(helpText);
            add(Box.createVerticalStrut(6));
            add(help);
        }

        error.setFont(Theme.F_SMALL);
        error.setForeground(Theme.ERR_FG);
        error.setIcon(Icons.icon(Icons.Type.ERROR_CIRCLE, 14, Theme.ERR_FG));
        error.setIconTextGap(6);
        error.setAlignmentX(Component.LEFT_ALIGNMENT);
        error.setVisible(false);
        add(Box.createVerticalStrut(6));
        add(error);
    }

    private int fixedWidth = -1;

    /**
     * Makes this field a set width instead of stretching; the height still
     * follows the content (help and error lines).
     */
    public void setFixedWidth(int width) {
        fixedWidth = width;
        revalidate();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return fixedWidth > 0 ? new Dimension(fixedWidth, d.height) : d;
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        return fixedWidth > 0 ? new Dimension(fixedWidth, d.height) : d;
    }

    public JComponent getInput() {
        return input;
    }

    /**
     * Shows the small counter on the right of the label, or hides it with null.
     */
    public void setCounter(String text) {
        counter.setText(text == null ? "" : text);
    }

    public void setError(String message) {
        int w = input.getWidth() > 0 ? Math.max(input.getWidth() - 24, 100) : 200;
        error.setText("<html><body style='width:" + w + "px'>" + message.replace("<", "&lt;") + "</body></html>");
        error.setVisible(true);
        input.putClientProperty("error", Boolean.TRUE);
        input.repaint();
        revalidate();
    }

    public void clearError() {
        error.setVisible(false);
        input.putClientProperty("error", null);
        input.repaint();
        revalidate();
    }

    public boolean hasError() {
        return error.isVisible();
    }
}
