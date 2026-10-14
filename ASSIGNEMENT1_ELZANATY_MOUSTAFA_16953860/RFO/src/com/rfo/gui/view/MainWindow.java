package com.rfo.gui.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 * The single application window. Home is always the first tab; sessions open as
 * extra tabs.
 */
public class MainWindow extends JFrame {

    private final JTabbedPane tabs = new JTabbedPane();
    private final HomeView homeView = new HomeView();

    public MainWindow() {
        super("RFO - RentalFleetOps");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(700, 500));
        setSize(1000, 700);
        setLocationRelativeTo(null);

        tabs.addTab("Home", homeView);
        tabs.setTabComponentAt(0, new TabTitle("Home", null));
        add(tabs);
    }

    public HomeView getHomeView() {
        return homeView;
    }

    public void addTab(String title, Component content, ActionListener closeAction) {
        tabs.addTab(title, content);
        tabs.setTabComponentAt(tabs.indexOfComponent(content), new TabTitle(title, closeAction));
        tabs.setSelectedComponent(content);
    }

    // swaps the content of an existing tab (login form -> session) and renames it
    public void replaceTab(Component oldContent, Component newContent, String title, ActionListener closeAction) {
        int index = tabs.indexOfComponent(oldContent);
        tabs.setComponentAt(index, newContent);
        tabs.setTabComponentAt(index, new TabTitle(title, closeAction));
        tabs.setSelectedIndex(index);
    }

    public void removeTab(Component content) {
        tabs.remove(content);
    }
}
