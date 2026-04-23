package ru.mephi.case2.db.config;

import ru.mephi.case2.log.BackendLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Postgresql {

    private static final String URL = "jdbc:postgresql://localhost:5432/test";
    private static final String USER = "test";
    private static final String PASSWORD = "test";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            BackendLogger.log("Failed to connect to DB: " + URL + "\n" + e.getMessage());
            throw new RuntimeException();
        }
    }
}
