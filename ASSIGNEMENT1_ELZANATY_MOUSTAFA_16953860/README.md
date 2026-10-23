# RFO: RentalFleetOps

A command-line car rental fleet management system, built for AUT COMP603
(Program Design and Construction).

## What this is

RFO handles the day-to-day running of a car rental business from a terminal:
managing vehicles, staff, locations and drivers, and running rentals, workshop
jobs, transfers and defleets through their full lifecycle. It's a single-user
Java application that reads and writes its data to plain text files - no
database, no GUI, just a menu-driven console app.

## How it's put together

The code is split into four layers, each doing one job:

```
com.rfo
├── RFOApp.java          entry point
├── com.rfo.model        domain classes - Vehicle, Driver, Staff, Location,
│                        the Movement hierarchy, and the enums. Model classes
│                        don't validate anything themselves; they trust the
│                        data they're handed.
├── com.rfo.io           file handlers, one per entity. Each one just reads
│                        and writes its own pipe-delimited text file.
├── com.rfo.service      where the actual business logic lives - Auth, Staff,
│                        Vehicle, Movement and Location services, all working
│                        off one shared DataStore.
└── com.rfo.ui           the menus. An abstract Menu base class plus a tree
                         of concrete menu classes matching how the app is
                         navigated.
```

### A few decisions worth explaining

- **There's only ever one DataStore.** It gets created once in RFOApp and
  passed by reference into everything else that needs it. No class creates
  its own separate copy.
- **Staff/driver references are IDs, not objects.** A Movement stores its
  checkout/checkin staff as plain Strings and its drivers as a list of ints,
  not full Staff or Driver objects. Same idea as a foreign key in a database -
  keeps movement records light and avoids holding onto live references they
  don't need.
- **Passwords never sit in memory as part of a Staff object.** Login checks
  the password straight off disk every time, and saving staff data reads the
  existing password first and writes it back untouched, rather than needing
  it held anywhere in the running program.
- **Checking in and closing a movement are two separate steps.** Checking in
  just stops the clock and records who/where/when. The vehicle itself - its
  odometer, its status - only actually updates once the movement is closed,
  since that's when the real numbers are known.
- **Menus are filtered by role**, not just at the top level but all the way
  down, so a Mechanic and a Manager see genuinely different option sets
  wherever they are in the menu tree.

## Running it

You'll need NetBeans (built on NetBeans 30), JDK 25, and Ant.

1. Open the project in NetBeans.
2. Make sure `com.rfo.RFOApp` is set as the Main Class (Project Properties →
   Run).
3. There should be a `data/` folder sitting next to `build.xml` and `src/`,
   with `locations.txt`, `drivers.txt`, `staff.txt`, `vehicles.txt` and
   `movements.txt` inside it. If there's no `data/` folder at all, the app
   still runs, it just starts with nothing loaded.
4. Run it. You'll be asked to log in.

### Test logins

| Staff ID  | Password      | Role            |
|-----------|---------------|-----------------|
| `ONZP774` | `Rana1251997` | MANAGER         |
| `AGNT002` | `agent123`    | AGENT           |
| `MECH003` | `mech123`     | MECHANIC        |
| `FLTO004` | `fleet123`    | FLEET_OPERATOR  |

## Getting around the menus

Everything's numbered - type the number and hit Enter. Every sub-menu has a
"Previous Menu" option to step back; the Main Menu has "Logout", which ends
the program.

During some of the longer flows (starting a rental, for example) you can type
`CANCEL` at pretty much any prompt to back out, or `LIST` where it's offered
to see the available locations.

```
Main Menu
├── Admin                    Change Location, Change Password
├── Movements
│   ├── Revenue Movements    Checkout, Check In, Close Rental
│   ├── Non-Revenue Movements  New Vehicle, Car Transfer, Workshop, Defleet
│   └── Movement Details     Query Movement Details, Query Drivers
├── Fleet                    Query Available/Service Due Vehicles, Update Status
├── Reports                  Out on Rental, Location Fleet, New Vehicles,
│                            Staff Rental Reports
└── Location Manager
    ├── Manage Staff         View/Add Staff, Grant/Revoke Location Access
    └── Manage Locations     View/Add Locations, Close Location
```

What you actually see depends on your role - not everyone gets every option.

## What's not finished / known issues

- **Data files aren't checked for corruption before loading.** If a
  `data/*.txt` file has a blank or malformed line in it (say, a stray empty
  line left over from editing it by hand), the app will crash on startup with
  an ArrayIndexOutOfBoundsException, since all the data gets loaded straight
  away when DataStore is created. I found this while testing and know how to
  fix it, but ran out of time to actually put the fix in before submitting.
  If you hit this, check whichever file is named in the stack trace for a
  blank line and delete it.
- **You can't mask the password field when running from NetBeans.** This
  isn't something I can fix in code - NetBeans' output console isn't a real
  terminal, so the usual Java mechanism for hiding typed input
  (`System.console()`) just returns null there. It would work if you ran the
  built jar from an actual terminal window.
- **NewCarMovement's intake depot and DefleetMovement's retail depot are
  hardcoded Location objects**, separate from whatever's actually loaded into
  DataStore. If a location with a matching ID ever gets closed through
  Location Manager, these two wouldn't know about it.
- **Amend Rental, Changeover and Print Invoice aren't built** - they're
  stubbed with a "coming soon" message. Changeover specifically (swapping the
  vehicle mid-rental without ending the agreement) is a real feature idea I
  didn't have time to build, not the same thing as a car transfer.
- **Idle-timeout auto-logout and any kind of thread-safety on DataStore** were
  both left out on purpose. Neither makes much sense for a single-threaded
  console app, and both would be much easier to do properly if this ever
  became a GUI application instead.

## AI usage

I used Claude quite frequently throughout this project. Both for reviewing
design decisions and code review that I wrote myself, as well as recreating
bulk classes that have the same or close logic and parameters. Claude Code
was used to create and perform smoke tests and check for redundancies and
inconsistencies after every major milestone. AI was never allowed or given
access to directly change in the codebase - it acted only merely as a
guidance and reviewing tool.

I also asked Claude to keep a log of the chat and create an easy to read log
in case the work needed to be reviewed. I also used it to help maintain a
good scheduled git usage.
