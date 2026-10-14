package com.rfo.gui.theme;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * Tab bar for the window. This class draws nothing for the tabs themselves:
 * every tab paints its own background through its custom tab component
 * (TabTitle). What it does is switch off Swing's default tab frame, remove the
 * content border, and keep track of which tab the mouse is over so that
 * TabTitle can draw its hover colour.
 */
public class StyledTabbedPaneUI extends BasicTabbedPaneUI {

    private int hoverTab = -1;
    private MouseAdapter hoverTracker;

    public static ComponentUI createUI(JComponent c) {
        return new StyledTabbedPaneUI();
    }

    /**
     * Index of the tab under the mouse, or -1.
     */
    public int getHoverTab() {
        return hoverTab;
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        hoverTracker = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                setHover(tabForCoordinate(tabPane, e.getX(), e.getY()));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setHover(-1);
            }
        };
        tabPane.addMouseMotionListener(hoverTracker);
        tabPane.addMouseListener(hoverTracker);
    }

    @Override
    protected void uninstallListeners() {
        tabPane.removeMouseMotionListener(hoverTracker);
        tabPane.removeMouseListener(hoverTracker);
        super.uninstallListeners();
    }

    private void setHover(int index) {
        if (index != hoverTab) {
            hoverTab = index;
            tabPane.repaint();
        }
    }

    /**
     * Paints the navy strip behind the tabs, then lets the tabs draw themselves
     * on top.
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        g.setColor(Theme.NAVY_900);
        g.fillRect(0, 0, c.getWidth(), Theme.TABBAR_H);
        super.paint(g, c);
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
            Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    }

    @Override
    protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;
    }

    @Override
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;
    }
}
