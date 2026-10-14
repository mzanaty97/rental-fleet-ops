package com.rfo.gui.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * RentalFleetOps design tokens and one-call Swing setup.
 *
 * Call Theme.installDefaults() as the very first line of main(), before any
 * component is created. It switches to the cross-platform Metal look and feel
 * (the macOS and Windows native looks ignore most colour keys), removes Metal's
 * gradients, and sets fonts and colours for every standard component.
 */
public final class Theme {

    private Theme() {
    }

    // ---------------------------------------------------------------- neutrals
    public static final Color NAVY_900 = new Color(0x1F2A44); // tab bar, number box, primary text
    public static final Color NAVY_800 = new Color(0x2B3857); // unselected tab hover
    public static final Color BG_WINDOW = new Color(0xF4F6F9); // content ground, selected tab
    public static final Color BG_SURFACE = new Color(0xFFFFFF); // cards, fields, tables, header, console
    public static final Color BG_ZEBRA = new Color(0xF8FAFC); // every second row
    public static final Color BG_SUBTLE = new Color(0xEEF2F7); // table header, key badge, idle number box
    public static final Color BG_HOVER = new Color(0xF0F7F6); // row and option hover
    public static final Color BG_SELECTED = new Color(0xD6ECE9); // selection
    public static final Color BORDER_DIVIDER = new Color(0xE3E8EF); // row lines, separators
    public static final Color BORDER_CARD = new Color(0xD8DEE8); // card and option outlines
    public static final Color BORDER_FIELD = new Color(0x7C8799); // field, combo, secondary button outline
    public static final Color BORDER_KEY = new Color(0xCBD3DF); // key badge outline
    public static final Color BORDER_DASHED = new Color(0xB8C2D1); // coming soon outline, idle number box, scrollbar thumb

    // ---------------------------------------------------------------- text
    public static final Color TEXT_PRIMARY = new Color(0x1F2A44);
    public static final Color TEXT_SECONDARY = new Color(0x4F5D75);
    public static final Color TEXT_DISABLED = new Color(0x8A96AA);
    public static final Color TEXT_ON_NAVY = new Color(0xC9D2E3);
    public static final Color TEXT_ON_PRIMARY = Color.WHITE;

    // ---------------------------------------------------------------- action and focus
    public static final Color PRIMARY = new Color(0x0F766E);
    public static final Color PRIMARY_HOVER = new Color(0x115E59);
    public static final Color PRIMARY_PRESSED = new Color(0x134E4A);
    public static final Color PRIMARY_TINT = new Color(0xE6F4F2);
    public static final Color PRIMARY_TINT_BORDER = new Color(0x9FD1CB);
    public static final Color PRIMARY_TINT_TEXT = new Color(0x0B4F4A);
    public static final Color FOCUS = new Color(0x2563EB);
    public static final Color DANGER = new Color(0xB42318);
    public static final Color DANGER_HOVER = new Color(0x912018);
    public static final Color DANGER_PRESSED = new Color(0x7A1A14);
    public static final Color DANGER_FIELD_BG = new Color(0xFFFBFA);
    public static final Color DISABLED_FILL = new Color(0xE3E8EF);

    // ---------------------------------------------------------------- feedback: text, tint, tint line, dark text
    public static final Color OK_FG = new Color(0x1E6B42), OK_BG = new Color(0xE8F5EE), OK_LINE = new Color(0xA7D8BC), OK_DARK = new Color(0x14512F);
    public static final Color ERR_FG = new Color(0xB42318), ERR_BG = new Color(0xFDECEA), ERR_LINE = new Color(0xF3B8B2), ERR_DARK = new Color(0x7A1A14);
    public static final Color WARN_FG = new Color(0x8A5A00), WARN_BG = new Color(0xFFF4DB), WARN_LINE = new Color(0xF2D48F), WARN_DARK = new Color(0x5C3C00);
    public static final Color INFO_FG = new Color(0x1D5FA8), INFO_BG = new Color(0xE8F1FB), INFO_LINE = new Color(0xD8DEE8), INFO_DARK = new Color(0x1F2A44);

