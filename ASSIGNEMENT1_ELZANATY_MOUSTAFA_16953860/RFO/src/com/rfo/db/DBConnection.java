package com.rfo.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

    private static final String DB_URL = "jdbc:derby:db/rfodb;create=true";
    private static DBConnection instance;
    private final Connection connection;

    private DBConnection() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            initializeSchema();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize Derby database: " + e.getMessage(), e);
        }
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void initializeSchema() throws SQLException {
        try (Statement st = connection.createStatement()) {

            createTableIfMissing(st, "LOCATIONS", """
                CREATE TABLE LOCATIONS (
                    LOCATION_ID VARCHAR(20) PRIMARY KEY,
                    NAME VARCHAR(100) NOT NULL,
                    STREET VARCHAR(100),
                    CITY VARCHAR(50),
                    COUNTRY VARCHAR(10),
                    POSTAL_CODE VARCHAR(20),
                    ACTIVE BOOLEAN NOT NULL DEFAULT TRUE
                )
                """);

            createTableIfMissing(st, "DRIVERS", """
                CREATE TABLE DRIVERS (
                    DRIVER_ID INT PRIMARY KEY,
                    FNAME VARCHAR(50),
                    LNAME VARCHAR(50),
                    LICENSE_NUMBER VARCHAR(30),
                    EMAIL VARCHAR(100),
                    PHONE_NUMBER VARCHAR(30),
                    STREET VARCHAR(100),
                    CITY VARCHAR(50),
                    COUNTRY VARCHAR(10),
                    POSTAL_CODE VARCHAR(20)
                )
                """);

            createTableIfMissing(st, "VEHICLES", """
                CREATE TABLE VEHICLES (
                    PLATE_NUMBER VARCHAR(20) PRIMARY KEY,
                    MAKE VARCHAR(50),
                    MODEL VARCHAR(50),
                    YR INT,
                    CATEGORY VARCHAR(20),
                    RATE DOUBLE,
                    HAS_GPS BOOLEAN,
                    FUEL_TYPE VARCHAR(20),
                    CURRENT_ODOMETER INT,
                    LAST_SERVICE_ODOMETER INT,
                    STATUS VARCHAR(20),
                    CURRENT_LOCATION_ID VARCHAR(20),
                    SERVICE_FLAG BOOLEAN,
                    FOREIGN KEY (CURRENT_LOCATION_ID) REFERENCES LOCATIONS(LOCATION_ID)
                )
                """);

            createTableIfMissing(st, "STAFF", """
                CREATE TABLE STAFF (
                    STAFF_ID VARCHAR(20) PRIMARY KEY,
                    PASSWORD VARCHAR(100) NOT NULL,
                    FNAME VARCHAR(50),
                    LNAME VARCHAR(50),
                    ROLE VARCHAR(20),
                    CURRENT_LOCATION_ID VARCHAR(20),
                    FOREIGN KEY (CURRENT_LOCATION_ID) REFERENCES LOCATIONS(LOCATION_ID)
                )
                """);

            createTableIfMissing(st, "STAFF_LOCATIONS", """
                CREATE TABLE STAFF_LOCATIONS (
                    STAFF_ID VARCHAR(20) NOT NULL,
                    LOCATION_ID VARCHAR(20) NOT NULL,
                    PRIMARY KEY (STAFF_ID, LOCATION_ID),
                    FOREIGN KEY (STAFF_ID) REFERENCES STAFF(STAFF_ID),
                    FOREIGN KEY (LOCATION_ID) REFERENCES LOCATIONS(LOCATION_ID)
                )
                """);

            createTableIfMissing(st, "MOVEMENTS", """
                CREATE TABLE MOVEMENTS (
                    MOVEMENT_ID VARCHAR(20) PRIMARY KEY,
                    MOVEMENT_TYPE VARCHAR(10) NOT NULL,
                    PLATE_NUMBER VARCHAR(20) NOT NULL,
                    CO_STAFF VARCHAR(20),
                    CI_STAFF VARCHAR(20),
                    CHECKOUT_DT TIMESTAMP,
                    CHECKIN_DT TIMESTAMP,
                    CHECKOUT_LOC_ID VARCHAR(20),
                    CHECKIN_LOC_ID VARCHAR(20),
                    ODO_OUT INT,
                    ODO_IN INT,
                    STATUS VARCHAR(20),
                    EXPECTED_CI_DT TIMESTAMP,
                    EXPECTED_CI_LOC_ID VARCHAR(20),
                    RSA BOOLEAN,
                    INSURANCE_TIER VARCHAR(20),
                    PRICE DOUBLE,
                    FOREIGN KEY (PLATE_NUMBER) REFERENCES VEHICLES(PLATE_NUMBER)
                )
                """);

            createTableIfMissing(st, "MOVEMENT_DRIVERS", """
                CREATE TABLE MOVEMENT_DRIVERS (
                    MOVEMENT_ID VARCHAR(20) NOT NULL,
                    DRIVER_ID INT NOT NULL,
                    PRIMARY KEY (MOVEMENT_ID, DRIVER_ID),
                    FOREIGN KEY (MOVEMENT_ID) REFERENCES MOVEMENTS(MOVEMENT_ID)
                )
                """);

            createTableIfMissing(st, "MOVEMENT_EQUIPMENT", """
                CREATE TABLE MOVEMENT_EQUIPMENT (
                    MOVEMENT_ID VARCHAR(20) NOT NULL,
                    EQUIPMENT_TYPE VARCHAR(30) NOT NULL,
                    QUANTITY INT NOT NULL,
                    PRIMARY KEY (MOVEMENT_ID, EQUIPMENT_TYPE),
                    FOREIGN KEY (MOVEMENT_ID) REFERENCES MOVEMENTS(MOVEMENT_ID)
                )
                """);

            // one saved invoice per closed rental; the values are a snapshot of what was charged
            createTableIfMissing(st, "INVOICES", """
                CREATE TABLE INVOICES (
                    INVOICE_ID INT PRIMARY KEY,
                    MOVEMENT_ID VARCHAR(20) NOT NULL UNIQUE,
                    ISSUED_AT TIMESTAMP NOT NULL,
                    PLATE VARCHAR(20),
                    VEHICLE VARCHAR(120),
                    DRIVERS VARCHAR(400),
                    CHECKOUT_TEXT VARCHAR(160),
                    CHECKIN_TEXT VARCHAR(160),
                    ODO_OUT INT,
                    ODO_IN INT,
                    DAYS INT,
                    BASE DOUBLE,
                    EQUIPMENT DOUBLE,
                    INSURANCE_TIER VARCHAR(20),
                    INSURANCE DOUBLE,
                    RSA DOUBLE,
                    TOTAL DOUBLE,
                    CO_STAFF VARCHAR(100),
                    CI_STAFF VARCHAR(100),
                    FOREIGN KEY (MOVEMENT_ID) REFERENCES MOVEMENTS(MOVEMENT_ID)
                )
                """);
        }
    }

    private void createTableIfMissing(Statement st, String tableName, String ddl) throws SQLException {
        try (ResultSet rs = connection.getMetaData().getTables(null, null, tableName, null)) {
            if (!rs.next()) {
                st.execute(ddl);
            }
        }
    }

    public void shutdown() {
        try {
            DriverManager.getConnection("jdbc:derby:db/rfodb;shutdown=true");
        } catch (SQLException e) {
            if (!"08006".equals(e.getSQLState())) {
                System.out.println("Error shutting down database: " + e.getMessage());
            }
        }
    }
}
