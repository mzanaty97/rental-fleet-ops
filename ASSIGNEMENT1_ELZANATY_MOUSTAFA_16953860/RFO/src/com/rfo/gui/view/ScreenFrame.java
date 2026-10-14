package com.rfo.gui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

/**
 * Wraps a feature screen with a bar on top: a Back button and the screen's
 * title. Every feature screen sits inside one of these so the way back to the
 * menu is always in the same place. Pressing Backspace also goes back, unless
 * the user is typing in a text box.
 */
public class ScreenFrame extends JPanel {

    private final JButton backButton = new JButton("< Back");

    public ScreenFrame(String title, JComponent content) {
        super(new BorderLayout());
        setBackground(Color.WHITE);
        setFocusable(true);

        backButton.setActionCommand(MenuView.BACK);

        JLabel heading = new JLabel(title);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 16f));
        heading.setForeground(HomeView.NAVY);

        JPanel bar = new JPanel(new BorderLayout(14, 0));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));
        bar.add(backButton, BorderLayout.WEST);
        bar.add(heading, BorderLayout.CENTER);

        add(bar, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        installBackspace();
    }

    private void installBackspace() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "back");
        getActionMap().put("back", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // a text box keeps its own Backspace
                if (!(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() instanceof JTextComponent)) {
                    backButton.doClick();
                }
            }
        });
    }

    public void addController(ActionListener controller) {
        backButton.addActionListener(controller);
    }
}
