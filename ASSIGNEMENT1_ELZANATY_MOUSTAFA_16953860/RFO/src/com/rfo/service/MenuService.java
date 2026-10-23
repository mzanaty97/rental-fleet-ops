package com.rfo.service;

import com.rfo.model.StaffRole;
import java.util.List;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * Builds the menu tree a staff role is allowed to see. It follows the same
 * menus as the console version, so the GUI and the CUI offer the same options
 * and the role rules stay in StaffService.
 */
public class MenuService {

    private final StaffService staffService;

    public MenuService(StaffService staffService) {
        this.staffService = staffService;
    }

    public MenuNode getMenuFor(StaffRole role) {
        MenuNode root = new MenuNode("Main Menu");
        for (String option : staffService.getMenuOptions(role)) {
            root.add(buildSection(option, role));
        }
        return root;
    }

    private MenuNode buildSection(String option, StaffRole role) {
        switch (option) {
            case "Admin":
                return withLeaves(option, List.of("Change Location", "Change Password"));
            case "Movements":
                MenuNode movements = new MenuNode(option);
                addIfNotEmpty(movements, "Revenue Movements", staffService.getRevenueMovementsOptions(role));
                addIfNotEmpty(movements, "Non-Revenue Movements", staffService.getNonRevenueMovementsOptions(role));
                addIfNotEmpty(movements, "Movement Details", staffService.getMovementDetailsOptions(role));
                return movements;
            case "Fleet":
                return withLeaves(option, staffService.getFleetOptions(role));
            case "Reports":
                return withLeaves(option, staffService.getReportsOptions(role));
            case "Location Manager":
                return new MenuNode(option)
                        .add(withLeaves("Manage Staff", List.of("View Staff", "Add Staff",
                                "Grant Location Access", "Revoke Location Access")))
                        .add(withLeaves("Manage Locations", List.of("View Locations", "Add Location",
                                "Close Location")));
            default:
                return new MenuNode(option);
        }
    }

    private MenuNode withLeaves(String label, List<String> options) {
        MenuNode node = new MenuNode(label);
        for (String option : options) {
            node.add(new MenuNode(option));
        }
        return node;
    }

    private void addIfNotEmpty(MenuNode parent, String label, List<String> options) {
        if (!options.isEmpty()) {
            parent.add(withLeaves(label, options));
        }
    }
}
