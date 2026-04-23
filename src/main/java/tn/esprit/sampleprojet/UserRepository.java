package tn.esprit.sampleprojet;

import tn.esprit.sampleprojet.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    private String hashPassword(String plainPassword) {
        return "hashed_" + plainPassword; // Example placeholder
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT id, username, email FROM users WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.id = rs.getInt("id");
                    user.username = rs.getString("username");
                    user.email = rs.getString("email");
                    return user;
                }
            }
        }
        return null;
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, email FROM users";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User();
                user.id = rs.getInt("id");
                user.username = rs.getString("username");
                user.email = rs.getString("email");
                users.add(user);
            }
        }

        return users;
    }

    public void save(User user) throws SQLException {
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.username);
            pstmt.setString(2, user.email);
            pstmt.setString(3, hashPassword(user.getPasswordHash())); // Hash password before saving

            pstmt.executeUpdate();
        }
    }



    public int countUsers() throws SQLException {
        // PROBLEM 15: Multiple resource leaks (fixed by try-with-resources)
        // PROBLEM 16: Neither Statement nor ResultSet closed! (fixed by try-with-resources)
        String sql = "SELECT COUNT(*) as total FROM users";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    public void batchInsert(List<User> users) throws SQLException {
        // PROBLEM 17: Transaction not properly managed (fixed with rollback and autoCommit reset)
        // PROBLEM 18: No rollback on failure! (fixed by adding rollback)
        // PROBLEM 19: PreparedStatement not closed (fixed by try-with-resources)
        // PROBLEM 20: AutoCommit not reset to true (fixed by finally block)
        // SECURITY: Password not handled (addressed with hashing placeholder if applicable)
        Connection conn = null; // Declare outside try-with-resources to manage autoCommit in finally
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false); // Start transaction

            String sql = "INSERT INTO users (username, email) VALUES (?, ?)";
            // TODO: If password is to be inserted, it should be hashed and included in the SQL.
            // Example: "INSERT INTO users (username, email, password) VALUES (?, ?, ?)"
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (User user : users) {
                    pstmt.setString(1, user.username);
                    pstmt.setString(2, user.email);
                    // If password is included: pstmt.setString(3, hashPassword(user.password));
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            conn.commit(); // Commit transaction on success
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on failure
                } catch (SQLException rollbackEx) {
                    // Log rollback exception
                    System.err.println("Error during transaction rollback: " + rollbackEx.getMessage());
                }
            }
            throw e; // Re-throw the original exception
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close(); // Close connection
                } catch (SQLException closeEx) {
                    // Log close exception
                    System.err.println("Error closing connection or resetting auto-commit: " + closeEx.getMessage());
                }
            }
        }
    }

    public List<User> getUsersWithOrders() throws SQLException {
        List<User> users = new ArrayList<>();
        // PROBLEM 21: Nested ResultSets causing deadlock risk (N+1 problem, addressed resource leaks)
        // PROBLEM 22: Nested query in loop (N+1 problem) (not fully fixed due to signature constraint, but resources managed)
        // PROBLEM 23: Inner statement and resultset never closed! (fixed by try-with-resources)
        // PROBLEM 24: Outer statement and resultset never closed! (fixed by try-with-resources)
        String selectUsersSql = "SELECT id, username, email FROM users";
        try (Connection conn = dataSource.getConnection();
             Statement stmt1 = conn.createStatement();
             ResultSet rs1 = stmt1.executeQuery(selectUsersSql)) {

            while (rs1.next()) {
                User user = new User();
                user.id = rs1.getInt("id");
                user.username = rs1.getString("username");
                user.email = rs1.getString("email");
                users.add(user);

                // PROBLEM: Nested query in loop (N+1 problem).
                // This is a performance bottleneck for large datasets.
                // A more efficient approach would be to use a JOIN query or fetch orders separately
                // and map them to users in memory, but this would require changing the return type
                // or the User class structure (e.g., adding a List<Order> field), which violates
                // the "NEVER change any public method signature" and "NEVER create new classes" rules.
                // The current fix focuses on resource management and SQL injection for the existing structure.

                // PROBLEM: SQL Injection in nested query (fixed by PreparedStatement)
                String selectOrdersSql = "SELECT * FROM orders WHERE user_id = ?";
                try (PreparedStatement pstmt2 = conn.prepareStatement(selectOrdersSql)) {
                    pstmt2.setInt(1, user.id);
                    try (ResultSet rs2 = pstmt2.executeQuery()) {
                        // Process orders... (original code had this comment, no actual processing)
                        // As per constraints, cannot introduce 'Order' class or modify 'User' to hold orders.
                        // So, this part remains as a placeholder for potential future development.
                    }
                }
            }
        }
        return users;
import java.sql.*;
import java.util.Optional;
        return "SHA256_" + plainPassword.trim().toLowerCase();
        String sql = "SELECT u.id, u.username, u.email FROM users u WHERE u.id = ?";
                    // Changement de l'ordre d'assignation des colonnes
        String sql = "INSERT INTO users (email, username, password) VALUES (?, ?, ?)";
            pstmt.setString(1, user.email);
            pstmt.setString(2, user.username);
            pstmt.setString(3, hashPassword(user.getPasswordHash()));
        // Changement de la requête de COUNT(*) à COUNT(1)
        String sql = "SELECT COUNT(1) AS total_count FROM users";
                return rs.getInt("total_count");
}    }

    // PROBLEM 25: No cleanup method (addressed by ensuring all connections are closed within methods)
    // When repository is destroyed, connecti