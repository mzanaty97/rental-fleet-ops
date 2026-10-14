package com.rfo.gui.controller;

import com.rfo.gui.view.AddLocationView;
import com.rfo.gui.view.AddStaffView;
import com.rfo.gui.view.ChangeLocationView;
import com.rfo.gui.view.CheckInView;
import com.rfo.gui.view.CheckoutView;
import com.rfo.gui.view.CloseRentalView;
import com.rfo.gui.view.ChangePasswordView;
import com.rfo.gui.view.CloseLocationView;
import com.rfo.gui.view.DefleetView;
import com.rfo.gui.view.FleetView;
import com.rfo.gui.view.GrantAccessView;
import com.rfo.gui.view.LocationsView;
import com.rfo.gui.view.NewVehicleView;
import com.rfo.gui.view.PlaceholderView;
import com.rfo.gui.view.RevokeAccessView;
import com.rfo.gui.view.SessionView;
import com.rfo.gui.view.StaffView;
import com.rfo.gui.view.TransferView;
import com.rfo.model.Staff;
import com.rfo.service.LocationService;
import com.rfo.service.MovementService;
import com.rfo.service.StaffService;
import com.rfo.service.VehicleService;
import javax.swing.JComponent;

/**
 * Factory for feature screens. Given the name of a menu option it builds the
 * matching screen together with its controller. Options that are not built yet
 * get a placeholder, so adding a section later means adding one case here.
 */
public class ScreenFactory {

    private final VehicleService vehicleService;
    private final LocationService locationService;
    private final StaffService staffService;
    private final MovementService movementService;

    public ScreenFactory(VehicleService vehicleService, LocationService locationService, StaffService staffService,
            MovementService movementService) {
        this.vehicleService = vehicleService;
        this.locationService = locationService;
        this.staffService = staffService;
        this.movementService = movementService;
    }

    public JComponent createScreen(String option, SessionView sessionView, Staff staff) {
        switch (option) {
            case "Query Available Vehicles":
                return fleetScreen(new FleetView(FleetView.Mode.AVAILABLE, true, false), sessionView);
            case "Query Service Due Vehicles":
                return fleetScreen(new FleetView(FleetView.Mode.SERVICE_DUE, false, false), sessionView);
            case "Update Vehicle Status":
                return fleetScreen(new FleetView(FleetView.Mode.ALL, true, true), sessionView);
            case "Change Location":
                ChangeLocationView changeLocation = new ChangeLocationView();
                sessionView.addModelListener(new ChangeLocationController(changeLocation, staffService, staff, sessionView));
                return changeLocation;
            case "Change Password":
                ChangePasswordView changePassword = new ChangePasswordView();
                new ChangePasswordController(changePassword, staffService, staff, sessionView.getConsole());
                return changePassword;
            case "Checkout":
                CheckoutView checkout = new CheckoutView();
                sessionView.addModelListener(new CheckoutController(checkout, movementService, locationService,
                        staff, sessionView.getConsole()));
                return checkout;
            case "Check In":
                CheckInView checkIn = new CheckInView();
                new CheckInController(checkIn, movementService, staff, sessionView.getConsole());
                return checkIn;
            case "Close Rental":
                CloseRentalView closeRental = new CloseRentalView();
                new CloseRentalController(closeRental, movementService, sessionView.getConsole());
                return closeRental;
            case "New Vehicle":
                NewVehicleView newVehicle = new NewVehicleView();
                new NewVehicleController(newVehicle, vehicleService, staff, sessionView.getConsole());
                return newVehicle;
            case "Car Transfer":
                TransferView transfer = new TransferView(TransferView.Kind.TRANSFER);
                sessionView.addModelListener(new TransferController(transfer, TransferView.Kind.TRANSFER,
                        movementService, locationService, staff, sessionView.getConsole()));
                return transfer;
            case "Workshop":
                TransferView workshop = new TransferView(TransferView.Kind.WORKSHOP);
                sessionView.addModelListener(new TransferController(workshop, TransferView.Kind.WORKSHOP,
                        movementService, locationService, staff, sessionView.getConsole()));
                return workshop;
            case "Defleet":
                DefleetView defleet = new DefleetView();
                new DefleetController(defleet, vehicleService, staff, sessionView.getConsole());
                return defleet;
            case "View Staff":
                StaffView staffList = new StaffView();
                sessionView.addModelListener(new StaffListController(staffList, staffService));
                return staffList;
            case "Add Staff":
                AddStaffView addStaff = new AddStaffView();
                new AddStaffController(addStaff, staffService, sessionView.getConsole());
                return addStaff;
            case "Grant Location Access":
                GrantAccessView grant = new GrantAccessView();
                sessionView.addModelListener(new GrantAccessController(grant, staffService, locationService,
                        sessionView.getConsole()));
                return grant;
            case "Revoke Location Access":
                RevokeAccessView revoke = new RevokeAccessView();
                sessionView.addModelListener(new RevokeAccessController(revoke, staffService,
                        sessionView.getConsole()));
                return revoke;
            case "View Locations":
                LocationsView locations = new LocationsView();
                sessionView.addModelListener(new LocationsController(locations, locationService));
                return locations;
            case "Add Location":
                AddLocationView form = new AddLocationView();
                new AddLocationController(form, locationService, sessionView.getConsole());
                return form;
            case "Close Location":
                CloseLocationView close = new CloseLocationView();
                sessionView.addModelListener(new CloseLocationController(close, locationService,
                        sessionView.getConsole(), staff.getStaffID()));
                return close;
            default:
                return new PlaceholderView(option);
        }
    }

    // the controller listens to the model through the session, so the table refreshes itself
    private JComponent fleetScreen(FleetView view, SessionView sessionView) {
        sessionView.addModelListener(new FleetController(view, vehicleService, sessionView.getConsole()));
        return view;
    }
}
