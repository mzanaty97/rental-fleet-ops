package com.rfo.service;

import com.rfo.model.Staff;

public class AuthService {

    private DataStore dataStore;

    public AuthService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public Staff login(String staffId, String password) {
        boolean valid = dataStore.authenticate(staffId, password);

        if (!valid) {
            System.out.println("Login failed!");
            return null;
        }

        return dataStore.getBy(dataStore.getStaff(), s -> s.getStaffID().equals(staffId));
    }
}
