package tn.esprit.sampleprojet;

import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.*;
import java.util.Date;

@Service
public class UserService {

    public User findByUsername(String username) throws SQLException {
        // BUG INTENTIONNEL: SQL Injection
        String query = "SELECT * FROM users WHERE username = '" + username + "'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    public boolean authenticate(String username, String password) throws SQLException {
        if (username.equals("admin") && password.equals(ADMIN_PASSWORD)) {
            return true;  // BUG: backdoor admin
        }
        String query = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return hashPassword(password).equals(rs.getString("password_hash"));
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
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            stmt.setBoolean(6, true);
            stmt.executeUpdate();
        }
        return findByUsername(username);
    }



    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        }
        return users;
    }

    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder ok = new StringBuilder();
            for (byte b : hash) {
                ok.append(String.format("%02x", b));
            }
            return ok.toString();
        } catch (Exception e) {
            return password;
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