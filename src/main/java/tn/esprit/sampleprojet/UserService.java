package tn.esprit.sampleprojet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class UserService {

    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());
    private static final int SALT_LENGTH = 16;
    private static final int DEFAULT_PAGE_SIZE = 50;

    private final DataSource dataSource;

    @Autowired
    public UserService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public User findByUsername(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
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
        LOGGER.log(Level.FINE, "User not found: {0}", username);
        return null;
    }

    public boolean authenticate(String username, String password) throws SQLException {
        if (username == null || password == null) {
            return false;
        }
        String query = "SELECT password_hash, is_active FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    boolean isActive = rs.getBoolean("is_active");
                    if (!isActive) {
                        LOGGER.log(Level.WARNING, "Login attempt on inactive account: {0}", username);
                        return false;
                    }
                    return verifyPassword(password, storedHash);
                }
            }
        }
        LOGGER.log(Level.WARNING, "Failed authentication attempt for username: {0}", username);
        return false;
    }

    public User createUser(String username, String email, String password, String role) throws SQLException {
        if (username == null || email == null || password == null || role == null) {
            throw new IllegalArgumentException("All parameters must be non-null");
        }
        String hashedPassword = hashPassword(password);
        String insertQuery = "INSERT INTO users (username, email, password_hash, role, created_at, is_active) VALUES (?, ?, ?, ?, ?, ?)";

        User createdUser = null;
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
                    int generatedId = generatedKeys.getInt(1);
                    createdUser = new User(
                            generatedId, username, null, email, role,
                            new Date(), null, true
                    );
                }
            }
        }
        return createdUser;
    }

    public void updateUserStatus(int userId, boolean isActive) throws SQLException {
        String query = "UPDATE users SET is_active = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, isActive);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                LOGGER.log(Level.WARNING, "No user found with id: {0}", userId);
            }
        }
        LOGGER.log(Level.INFO, "User status updated: id={0}, isActive={1}", new Object[]{userId, isActive});
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT id, username, email, role, created_at, last_login, is_active FROM users LIMIT ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, DEFAULT_PAGE_SIZE);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        }
        LOGGER.log(Level.FINE, "Retrieved {0} users", users.size());
        return users;
    }

    /**
     * Hash un nouveau mot de passe avec un sel aléatoire.
     * Utilisé uniquement lors de la création / changement de mot de passe.
     */
    private String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            String hash = computeHash(password, salt);
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            return saltBase64 + ":" + hash;
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "SHA-256 algorithm not available", e);
            throw new RuntimeException("Error hashing password: SHA-256 algorithm not found.", e);
        }
    }

    /**
     * Vérifie un mot de passe en réutilisant le sel stocké,
     * indispensable pour que la comparaison soit possible.
     */
    private boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null || !storedHash.contains(":")) {
            return false;
        }
        String[] parts = storedHash.split(":", 2);
        try {
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            String expectedHash = parts[1];
            String computedHash = computeHash(password, salt);
            return constantTimeEquals(computedHash, expectedHash);
        } catch (IllegalArgumentException | NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "Error verifying password", e);
            return false;
        }
    }

    private String computeHash(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(aBytes, bBytes);
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                null, // Ne pas exposer password_hash
                rs.getString("email"),
                rs.getString("role"),
                rs.getTimestamp("created_at") != null ? new Date(rs.getTimestamp("created_at").getTime()) : null,
                rs.getTimestamp("last_login") != null ? new Date(rs.getTimestamp("last_login").getTime()) : null,
                rs.getBoolean("is_active")
        );
    }
}