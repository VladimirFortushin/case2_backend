package ru.mephi.case2.db.config;

import ru.mephi.case2.log.BackendLogger;
import ru.mephi.case2.util.ApiConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Postgresql {

    private static final String URL = ApiConfig.getDbUrl();
    private static final String USER = ApiConfig.getDbUserName();
    private static final String PASSWORD = ApiConfig.getDbPassword();

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            BackendLogger.log("Failed to connect to DB: " + URL + "\n" + e.getMessage());
            throw new RuntimeException();
        }
    }
}
