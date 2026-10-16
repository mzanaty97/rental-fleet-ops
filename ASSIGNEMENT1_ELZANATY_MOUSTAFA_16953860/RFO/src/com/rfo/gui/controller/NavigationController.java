package com.rfo.gui.controller;

import com.rfo.gui.view.MenuView;
import com.rfo.gui.view.ScreenFrame;
import com.rfo.gui.view.ScrollHost;
import com.rfo.gui.view.SessionView;
import com.rfo.model.Staff;
import com.rfo.service.MenuNode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * Walks a session's menu tree. For every menu entry it adds a menu screen to
 * the session, for every leaf a feature screen (built by the ScreenFactory)
 * inside a frame with a Back button, and wires every button so the session
 * shows the right screen. Screens are keyed by their path, for example "Main
 * Menu/Fleet/Update Vehicle Status".
 */
public class NavigationController {

    private final SessionView sessionView;
    private final ScreenFactory screenFactory;
    private final Staff staff;

    public NavigationController(SessionView sessionView, MenuNode root, ScreenFactory screenFactory, Staff staff) {
        this.sessionView = sessionView;
        this.screenFactory = screenFactory;
        this.staff = staff;

        addMenu(root, root.getLabel(), null);
        sessionView.showScreen(root.getLabel());
    }

    private void addMenu(MenuNode node, String key, String parentKey) {
        List<String> labels = new ArrayList<>();
        for (MenuNode child : node.getChildren()) {
            labels.add(child.getLabel());
        }

        boolean isMain = parentKey == null;
        MenuView menu = new MenuView(isMain ? "What would you like to do?" : node.getLabel(), crumbs(key, true), labels, !isMain);
        menu.setMessageSink(sessionView.getConsole());
        menu.addController(e -> {
            String command = e.getActionCommand();
            sessionView.showScreen(MenuView.BACK.equals(command) ? parentKey : key + "/" + command);
        });
        // a menu scrolls when the window is too short to show every option
        sessionView.addScreen(key, ScrollHost.wrap(menu, false));

        for (MenuNode child : node.getChildren()) {
            String childKey = key + "/" + child.getLabel();
            if (child.isLeaf()) {
                addFeature(child.getLabel(), childKey, key);
            } else {
                addMenu(child, childKey, key);
            }
        }
    }

    /**
     * The path shown above a title: "Main menu › Fleet". The main menu itself
     * is just "Main menu".
     */
    private static String crumbs(String key, boolean includeMain) {
        String[] parts = key.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i == 0 && !includeMain) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(" \u203A ");
            }
            sb.append(i == 0 ? "Main menu" : parts[i]);
        }
        return sb.toString();
    }

    private void addFeature(String option, String key, String parentKey) {
        JComponent content = screenFactory.createScreen(option, sessionView, staff);
        ScreenFrame frame = new ScreenFrame(option, crumbs(parentKey, false), content);
        frame.addController(e -> sessionView.showScreen(parentKey));
        sessionView.addScreen(key, frame);
    }
}
