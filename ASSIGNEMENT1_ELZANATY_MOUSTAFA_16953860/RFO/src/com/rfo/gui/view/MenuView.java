package com.rfo.gui.view;

import com.rfo.gui.theme.Theme;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

/**
 * One menu screen: a breadcrumb, a title and one numbered row per option, like
 * a console menu. An option can be chosen by clicking it, or by typing its
 * number and pressing Enter; the number box under the list shows what has been
 * typed, and the matching row lights up. Backspace deletes a typed digit, or
 * goes back when nothing is typed. Escape clears the number. Each row carries
 * its label as its action command, and an optional Back row (numbered last,
 * like the console's exit option) returns to the menu above.
 */
public class MenuView extends JPanel {

    public static final String BACK = "Back";

    private static final int MAX_DIGITS = 2;
    private static final int REJECT_MS = 2000;

    private final List<MenuOptionButton> optionButtons = new ArrayList<>();
    private final List<MenuOptionButton> allButtons = new ArrayList<>();
    private final MenuOptionButton backButton;
    private final List<String> labels;
    private final NumberEntryBox numberBox = new NumberEntryBox();
    private final JLabel hintMain = new JLabel();
    private final JLabel hintSub = new JLabel();
    private final StringBuilder typed = new StringBuilder();
    private final boolean withBack;
    private final Timer rejectTimer = new Timer(REJECT_MS, e -> clearTyped());
    private boolean rejected = false;
    private MessageSink console;

    /**
     * @param title the big heading
     * @param breadcrumb the small path above it, for example "Main menu ›
     * Fleet"
     */
    public MenuView(String title, String breadcrumb, List<String> options, boolean withBack) {
        super(new BorderLayout());
        this.labels = options;
        this.withBack = withBack;
        setBackground(Theme.BG_WINDOW);
        setFocusable(true);
        rejectTimer.setRepeats(false);

        int rows = options.size() + (withBack ? 1 : 0);
        int rowHeight = rows <= 5 ? Theme.OPTION_H : Theme.OPTION_H_COMPACT;

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(BorderFactory.createEmptyBorder(28, 32, 16, 32));

        JLabel crumb = new JLabel(breadcrumb);
        crumb.setFont(Theme.F_SMALL);
        crumb.setForeground(Theme.TEXT_SECONDARY);
        JLabel heading = new JLabel(title);
        heading.setFont(Theme.F_TITLE);
        heading.setForeground(Theme.TEXT_PRIMARY);
        left(crumb);
        left(heading);
        page.add(crumb);
        page.add(Box.createVerticalStrut(2));
        page.add(heading);
        page.add(Box.createVerticalStrut(20));

        for (int i = 0; i < options.size(); i++) {
            String label = options.get(i);
            MenuOptionButton.Kind kind = MenuInfo.isComingSoon(label) ? MenuOptionButton.Kind.SOON : MenuOptionButton.Kind.NORMAL;
            MenuOptionButton button = new MenuOptionButton(i + 1, label, MenuInfo.describe(label), kind, rowHeight);
            button.setActionCommand(label);
            optionButtons.add(button);
            allButtons.add(button);
            page.add(left(button));
            page.add(Box.createVerticalStrut(8));
        }

        if (withBack) {
            JPanel divider = new JPanel();
            divider.setBackground(Theme.BORDER_DIVIDER);
            divider.setPreferredSize(new Dimension(Theme.FORM_MAX_W, 1));
            divider.setMaximumSize(new Dimension(Theme.FORM_MAX_W, 1));
            page.add(left(divider));
            page.add(Box.createVerticalStrut(6));
            backButton = new MenuOptionButton(options.size() + 1, BACK, "", MenuOptionButton.Kind.BACK, rows <= 5 ? 48 : 44);
            backButton.setActionCommand(BACK);
            allButtons.add(backButton);
            page.add(left(backButton));
        } else {
            backButton = null;
        }

        // number box with its hint to the right
        hintMain.setFont(Theme.F_SMALL.deriveFont(13f));
        hintSub.setFont(Theme.F_SMALL);
        hintSub.setForeground(Theme.TEXT_SECONDARY);
        JPanel hints = new JPanel();
        hints.setOpaque(false);
        hints.setLayout(new BoxLayout(hints, BoxLayout.Y_AXIS));
        hints.add(hintMain);
        hints.add(Box.createVerticalStrut(2));
        hints.add(hintSub);

        JPanel entry = new JPanel();
        entry.setOpaque(false);
        entry.setLayout(new BoxLayout(entry, BoxLayout.X_AXIS));
        entry.add(numberBox);
        entry.add(Box.createHorizontalStrut(12));
        entry.add(hints);
        page.add(Box.createVerticalStrut(rows <= 5 ? 20 : 12));
        page.add(left(entry));

        add(page, BorderLayout.NORTH);
        installKeys();
        update();
    }

