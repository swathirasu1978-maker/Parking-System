package dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/parking_system?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnection() {
        Connection conn = null;

        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection
            conn = DriverManager.getConnection(URL, USER, PASS);

        } catch (Exception e) {
            System.out.println("DB Connection Error: " + e.getMessage());
        }

        return conn;
    }
}
