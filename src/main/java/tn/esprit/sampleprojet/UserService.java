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

    private DataSource dataSource;

    // Setter injection for DataSource (following the same pattern as UserRepository)
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean authenticate(String username, String password) throws SQLException {
        String query = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    return hashPassword(password).equals(storedHash);
                }
            }
        }
        return false;
    }

    public User createUser(String username, String email, String password, String role) throws SQLException {
        String hashedPassword = hashPassword(password);
        String insertQuery = "INSERT INTO users (username, email, password_hash, role, created_at, is_active) VALUES (?, ?, ?, ?, ?, ?)";

        int generatedId = 0;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, role);
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            stmt.setBoolean(6, true);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getInt(1);
                }
            }
        }

        // Return user directly without additional DB call
        return new User(generatedId, username, hashedPassword, email, role,
                new Date(), null, true);
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
        String query = "SELECT id, username, password_hash, email, role, created_at, last_login, is_active FROM users LIMIT 1000";
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
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Add static salt for basic protection (note: per-user salt would require schema changes)
            String saltedPassword = password + "StaticSalt2024";
            byte[] hash = md.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password: SHA-256 algorithm not found.", e);
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