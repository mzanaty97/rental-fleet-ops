package com.rfo.gui.view;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The title shown on a tab: a label plus a small close button. Pass null for
 * the listener to get a tab that cannot be closed (the Home tab).
 */
public class TabTitle extends JPanel {

    private final JLabel label;

    public TabTitle(String title, ActionListener closeAction) {
        super(new FlowLayout(FlowLayout.LEFT, 6, 0));
        setOpaque(false);

        label = new JLabel(title);
        add(label);

        if (closeAction != null) {
            JButton close = new JButton("✕");
            close.setToolTipText("Close this tab");
            close.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            close.setContentAreaFilled(false);
            close.setFocusable(false);
            close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            close.addActionListener(closeAction);
            add(close);
        }
    }

    public void setTitle(String title) {
        label.setText(title);
    }
}
