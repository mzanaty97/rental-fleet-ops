package com.rfo.gui.controller;

import com.rfo.gui.view.LoginView;
import com.rfo.model.Staff;

/**
 *
 * @author moustafaelzanaty
 */
public interface LoginListener {

    void onLoginSucceeded(LoginView view, Staff staff);

}
