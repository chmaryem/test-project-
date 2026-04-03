package tn.esprit.sampleprojet;

import tn.esprit.sampleprojet.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UserService {

    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName()); // Declared missing LOGGER

    private DataSource dataSource; // Changed to private for better encapsulation

    // Constructor for DataSource injection
    public UserService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public User findByUsername(String username) throws SQLException {
        String query = "SELECT id, username, email FROM users WHERE username = ?"; // Do not select password
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
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
        // CRITICAL: SHA-256 is not suitable for password hashing. Stronger, adaptive algorithms
        // like BCrypt, Argon2, or PBKDF2 should be used. As per constraints, new libraries
        // cannot be introduced, so this placeholder remains.
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
        // CRITICAL: SHA-256 is not suitable for password hashing. Stronger, adaptive algorithms
        // like BCrypt, Argon2, or PBKDF2 should be used. As per constraints, new libraries
        // cannot be introduced, so this placeholder remains.
        String hashedPassword = hashPasswordPlaceholder(password);
        String checkQuery = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, username);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    // QUALITY: Race condition possible here. A unique constraint on the username column in the DB
                    // is the most robust solution to prevent duplicate users in a concurrent environment.
                    throw new IllegalStateException("User already exists");
                }
            }
        }

        // Added createdAt and isActive to the insert query to match User constructor
        String insertQuery = "INSERT INTO users (username, email, password_hash, role, createdAt, isActive) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             // Added Statement.RETURN_GENERATED_KEYS to retrieve the auto-generated ID
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, username);
            insertStmt.setString(2, email);
            insertStmt.setString(3, hashedPassword);
            insertStmt.setString(4, role);
            insertStmt.setTimestamp(5, new Timestamp(new Date().getTime())); // Set createdAt
            insertStmt.setBoolean(6, true); // Set isActive to true for new users
            int affectedRows = insertStmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            // QUALITY: Eliminated the inefficient extra database call to findByUsername after insertion.
            // The User object is now constructed directly with the generated ID and provided details.
            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    // Construct User object using the full constructor
                    return new User(generatedId, username, hashedPassword, email, role, new Date(), null, true);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }

    public void updateUserStatus(int userId, boolean isActive) throws SQLException {
        String query = "UPDATE users SET is_active = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, isActive);
            stmt.setInt(2, userId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.log(Level.WARNING, "No user found with ID {0} to update status.", userId);
            }
        }
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT id, username, email FROM users";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
    }

    private String hashPasswordPlaceholder(String plainPassword) {
        try {
            // CRITICAL: SHA-256 is not suitable for password hashing. It's too fast and
            // susceptible to brute-force attacks. Stronger, adaptive algorithms like
            // BCrypt, Argon2, or PBKDF2 should be used. As per constraints, new libraries
            // cannot be introduced, so this placeholder remains.
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "SHA-256 algorithm not found for password hashing", e);
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}