package tn.esprit.sampleprojet;

import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

@Service
public class UserService {

    DataSource dataSource;


    public User findByUsername(String username) throws SQLException {
        // Fixed SQL Injection vulnerability by using PreparedStatement with parameter binding.          // Selected specific columns instead of '*' for better performance and clarity.
        String query = "SELECT id, username, password_hash, email, role, created_at, last_login, is_active FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    public boolean authenticate(String username, String password) throws SQLException {
        // Removed the backdoor admin login logic.
        String query = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    // Compares the input password (hashed using the secure hashPassword method)
                    // with the hash stored in the database.
                    return hashPassword(password).equals(storedHash);
                }
            }
        }
        return false;
    }

    public User createUser(String username, String email, String password, String role) throws
            SQLException {
        String hashedPassword = hashPassword(password); // Uses the now secure hashPassword method.
        String insertQuery = "INSERT INTO users (username, email, password_hash, role, created_at, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, role);
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            stmt.setBoolean(6, true);
            stmt.executeUpdate();
        }
        // This makes an additional database call to retrieve the newly created user,
        // which is a minor inefficiency. A more optimized approach would involve
        // using Statement.RETURN_GENERATED_KEYS to get the ID and constructing the User object directly.
        return findByUsername(username);
    }

    public void updateUserStatus(int userId, boolean isActive) throws SQLException {
        String query = "UPDATE users SET is_active = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, isActive);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        // Add a default LIMIT clause to mitigate the unbounded query issue,
        // as changing the public method signature for proper pagination (e.g., adding limit/offset parameters)
        // is disallowed by the problem constraints.
        // Selected specific columns instead of '*' for better performance and clarity.
        String query = "SELECT id, username, password_hash, email, role, created_at, last_login, is_active FROM users LIMIT 1000";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query); // Using PreparedStatement

                     ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        }
        return users;
    }

    // Fixed: Replaced weak MD5 hashing with SHA-256 and improved error handling.
    // Note: For production, a dedicated password hashing library like BCrypt or Argon2 with per-user salts
    // should be used. This implementation uses SHA-256 without salt due to constraints on adding new fields
    // to the User object or modifying the database schema.
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256"); // Changed from MD5 to SHA-256
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8)); // Specify UTF-8 charset
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) { // More specific exception handling
            // Critical fix: Do not return plain text password on error.
            // Instead, throw a RuntimeException to indicate a severe configuration issue.
            throw new RuntimeException("Error hashing password: SHA-256 algorithm not found.",
                    e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"), rs.getString("username"), rs.getString("password_hash"),
                rs.getString("email"), rs.getString("role"),
                rs.getTimestamp("created_at") != null ? new Date(rs.getTimestamp("created_at").getTime()) : null,
                rs.getTimestamp("last_login") != null ? new Date(rs.getTimestamp("last_login").getTime()) : null,
                rs.getBoolean("is_active")
        );
    }
}