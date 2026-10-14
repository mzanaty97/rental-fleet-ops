package com.rfo.gui.controller;

import com.rfo.gui.view.MenuView;
import com.rfo.gui.view.ScreenFrame;
import com.rfo.gui.view.SessionView;
import com.rfo.service.MenuNode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

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

    public NavigationController(SessionView sessionView, MenuNode root, ScreenFactory screenFactory) {
        this.sessionView = sessionView;
        this.screenFactory = screenFactory;

        addMenu(root, root.getLabel(), null);
        sessionView.showScreen(root.getLabel());
    }

    private void addMenu(MenuNode node, String key, String parentKey) {
        List<String> labels = new ArrayList<>();
        for (MenuNode child : node.getChildren()) {
            labels.add(child.getLabel());
        }

        MenuView menu = new MenuView(node.getLabel(), labels, parentKey != null);
        menu.addController(e -> {
            String command = e.getActionCommand();
            sessionView.showScreen(MenuView.BACK.equals(command) ? parentKey : key + "/" + command);
        });
        sessionView.addScreen(key, menu);

        for (MenuNode child : node.getChildren()) {
            String childKey = key + "/" + child.getLabel();
            if (child.isLeaf()) {
                addFeature(child.getLabel(), childKey, key);
            } else {
                addMenu(child, childKey, key);
            }
        }
    }

    private void addFeature(String option, String key, String parentKey) {
        JComponent content = screenFactory.createScreen(option, sessionView);
        ScreenFrame frame = new ScreenFrame(option, content);
        frame.addController(e -> sessionView.showScreen(parentKey));
        sessionView.addScreen(key, frame);
    }
}
