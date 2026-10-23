package com.rfo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * One entry in the menu tree: a label and, if it opens a sub menu, its
 * children. An entry with no children is a leaf, an action the user can open.
 */
public class MenuNode {

    private final String label;
    private final List<MenuNode> children = new ArrayList<>();

    public MenuNode(String label) {
        this.label = label;
    }

    public MenuNode add(MenuNode child) {
        children.add(child);
        return this;
    }

    public String getLabel() {
        return label;
    }

    public List<MenuNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }
}