package ru.mephi.case2.db.config;

import ru.mephi.case2.log.BackendLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Postgresql {

    private static final String URL = envOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/test");
    private static final String USER = envOrDefault("DB_USERNAME", "test");
    private static final String PASSWORD = envOrDefault("DB_PASSWORD", "test");

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            BackendLogger.log("Failed to connect to DB: " + URL + "\n" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static String envOrDefault(String envName, String defaultValue) {
        String value = System.getenv(envName);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}
