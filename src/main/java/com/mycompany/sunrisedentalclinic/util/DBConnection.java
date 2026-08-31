package com.mycompany.sunrisedentalclinic.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    private static final String URL
            = "jdbc:mysql://localhost:8889/sunrise_dental_clinic_2?useSSL=false&serverTimezone=Asia/Colombo";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
