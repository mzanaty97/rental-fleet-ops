package com.rfo;

import com.rfo.model.Staff;
import com.rfo.service.AuthService;
import com.rfo.service.DataStore;
import com.rfo.ui.LoginMenu;
import com.rfo.ui.MainMenu;
import java.util.Scanner;

public class RFOApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        DataStore dataStore = new DataStore();
        AuthService service = new AuthService(dataStore);

        LoginMenu loginMenu = new LoginMenu(service, scanner);
        Staff staff = loginMenu.run();

        if (staff == null) {
            return;
        }

        new MainMenu(scanner, staff, dataStore).run();

        System.out.println("Good bye!");
        scanner.close();
    }
}
