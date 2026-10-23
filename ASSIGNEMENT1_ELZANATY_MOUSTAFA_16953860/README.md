# RFO: RentalFleetOps

A car rental fleet management system with a Swing GUI and an embedded Apache Derby
database, built for AUT COMP603  (Project 2). It builds on my Project 1
domain model and business rules.

## What this is

RFO handles the day-to-day running of a car rental business: managing vehicles,
staff, locations and drivers, and running rentals, workshop jobs, transfers and
defleets through their full lifecycle, including pricing and invoices (with PDF
export). It is a single-user Java desktop application.

## How it's put together

The code is split into layers, each doing one job:

```
com.rfo
├── com.rfo.model      domain classes - Vehicle, Driver, Staff, Location,
│                      Invoice, the Movement hierarchy, and the enums
├── com.rfo.db         DBConnection (Singleton) and DatabaseSeeder (fills an
│                      empty database from the sample data in data/)
├── com.rfo.io         the DAOs (one per entity, all the SQL), the invoice PDF
│                      writer, and the text file readers the seeder uses
├── com.rfo.service    business logic and input validation - Auth, Staff,
│                      Vehicle, Movement, Location, Driver and Invoice
│                      services, all working off one shared DataStore
└── com.rfo.gui        the Swing GUI
    ├── RFOGuiApp      entry point
    ├── controller     one controller per screen
    ├── view           the Swing screens
    └── theme          colours, fonts and shared components
```

### Design patterns used

- **MVC** - views only draw, controllers handle the user's actions and call the
  services, the model and DataStore hold the data. Screens refresh through a
  `ModelListener` when the data changes.
- **DAO** - all SQL lives in the DAO classes in `com.rfo.io`. Nothing else talks
  to the database.
- **Singleton** - `DBConnection` holds the one database connection.
- **Factory** - `ScreenFactory` builds each screen and its controller.

### A few decisions worth explaining

- **The database sits behind the DataStore.** The DataStore from Project 1 still
  holds the data in memory for the screens, but it now loads from Derby and saves
  through the DAOs instead of reading and writing text files. That meant the
  business rules did not have to change.
- **The database sets itself up.** `DBConnection` opens `db/rfodb` with
  `create=true`, and `DatabaseSeeder` fills it from the files in `data/` only when
  it is empty, so there is no manual setup. Those files are a snapshot of the
  demo data (locations, drivers, vehicles, staff and rental history).
- **Services validate, controllers show.** Business rules and input checks live in
  the service layer, which throws an `IllegalArgumentException` with a plain
  message. The controller shows that message to the user, so the screens contain
  no business logic.
- **Screens refresh themselves.** Controllers listen for model changes
  (`ModelListener`), so a screen updates when the data changes without being
  reopened.
- **Invoices are saved.** When a rental is closed, its invoice is stored in the
  database and can be viewed again or exported to PDF.

## Running it

You need NetBeans, JDK 25 and Ant. No database setup is needed.

1. Open the `RFO` project folder in NetBeans (File > Open Project).
2. Run the project (F6). The main class is `com.rfo.gui.RFOGuiApp`.
3. The first time it runs, the Derby database is created automatically in the
   `db/` folder and filled from the sample files in `data/`. After that the app
   uses the database only.
4. Log in with one of the accounts below.

The Derby driver (`derby.jar`, version 10.14.2.0) is included in `lib/`. To get a
fresh database, close the app and delete the `db/rfodb` folder; it is rebuilt on
the next run.

To run the unit tests, right-click the project and choose Test (Alt+F6). Close
the app first, because only one program can open the embedded database at a time.

### Test logins

| Staff ID  | Password      | Role            |
|-----------|---------------|-----------------|
| `ONZP774` | `test123`     | MANAGER         |
| `AGNT002` | `agent123`    | AGENT           |
| `MECH003` | `mech123`     | MECHANIC        |
| `FLTO004` | `fleet123`    | FLEET_OPERATOR  |

## Unit tests

JUnit 4 tests are in the `test` folder (15 tests):

- `RentalMovementTest` - rental price rules (days, insurance bands, RSA).
- `RentalLifecycleTest` - checking in and closing a rental on time, early and
  late, and what that does to the final price and the vehicle.
- `VehicleTest` - availability, workshop/in-use vehicles and the service-due
  rules.

## Known limitations

- Passwords are stored as plain text in the database (fine for a coursework
  demo, not for real use).
- The DAOs print database errors instead of reporting them to the user.
- The unit tests cover the model and pricing rules. The service layer and the
  database are tested by hand through the GUI.
- Changeover (swapping the vehicle mid-rental) was not built.

## AI usage

I used Claude (Anthropic) throughout this project. In Part 1 it reviewed my own
design and code and wrote some of the repeated classes and methods. In Part 2 I
used Claude Code, which had access to my project files, to help build the GUI,
the Derby layer, the tests and the AI-use comments, working from the design and
requirements I gave it. I reviewed and tested what it produced. Each source file
carries a comment saying who wrote it. A full report on the interaction log will be in
Appendix A of the report.
