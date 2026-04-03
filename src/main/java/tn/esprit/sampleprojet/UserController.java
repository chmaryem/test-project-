package tn.esprit.sampleprojet;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired; // Added for Spring DI

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level; // Added for logging
import java.util.logging.Logger; // Added for logging

@Controller
public class UserController {

    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName()); // Declared missing LOGGER

    private final UserService userService; // Declare the UserService field

    @Autowired // Annotate constructor for Spring DI
    public UserController(UserService userService) {
        this.userService = userService;
    }

    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        try {
            // CRITICAL: The underlying UserService.authenticate uses a weak hashing placeholder (SHA-256).
            // Stronger, adaptive algorithms like BCrypt, Argon2, or PBKDF2 should be used for password hashing.
            // This is an issue in UserService, not directly fixable here without changing UserService.
            if (userService.authenticate(username.trim(), password)) {
                User user = userService.findByUsername(username.trim());
                if (user == null) {
                    LOGGER.log(Level.SEVERE, "Authentication succeeded for user {0} but user not found. Internal inconsistency.", username.trim());
                    throw new RuntimeException("Authentication succeeded but user not found. Internal inconsistency.");
                }
                return user;
            } else {
                LOGGER.log(Level.WARNING, "Failed login attempt for user: {0}", username.trim());
                throw new RuntimeException("Invalid credentials.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error during login for user {0}: {1}", new Object[]{username.trim(), e.getMessage()});
            throw new RuntimeException("An unexpected error occurred during login. Please try again later.");
        }
    }

    public User register(String username, String email, String password, String role) {
        if (username == null || username.trim().isEmpty() || username.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long and not empty.");
        }
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) { // Basic email regex
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (password == null || password.length() < 8 || !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")
                || !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain uppercase, lowercase, a digit, and a special character.");
        }

        String effectiveRole = "USER"; // Default role for new registrations
        // The original code had an empty if block for role. Assuming the intent is to always default to "USER"
        // and ignore the 'role' parameter if it's not explicitly handled for security reasons.
        // If other roles are intended to be assignable, this logic needs to be expanded in UserService.

        try {
            User newUser = userService.createUser(username.trim(), email.trim(), password, effectiveRole);
            return newUser;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error during registration for user {0}: {1}", new Object[]{username.trim(), e.getMessage()});
            throw new RuntimeException("An unexpected error occurred during registration. Please try again later.");
        }
    }

    public List<User> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            // The original code had an empty for loop here. It has been removed as it served no purpose.
            // TODO: Implement pagination for getAllUsers to prevent unbounded queries and improve performance for large datasets.
            return users;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error while retrieving all users: {0}", e.getMessage());              throw new RuntimeException("An unexpected error occurred while retrieving users. Please try again later.");
        }
    }

    public void updateUser(int userId, String newEmail, String newPassword) {
        if (newEmail == null || !newEmail.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (newPassword == null || newPassword.length() < 8 || !newPassword.matches(".*[A-Z].*") || !newPassword.matches(".*[a-z].*") || !newPassword.matches(".*\\d.*") || !newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new IllegalArgumentException("New password must be at least 8 characters long and contain uppercase, lowercase, a digit, and a special character.");
        }

        try {
            User user = userService.findById(userId);
            if (user == null) {
                LOGGER.log(Level.WARNING, "Attempted to update non-existent user with ID: {0}", userId);
                throw new IllegalArgumentException("User with ID " + userId + " not found.");
            }
            // CRITICAL: User.email is a public field, violating encapsulation.
            // Ideally, User should have a private email field and a public setter.
            // As per constraints, User.java cannot be modified here.
            user.email = newEmail.trim();

            // CRITICAL: The newPassword parameter is validated but never used to update the user's password.
            // This is a functional bug and security oversight.
            // A corresponding method (e.g., userService.updateUserPassword(userId, newPassword))
            // is missing in UserService.java and cannot be invented as per constraints.
            // TODO: Implement password update logic in UserService and call it here.
            LOGGER.log(Level.INFO, "User {0} email updated to {1}. Password update skipped (UserService method missing).", new Object[]{userId, newEmail.trim()});

            // Assuming there should be a call to UserService to persist changes,
            // but no such method (e.g., userService.save(user) or userService.updateUser(user))
            // is explicitly provided in the UserService snippet for general updates.
            // If userService.save(user) exists and handles updates, it should be called here.
            // For now, only the email field is "updated" in the in-memory object.
            // This is an architectural gap.
            // TODO: Add a call to a UserService method to persist the updated user object.

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error while updating user {0}: {1}", new Object[]{userId, e.getMessage()});
            throw new RuntimeException("An unexpected error occurred while updating user profile.
                    Please try again later.");
        }
    }

    public void resetPassword(String username, String newPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (newPassword == null || newPassword.length() < 8 || !newPassword.matches(".*[A-Z].*") || !newPassword.matches(".*[a-z].*") || !newPassword.matches(".*\\d.*") || !newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new IllegalArgumentException("New password must be at least 8 characters long and contain uppercase, lowercase, a digit, and a special character.");
        }

        try {
            User user = userService.findByUsername(username.trim());
            if (user == null) {
                LOGGER.log(Level.WARNING, "Attempted password reset for non-existent username: {0}", username.trim());
                throw new IllegalArgumentException("Invalid username or password reset request.");              }
            // CRITICAL: The newPassword parameter is validated but never used to update the user's password.
            // This is a functional bug and security oversight.
            // A corresponding method (e.g., userService.resetPassword(username, newPassword))
            // is missing in UserService.java and cannot be invented as per constraints.
            // TODO: Implement password reset logic in UserService and call it here.
            LOGGER.log(Level.INFO, "Password reset requested for user {0}. Password update skipped (UserService method missing).", username.trim());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error during password reset for user {0}: {1}", new Object[]{username.trim(), e.getMessage()});
            throw new RuntimeException("An unexpected error occurred during password reset. Please try again later.");
        }
    }
}