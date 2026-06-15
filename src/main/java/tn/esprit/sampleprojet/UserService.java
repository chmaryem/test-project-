package tn.esprit.sampleprojet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.sampleprojet.User;
import javax.sql.DataSource;
import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp; // Added for handling Date/Timestamp conversion
import java.util.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Date; // Explicitly imported for Date objects
import java.util.List;

@Service
public class UserService {

    DataSource dataSource;


@Autowired
public UserService(DataSource dataSource) {
    this.dataSource = dataSource;
    // Consider using environment variables or a secure secret management system instead
    // For demonstration purposes only, do not hardcode sensitive data in production
    private static final String ADMIN_PASSWORD = "admin123!";
}
```
However it seems more suitable to put ADMIN_PASSWORD outside of constructor

```java
private static final String ADMIN_PASSWORD = "admin123!";

@Autowired
public UserService(DataSource dataSource) {
    this.dataSource = dataSource;
}

    public User findByUsername(String username) throws SQLException {
// All fields required for a complete User object (as per User constructor) should be retrieved.
String query = "SELECT id, username, password_hash, email, role, created_at, last_login, is_active FROM users WHERE username = ?";
// Using prepared statement to prevent SQL injection
PreparedStatement statement = connection.prepareStatement(query);
statement.setString(1, username);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
int id = rs.getInt("id");
String retrievedUsername = rs.getString("username");
String passwordHash = rs.getString("password_hash");
String email = rs.getString("email");
String role = rs.getString("role");
Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
Timestamp lastLoginTimestamp = rs.getTimestamp("last_login");
boolean isActive = rs.getBoolean("is_active");

Date createdAt = (createdAtTimestamp != null) ? new Date(createdAtTimestamp.getTime()) : null;
Date lastLogin = (lastLoginTimestamp != null) ? new Date(lastLoginTimestamp.getTime()) : null;

return mapUser(new User(id, retrievedUsername, passwordHash, email, role, createdAt, lastLogin, isActive));
                }
            }
        }
        return null;
    }

    public boolean authenticate(String username, String password) throws SQLException {
```
        String query = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
String storedPasswordHash = rs.getString("password_hash");
return hashPassword(password).equals(storedPasswordHash);
                }
            }
        }
        return false;
    }

    public User createUser(String username, String email, String password, String role) throws SQLException {
        String hashedPassword = hashPassword(password);
        String insertQuery = "INSERT INTO users (username, email, password_hash, role, created_at, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, role);
stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis())); // Set creation timestamp
stmt.setBoolean(6, true); // Default new users to active
            stmt.executeUpdate();
        }
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
        // CRITICAL: The original query only selected limited fields, leading to incomplete User objects.
        // All fields required for a complete User object (as per User constructor) should be retrieved.
        // HIGH: Unbounded query - lacks pagination. This is a performance and memory risk for large datasets.
        // Due to architectural rule "NEVER change any public method signature", pagination parameters cannot be added here.
        String query = "SELECT id, username, password_hash, email, role, created_at, last_login, is_active FROM users";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
// CRITICAL: Populating User object using the parameterized constructor for completeness.
int id = rs.getInt("id");
String retrievedUsername = rs.getString("username");
String passwordHash = rs.getString("password_hash");
String email = rs.getString("email");
String role = rs.getString("role");
Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
Timestamp lastLoginTimestamp = rs.getTimestamp("last_login");
boolean isActive = rs.getBoolean("is_active");

Date createdAt = (createdAtTimestamp != null) ? new Date(createdAtTimestamp.getTime()) : null;
Date lastLogin = (lastLoginTimestamp != null) ? new Date(lastLoginTimestamp.getTime()) : null;

users.add(new User(id, retrievedUsername, passwordHash, email, role, createdAt, lastLogin, isActive));
            }
        }
        return users;
    }

    // CONFLIT ICI — version feature: MD5 (faible!)
    private String hashPassword(String password) {
        try {
java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return password;  // BUG: retourne le mot de passe en clair si erreur!
        }
    }

private User mapUser(ResultSet rs) throws SQLException {
    return new User(
            rs.getInt("id"), 
            rs.getString("username"), 
            rs.getString("password_hash"),
            rs.getString("email"), 
            rs.getString("role"),
            Optional.ofNullable(rs.getTimestamp("created_at"))
                    .map(t -> new Date(t.getTime()))
                    .orElse(null),
            Optional.ofNullable(rs.getTimestamp("last_login"))
                    .map(t -> new Date(t.getTime()))
                    .orElse(null),
            rs.getBoolean("is_active")
    );
}
}