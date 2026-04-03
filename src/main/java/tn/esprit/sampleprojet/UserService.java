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

    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());

    private DataSource dataSource;

    // Constructor for DataSource injection
    public UserService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Finds a user by their username.
     *
     * @param username The username to search for.
     * @return A User object if found, null otherwise. Note: This User object will only contain id, username, and email.
     * @throws SQLException If a database access error occurs.
     */
    public User findByUsername(String username) throws SQLException {
        // SECURITY: Do not select password_hash here to prevent accidental exposure.
        String query = "SELECT id, username, email FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // ARCHITECTURE: User object is partially populated (id, username, email).
                    // Other fields like passwordHash, role, createdAt, etc., are not fetched for security and performance.
                    User user = new User(); // Using default constructor
                    user.id = rs.getInt("id");
                    user.username = rs.getString("username");
                    user.email = rs.getString("email");
                    return user;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while finding user by username: " + username, e);
            throw e; // Re-throw to indicate a critical failure
        }
        return null;
    }

    /**
     * Authenticates a user by checking their username and password.
     *
     * @param username The user's username.
     * @param password The user's plain-text password.
     * @return True if authentication is successful, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public boolean authenticate(String username, String password) throws SQLException {
        // CRITICAL: SHA-256 is not suitable for password hashing. Stronger, adaptive algorithms
        // like BCrypt, Argon2, or PBKDF2 should be used. As per constraints, new libraries          // cannot be introduced, so this placeholder remains. This is a known security vulnerability.
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
        } catch (SQLException e) {
            // QUALITY: Log the exception instead of silently returning false.
            LOGGER.log(Level.SEVERE, "Database error during authentication for user: " + username, e);
            throw e; // Re-throw to indicate a critical failure
        }
        return false;
    }

    /**
     * Creates a new user in the system.
     *
     * @param username The new user's username.
     * @param email The new user's email.
     * @param password The new user's plain-text password.
     * @param role The new user's role.
     * @return The newly created User object, including its generated ID.
     * @throws SQLException If a database access error occurs.
     * @throws IllegalStateException If a user with the given username already exists.
     */
    public User createUser(String username, String email, String password, String role) throws SQLException {
        // CRITICAL: SHA-256 is not suitable for password hashing. Stronger, adaptive algorithms
        // like BCrypt, Argon2, or PBKDF2 should be used. As per constraints, new libraries          // cannot be introduced, so this placeholder remains. This is a known security vulnerability.
        String hashedPassword = hashPasswordPlaceholder(password);

        // Check if user already exists
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
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while checking for existing user: " +
                    username, e);
            throw e;
        }

        // Insert new user
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
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while creating user: " + username, e);              throw e;
        }
    }

    /**
     * Updates the active status of a user.
     *
     * @param userId The ID of the user to update.
     * @param isActive The new active status.
     * @throws SQLException If a database access error occurs.
     */
    public void updateUserStatus(int userId, boolean isActive) throws SQLException {
        String query = "UPDATE users SET isActive = ? WHERE id = ?"; // Assuming column name is 'isActive'
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, isActive);
            stmt.setInt(2, userId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.log(Level.WARNING, "No user found with ID {0} to update status.", userId);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while updating status for user ID: " + userId, e);
            throw e;
        }
    }

    /**
     * Retrieves a list of all users in the system.
     *
     * @return A list of User objects. Note: These User objects will only contain id, username, and email.
     * @throws SQLException If a database access error occurs.
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        // HIGH: Unbounded query. For large datasets, this can lead to performance issues and OutOfMemoryError.
        // Consider adding pagination (LIMIT/OFFSET) for scalable applications.
        String query = "SELECT id, username, email FROM users";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) { // CRITICAL: Missing closing brace and return statement in original code
            while (rs.next()) {
                // ARCHITECTURE: User object is partially populated (id, username, email).
                // Other fields like passwordHash, role, createdAt, etc., are not fetched for security and performance.
                User user = new User(); // Using default constructor
                user.id = rs.getInt("id");
                user.username = rs.getString("username");
                user.email = rs.getString("email");
                users.add(user);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while retrieving all users.", e);
            throw e;
        }
        return users;
    }

    /**
     * Hashes a plain-text password using SHA-256.
     *
     * @param plainPassword The plain-text password.
     * @return The SHA-256 hash of the password as a hexadecimal string.
     * @throws RuntimeException If the SHA-256 algorithm is not found.
     */
    private String hashPasswordPlaceholder(String plainPassword) {
        try {
            // CRITICAL: SHA-256 is not suitable for password hashing. It's too fast and
            // susceptible to brute-force attacks. Stronger, adaptive algorithms like
            // BCrypt, Argon2, or PBKDF2 should be used. As per constraints, new libraries
            // cannot be introduced, so this placeholder remains. This is a known security

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