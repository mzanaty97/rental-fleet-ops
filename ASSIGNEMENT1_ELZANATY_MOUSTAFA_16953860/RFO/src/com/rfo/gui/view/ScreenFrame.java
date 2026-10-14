package com.rfo.gui.view;

import com.rfo.gui.theme.BadgeLabel;
import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

/**
 * Wraps a feature screen with a bar on top: a Back button, a thin divider, the
 * path to the screen in small text and its title in large text. Every feature
 * screen sits inside one of these so the way back to the menu is always in the
 * same place. Pressing Backspace also goes back, unless the user is typing in a
 * text box.
 */
public class ScreenFrame extends JPanel {

    private final JButton backButton = new JButton("Back");

    /**
     * @param title the screen's name
     * @param breadcrumb where it sits, for example "Movements › Revenue
     * Movements"; may be empty
     */
    public ScreenFrame(String title, String breadcrumb, JComponent content) {
        super(new BorderLayout());
        setBackground(Theme.BG_WINDOW);
        setFocusable(true);

        backButton.setActionCommand(MenuView.BACK);
        backButton.putClientProperty("variant", "link");
        backButton.putClientProperty("size", "compact");
        backButton.setIcon(Icons.icon(Icons.Type.CHEVRON_LEFT, 16, Theme.PRIMARY));
        backButton.setIconTextGap(6);

        JLabel crumb = new JLabel(breadcrumb);
        crumb.setFont(Theme.F_SMALL);
        crumb.setForeground(Theme.TEXT_SECONDARY);
        crumb.setVisible(!breadcrumb.isEmpty());
        JLabel heading = new JLabel(title);
        heading.setFont(Theme.F_TITLE);
        heading.setForeground(Theme.TEXT_PRIMARY);

        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        names.add(crumb);
        names.add(heading);

        JPanel divider = new JPanel();
        divider.setBackground(Theme.BORDER_CARD);
        divider.setPreferredSize(new Dimension(1, 28));

        JPanel bar = new JPanel(new GridBagLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(16, 32, 0, 32));
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.gridx = 0;
        bar.add(backButton, c);
        c.gridx = 1;
        c.insets = new Insets(0, 12, 0, 12);
        bar.add(divider, c);
        c.gridx = 2;
        c.insets = new Insets(0, 0, 0, 0);
        bar.add(names, c);
        if (content instanceof PlaceholderView) {
            c.gridx = 3;
            c.insets = new Insets(0, 12, 0, 0);
            bar.add(new BadgeLabel("COMING SOON", false), c);
        }
        c.gridx = 4;
        c.weightx = 1;
        bar.add(Box.createHorizontalGlue(), c);

        add(bar, BorderLayout.NORTH);
        if (content instanceof PlaceholderView) {
            add(content, BorderLayout.CENTER);
        } else {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(Theme.BG_SURFACE);
            card.setBorder(BorderFactory.createLineBorder(Theme.BORDER_CARD, 1));
            card.add(content, BorderLayout.CENTER);
            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false);
            wrap.setBorder(BorderFactory.createEmptyBorder(16, 32, 24, 32));
            wrap.add(card, BorderLayout.CENTER);
            add(wrap, BorderLayout.CENTER);
        }

        installBackspace();
    }

    /**
     * Goes back to the menu, exactly as if Back had been clicked.
     */
    public void goBack() {
        backButton.doClick();
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