    /**
     * Badge colours: text, fill, dot. Look up with badge("AVAILABLE").
     */
    public static final class Badge {

        public final Color text, fill, dot;

        Badge(int text, int fill, int dot) {
            this.text = new Color(text);
            this.fill = new Color(fill);
            this.dot = new Color(dot);
        }
    }

    public static Badge badge(String key) {
        switch (key) {
            case "AVAILABLE":
                return new Badge(0x1B6B3F, 0xE3F4EA, 0x2E9E5B);
            case "IN_USE":
            case "OPEN":
                return new Badge(0x17578C, 0xE3EEFA, 0x1F6FB2);
            case "WORKSHOP":
            case "INCOMPLETE":
                return new Badge(0x8A4B00, 0xFFF1DC, 0xD97706);
            case "TO_BE_CLEANED":
                return new Badge(0x5B3A9E, 0xEEE8FA, 0x7B3FB5);
            case "SERVICE_DUE":
                return new Badge(0xA8261B, 0xFDE8E6, 0xD92D20);
            case "CLOSED":
                return new Badge(0x4A5568, 0xECEFF3, 0x4A5568);
            case "Active":
                return new Badge(0x1B6B3F, 0xE3F4EA, 0x2E9E5B);
            case "MANAGER":
                return new Badge(0x2F3C94, 0xE8EAF8, 0x3949AB);
            case "AGENT":
                return new Badge(0x17578C, 0xE3EEFA, 0x1F6FB2);
            case "MECHANIC":
                return new Badge(0x8F4207, 0xFDF0E1, 0xB45309);
            case "FLEET_OPERATOR":
                return new Badge(0x6630A0, 0xF1E9FA, 0x7B3FB5);
            default:
                return new Badge(0x4A5568, 0xECEFF3, 0x8A96AA); // DEFLEETED, Closed, COMING SOON
        }
    }

    // ---------------------------------------------------------------- spacing and shape (px)
    public static final int S1 = 4, S2 = 8, S3 = 12, S4 = 16, S5 = 24, S6 = 32, S7 = 48;
    public static final int R_CONTROL = 6, R_OPTION = 8, R_CARD = 10;
    public static final int CONTROL_H = 40, BUTTON_LARGE_H = 44, OPTION_H = 60, OPTION_H_COMPACT = 52;
    public static final int ROW_H = 40, HEADER_ROW_H = 36;
    public static final int TABBAR_H = 44, TAB_H = 34, SESSION_HEADER_H = 64;
    public static final int CONSOLE_H = 36, CONSOLE_EXPANDED_H = 168;
    public static final int FORM_MAX_W = 640;

    // ---------------------------------------------------------------- fonts
    static {
        // Must run before AWT loads fonts, so it sits above the font fields.
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
    }

    public static final String UI_FAMILY = pick("Segoe UI", "Helvetica Neue", Font.SANS_SERIF);
    public static final String MONO_FAMILY = pick("Consolas", "Menlo", Font.MONOSPACED);

    public static final Font F_DISPLAY = new Font(UI_FAMILY, Font.BOLD, 32);
    public static final Font F_TITLE = new Font(UI_FAMILY, Font.BOLD, 22);
    public static final Font F_HEADING = new Font(UI_FAMILY, Font.BOLD, 20);
    public static final Font F_SECTION = new Font(UI_FAMILY, Font.BOLD, 16);
    public static final Font F_OPTION = new Font(UI_FAMILY, Font.BOLD, 15);
    public static final Font F_BODY = new Font(UI_FAMILY, Font.PLAIN, 14);
    public static final Font F_BODY_B = new Font(UI_FAMILY, Font.BOLD, 14);
    public static final Font F_BUTTON = new Font(UI_FAMILY, Font.BOLD, 14);
    public static final Font F_LABEL = new Font(UI_FAMILY, Font.BOLD, 13);
    public static final Font F_SMALL = new Font(UI_FAMILY, Font.PLAIN, 12);
    public static final Font F_BADGE = new Font(UI_FAMILY, Font.BOLD, 11);
    public static final Font F_MONO = new Font(MONO_FAMILY, Font.PLAIN, 13);
    public static final Font F_KEY = new Font(MONO_FAMILY, Font.BOLD, 13);
    public static final Font F_NUMERAL = new Font(MONO_FAMILY, Font.BOLD, 22);

