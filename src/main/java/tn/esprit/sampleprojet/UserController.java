package tn.esprit.sampleprojet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public User login(@RequestParam String username, @RequestParam String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Username and password are required.");
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

    @PostMapping("/register")
    public User register(@RequestParam String username, @RequestParam String email,
                         @RequestParam String password, @RequestParam(required = false) String role) {
        if (username == null || username.trim().isEmpty() || username.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long and not empty.");
        }
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (password == null || password.length() < 8 || !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")
                || !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain uppercase, lowercase, a digit, and a special character.");
        }


        String effectiveRole = "USER";

        try {
            return userService.createUser(username.trim(), email.trim(), password, effectiveRole);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error during registration for user {0}: {1}", new Object[]{username.trim(), e.getMessage()});
            throw new RuntimeException("An unexpected error occurred during registration. Please try again later.");
        }
    }

    @GetMapping
    public List<User> getAllUsers() {
        try {
            return userService.getAllUsers();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error while retrieving all users: {0}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while retrieving users. Please try again later.");
        }
    }


}