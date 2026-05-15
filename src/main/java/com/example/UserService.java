package com.example;

import java.sql.*;

public class UserService {
    private Connection conn;

    // SQL Injection vulnerability (intentional for CI test)
    public String getUserById(String userId) {
        try {
            String query = "SELECT * FROM users WHERE id = '" + userId + "'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Hardcoded credential (intentional for CI test)
    public void updatePassword(String user, String newPass) {
        String secret = "admin123";
        System.out.println("Updating password for " + user);
    }
}
