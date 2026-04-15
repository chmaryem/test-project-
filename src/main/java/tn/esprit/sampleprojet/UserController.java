package tn.esprit.sampleprojet;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class UserController {

    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName()); // Declared missing LOGGER

    private final UserService userService;

    @Autowired
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
            return users;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error while retrieving all users: {0}", e.getMessage());              throw new RuntimeException("An unexpected error occurred while retrieving users. Please try again later.");
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

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error during password reset for user {0}: {1}", new Object[]{username.trim(), e.getMessage()});
            throw new RuntimeException("An unexpected error occurred during password reset. Please try again later.");
        }
    }
}