    /**
     * The solid colour of a role: avatar and tab stripe.
     */
    public static Color roleSolid(String role) {
        switch (role) {
            case "MANAGER":
                return new Color(0x3949AB);
            case "AGENT":
                return new Color(0x1F6FB2);
            case "MECHANIC":
                return new Color(0xB45309);
            case "FLEET_OPERATOR":
                return new Color(0x7B3FB5);
            default:
                return new Color(0x4A5568);
        }
    }

    private static String pick(String first, String second, String fallback) {
        List<String> installed = Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        if (installed.contains(first)) {
            return first;
        }
        if (installed.contains(second)) {
            return second;
        }
        return fallback;
    }

    // ---------------------------------------------------------------- setup
    /**
     * Call once, first thing in main(), before building any window.
     */
    public static void installDefaults() {
        try {
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception ignored) {
            // Metal ships with every JDK; if it fails we simply keep the default look.
        }

        // 1. One body font for every component, then the exceptions below.
        FontUIResource body = new FontUIResource(F_BODY);
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        for (Object key : defaults.keySet().toArray()) {
            if (key instanceof String && ((String) key).endsWith(".font")) {
                UIManager.put(key, body);
            }
        }

        // 2. Metal paints gradients and 3D bevels: switch them off.
        for (String k : new String[]{"Button.gradient", "ToggleButton.gradient", "CheckBox.gradient",
            "RadioButton.gradient", "ScrollBar.gradient", "Slider.gradient", "MenuBar.gradient",
            "InternalFrame.activeTitleGradient"}) {
            UIManager.put(k, null);
        }

        // 3. General surfaces and text.
        put("Panel.background", BG_WINDOW);
        put("Label.foreground", TEXT_PRIMARY);
        put("Label.disabledForeground", TEXT_DISABLED);
        put("Separator.foreground", BORDER_DIVIDER);
        put("Separator.background", BG_SURFACE);
        put("control", BG_WINDOW);
        put("controlText", TEXT_PRIMARY);
        put("text", BG_SURFACE);
        put("textText", TEXT_PRIMARY);

        // 4. Buttons (StyledButton paints itself; these keep plain JButtons tidy too).
        put("Button.background", BG_SURFACE);
        put("Button.foreground", TEXT_PRIMARY);
        put("Button.select", BG_SELECTED);
        put("Button.focus", FOCUS);
        put("Button.disabledText", TEXT_DISABLED);
        UIManager.put("Button.font", new FontUIResource(F_BUTTON));
        put("ToggleButton.background", BG_SURFACE);
        put("ToggleButton.select", PRIMARY_TINT);
        put("ToggleButton.focus", FOCUS);

        // 5. Text inputs: rounded outline, turns into a 2 px blue ring when focused.
        Border field = new RoundedBorder(R_CONTROL, BORDER_FIELD, new Insets(10, 12, 10, 12));
        for (String p : new String[]{"TextField", "PasswordField", "FormattedTextField"}) {
            put(p + ".background", BG_SURFACE);
            put(p + ".foreground", TEXT_PRIMARY);
            put(p + ".caretForeground", NAVY_900);
            put(p + ".selectionBackground", BG_SELECTED);
            put(p + ".selectionForeground", TEXT_PRIMARY);
            put(p + ".inactiveForeground", TEXT_DISABLED);
            put(p + ".inactiveBackground", BG_WINDOW);
            UIManager.put(p + ".border", new BorderUIResource(field));
            UIManager.put(p + ".margin", new InsetsUIResource(0, 0, 0, 0));
        }
        put("TextArea.background", BG_SURFACE);
        put("TextArea.foreground", TEXT_PRIMARY);
        put("TextArea.selectionBackground", BG_SELECTED);
        put("TextArea.selectionForeground", TEXT_PRIMARY);

        // 6. Combo boxes and lists.
        put("ComboBox.background", BG_SURFACE);
        put("ComboBox.foreground", TEXT_PRIMARY);
        put("ComboBox.buttonBackground", BG_SURFACE);
        put("ComboBox.buttonShadow", BG_SURFACE);
        put("ComboBox.buttonDarkShadow", TEXT_SECONDARY);
        put("ComboBox.buttonHighlight", BG_SURFACE);
        put("ComboBox.selectionBackground", BG_SELECTED);
        put("ComboBox.selectionForeground", TEXT_PRIMARY);
        put("ComboBox.disabledBackground", BG_WINDOW);
        put("ComboBox.disabledForeground", TEXT_DISABLED);
        put("List.background", BG_SURFACE);
        put("List.foreground", TEXT_PRIMARY);
        put("List.selectionBackground", BG_SELECTED);
        put("List.selectionForeground", TEXT_PRIMARY);
        UIManager.put("List.focusCellHighlightBorder", new BorderUIResource(BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        UIManager.put("PopupMenu.border", new BorderUIResource(BorderFactory.createLineBorder(BORDER_CARD, 1)));

        // 7. Tables.
        put("Table.background", BG_SURFACE);
        put("Table.foreground", TEXT_PRIMARY);
        put("Table.selectionBackground", BG_SELECTED);
        put("Table.selectionForeground", TEXT_PRIMARY);
        put("Table.gridColor", BORDER_DIVIDER);
        put("Table.focusCellBackground", BG_SELECTED);
        UIManager.put("Table.focusCellHighlightBorder", new BorderUIResource(BorderFactory.createEmptyBorder(0, 16, 0, 16)));
        UIManager.put("Table.rowHeight", ROW_H);
        UIManager.put("Table.showVerticalLines", Boolean.FALSE);
        UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 1));
        put("TableHeader.background", BG_SUBTLE);
        put("TableHeader.foreground", TEXT_SECONDARY);
        UIManager.put("TableHeader.font", new FontUIResource(F_LABEL));
        UIManager.put("TableHeader.cellBorder", new BorderUIResource(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CARD),
                BorderFactory.createEmptyBorder(0, 16, 0, 16))));

        // 8. Scroll panes and scrollbars (MinimalScrollBarUI finishes the look).
        UIManager.put("ScrollPane.border", new BorderUIResource(BorderFactory.createEmptyBorder()));
        put("ScrollPane.background", BG_SURFACE);
        put("Viewport.background", BG_SURFACE);
        UIManager.put("ScrollBar.width", 10);
        put("ScrollBar.background", BG_SURFACE);
        put("ScrollBar.track", BG_SURFACE);
        put("ScrollBar.thumb", BORDER_DASHED);
        put("ScrollBar.thumbHighlight", BORDER_DASHED);
        put("ScrollBar.thumbShadow", BORDER_DASHED);
        put("ScrollBar.thumbDarkShadow", BORDER_DASHED);

        // 9. Tabbed pane: a navy bar. Each tab paints itself (see TabTitle); these keys just
        //    remove Metal's frames and set the space around the tabs.
        put("TabbedPane.background", NAVY_900);
        put("TabbedPane.tabAreaBackground", NAVY_900);
        put("TabbedPane.foreground", TEXT_ON_NAVY);
        put("TabbedPane.selected", BG_WINDOW);
        put("TabbedPane.contentAreaColor", BG_WINDOW);
        put("TabbedPane.focus", FOCUS);
        UIManager.put("TabbedPane.font", new FontUIResource(F_LABEL));
        UIManager.put("TabbedPane.tabAreaInsets", new InsetsUIResource(10, 12, 0, 12));
        UIManager.put("TabbedPane.tabInsets", new InsetsUIResource(0, 0, 0, 0));
        UIManager.put("TabbedPane.selectedTabPadInsets", new InsetsUIResource(0, 0, 0, 0));
        UIManager.put("TabbedPane.contentBorderInsets", new InsetsUIResource(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabsOverlapBorder", Boolean.FALSE);
        UIManager.put("TabbedPane.contentOpaque", Boolean.TRUE);

        // 10. Check boxes, radio buttons, tooltips, option panes.
        put("CheckBox.background", BG_SURFACE);
        put("CheckBox.foreground", TEXT_PRIMARY);
        put("CheckBox.focus", FOCUS);
        put("RadioButton.background", BG_SURFACE);
        put("RadioButton.foreground", TEXT_PRIMARY);
        put("RadioButton.focus", FOCUS);
        put("ToolTip.background", NAVY_900);
        put("ToolTip.foreground", Color.WHITE);
        UIManager.put("ToolTip.border", new BorderUIResource(BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        UIManager.put("ToolTip.font", new FontUIResource(F_SMALL));
        put("OptionPane.background", BG_SURFACE);
        put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("OptionPane.messageFont", new FontUIResource(F_BODY));
        UIManager.put("OptionPane.buttonFont", new FontUIResource(F_BUTTON));

        // 11. Our own painters. Every JButton, JScrollBar, JComboBox and JTable created
        //     after this point is drawn by these classes, so no view needs changing.
        UIManager.put("ButtonUI", StyledButtonUI.class.getName());
        UIManager.put("ScrollBarUI", StyledScrollBarUI.class.getName());
        UIManager.put("ComboBoxUI", StyledComboBoxUI.class.getName());
        UIManager.put("TableUI", StyledTableUI.class.getName());
        UIManager.put("TabbedPaneUI", StyledTabbedPaneUI.class.getName());

        installFocusTracking();
    }

    // ---------------------------------------------------------------- focus tracking
    private static boolean keyboardFocus = false;

    /**
     * True after the user pressed Tab, false after a mouse click. Buttons only
     * show a focus ring in keyboard mode.
     */
    public static boolean isKeyboardFocus() {
        return keyboardFocus;
    }

    private static void installFocusTracking() {
        Toolkit.getDefaultToolkit().addAWTEventListener((AWTEventListener) event -> {
            if (event instanceof KeyEvent && event.getID() == KeyEvent.KEY_PRESSED
                    && ((KeyEvent) event).getKeyCode() == KeyEvent.VK_TAB) {
                keyboardFocus = true;
            } else if (event instanceof MouseEvent && event.getID() == MouseEvent.MOUSE_PRESSED) {
                keyboardFocus = false;
            }
        }, WATCHED_EVENTS);

        // Borders are only painted when their component repaints, so ask the old and new focus owner to repaint.
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("permanentFocusOwner", e -> {
            repaintWithOwner((Component) e.getOldValue());
            repaintWithOwner((Component) e.getNewValue());
        });
    }

    private static final long WATCHED_EVENTS
            = java.awt.AWTEvent.KEY_EVENT_MASK | java.awt.AWTEvent.MOUSE_EVENT_MASK;

    private static void repaintWithOwner(Component c) {
        if (c == null) {
            return;
        }
        c.repaint();
        Component combo = SwingUtilities.getAncestorOfClass(JComboBox.class, c);
        if (combo != null) {
            combo.repaint();   // an editable combo's text field sits inside the combo
        }
        Component parent = c.getParent();
        if (parent instanceof JComponent) {
            parent.repaint();
        }
    }

    private static void put(String key, Color c) {
        UIManager.put(key, new ColorUIResource(c));
    }

    /**
     * Insets helper for EmptyBorders: Theme.pad(24) etc.
     */
    public static Insets pad(int all) {
        return new Insets(all, all, all, all);
    }
}