    private static <T extends JComponent> T left(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    /**
     * Where "Please enter a valid option." is printed when a typed number
     * matches nothing.
     */
    public void setMessageSink(MessageSink console) {
        this.console = console;
    }

    // keys work whenever this menu is the one showing, wherever the focus is
    private void installKeys() {
        for (char digit = '0'; digit <= '9'; digit++) {
            final char c = digit;
            bind(KeyStroke.getKeyStroke(c), "digit" + c, () -> typeDigit(c));
        }
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter", this::choose);
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "back", this::backspace);
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clear", this::clearTyped);
    }

    private void bind(KeyStroke key, String name, Runnable action) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, name);
        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void typeDigit(char digit) {
        if (rejected) {
            clearTyped();                    // typing again after a rejected number starts fresh
        }
        if (typed.length() < MAX_DIGITS) {
            typed.append(digit);
            update();
        }
    }

    private void backspace() {
        if (typed.length() > 0) {
            if (rejected) {
                clearTyped();
            } else {
                typed.deleteCharAt(typed.length() - 1);
                update();
            }
        } else if (backButton != null) {
            backButton.doClick();
        }
    }

    private void clearTyped() {
        rejectTimer.stop();
        rejected = false;
        typed.setLength(0);
        update();
    }

    private void choose() {
        if (typed.length() == 0) {
            // nothing typed: Enter presses the row that has the keyboard focus (after Tab)
            Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (owner instanceof MenuOptionButton && allButtons.contains(owner)) {
                ((MenuOptionButton) owner).doClick();
            }
            return;
        }
        if (rejected) {
            return;
        }
        MenuOptionButton target = buttonFor(Integer.parseInt(typed.toString()));
        if (target != null) {
            clearTyped();
            target.doClick();
        } else {
            if (console != null) {
                console.print("Please enter a valid option.");
            }
            rejected = true;
            rejectTimer.restart();
            update();
        }
    }

    /**
     * The row with this number, or null if there is none.
     */
    private MenuOptionButton buttonFor(int number) {
        if (number >= 1 && number <= optionButtons.size()) {
            return optionButtons.get(number - 1);
        }
        if (backButton != null && number == optionButtons.size() + 1) {
            return backButton;
        }
        return null;
    }

    /**
     * Refreshes the number box, the highlighted row and the hint next to the
     * box.
     */
    private void update() {
        boolean idle = typed.length() == 0;
        MenuOptionButton match = idle ? null : buttonFor(Integer.parseInt(typed.toString()));
        for (MenuOptionButton b : allButtons) {
            b.setMatched(b == match);
        }
        boolean error = !idle && match == null;
        numberBox.setState(typed.toString(), error);

        int total = optionButtons.size() + (withBack ? 1 : 0);
        String navy = "#1F2A44";
        if (idle) {
            hintMain.setForeground(Theme.TEXT_SECONDARY);
            hintMain.setText("<html>Type an option number, then press <b style='color:" + navy + "'>Enter</b>."
                    + (withBack ? " <b style='color:" + navy + "'>Backspace</b> goes back."
                            : " Or click an option.") + "</html>");
            hintSub.setText("");
        } else if (error) {
            hintMain.setForeground(Theme.ERR_FG);
            hintMain.setText(rejected ? "<html><b>There is no option " + Integer.parseInt(typed.toString()) + ". Choose 1 to " + total + ".</b></html>"
                    : "<html><b>✕ No option " + Integer.parseInt(typed.toString()) + ". Choose 1 to " + total + ".</b></html>");
            hintSub.setText(rejected ? "Clears by itself in 2 seconds, or press Esc" : "Press Esc to clear");
        } else {
            hintMain.setForeground(Theme.TEXT_PRIMARY);
            String name = match == backButton ? "Back" : labels.get(optionButtons.indexOf(match));
            hintMain.setText("<html><b>" + (match == backButton ? "Enter to go back" : "Enter to open " + escape(name)) + "</b></html>");
            hintSub.setText("Backspace deletes the digit · Esc clears");
        }
        hintMain.revalidate();
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;");
    }

    public void addController(ActionListener controller) {
        for (MenuOptionButton button : allButtons) {
            button.addActionListener(controller);
        }
    }
}
