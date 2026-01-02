package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    private static final String URL =
            "jdbc:postgresql://localhost:5432/irctc";
    private static final String USER = "postgres";
    private static final String PASSWORD = "root";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            throw new RuntimeException("DB connection failed", e);
        }
    }
}
