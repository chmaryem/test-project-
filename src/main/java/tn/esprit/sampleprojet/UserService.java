package tn.esprit.sampleprojet;

import tn.esprit.sampleprojet.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.logging.Level; // Added for logging
import java.util.logging.Logger; // Added for logging


public class UserService {



    public User findByUsername(String username) throws SQLException {
        String query = "SELECT id, username, email FROM users WHERE username = ?"; // Do not select password
        try (Connection conn = dataSource.getConnection(); // Get connection from pool
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) { // Use try-with-resources for ResultSet
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


    public boolean authenticate(String username, String password) throws SQLException {
        String hashedPasswordInput = hashPasswordPlaceholder(password);

        String query = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPasswordHash = rs.getString("password_hash");
                    return hashedPasswordInput.equals(storedPasswordHash);
                }
            }
        }
        return false;
    }
    public User createUser(String username, String email, String password, String role) throws SQLException {
        String hashedPassword = hashPasswordPlaceholder(password);
        String checkQuery = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, username);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalStateException("User already exists");
                }
            }
        }

        String insertQuery = "INSERT INTO users (username, email, password_hash, role) VALUES (?, ?, ?, ?)"; // Use password_hash column
        try (Connection conn = dataSource.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            insertStmt.setString(1, username);
            insertStmt.setString(2, email);
            insertStmt.setString(3, hashedPassword);
            insertStmt.setString(4, role);
            insertStmt.executeUpdate();
        }

        return findByUsername(username);
    }
    public void updateUserStatus(int userId, boolean isActive) throws SQLException {
        String query = "UPDATE users SET is_active = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, isActive); // Use setBoolean for boolean values
            stmt.setInt(2, userId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                // Optionally, throw an exception or log if no user was found/updated
                LOGGER.log(Level.WARNING, "No user found with ID {0} to update status.", userId); // Replaced System.out.println with logger
            }
        }
    }
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();

        String query = "SELECT id, username, email FROM users"; // Do not select password_hash
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {

                User user = new User();
                user.id = rs.getInt("id");
                user.username = rs.getString("username");
                user.email = rs.getString("email");
                // user.password is not set as it's sensitive and not retrieved
                users.add(user);
            }
        }
        return users;
    }

    private String hashPasswordPlaceholder(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) { // Fixed truncated line
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString(); // Fixed missing return
        } catch (NoSuchAlgorithmException e) {
            // Log the error properly instead of just printing stack trace
            LOGGER.log(Level.SEVERE, "SHA-256 algorithm not found for password hashing", e); // Replaced e.printStackTrace() with logger
            throw new RuntimeException("SHA-256 algorithm not found", e); // Rethrow as unchecked exception
        }
    }


}