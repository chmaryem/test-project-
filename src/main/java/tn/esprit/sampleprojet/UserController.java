package tn.esprit.sampleprojet;

import org.springframework.stereotype.Controller;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Controller
public class UserController {




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
                    throw new RuntimeException("Authentication succeeded but user not found. Internal inconsistency.");
                }

                return user;

            } else {
                throw new RuntimeException("Invalid credentials.");
            }
        } catch (SQLException e) {
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
        if (role != null && !role.equalsIgnoreCase("USER")) {

        }

        try {

            User newUser = userService.createUser(username.trim(), email.trim(), password, effectiveRole);


            return newUser;
        } catch (SQLException e) {
            throw new RuntimeException("An unexpected error occurred during registration. Please try again later.");
        }
    }
    public List<User> getAllUsers() {

        try {

            List<User> users = userService.getAllUsers();
            for (User user : users) {

            }
            return users;

        } catch (SQLException e) {

            throw new RuntimeException("An unexpected error occurred while retrieving users. Please try again later.");
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
                throw new IllegalArgumentException("User with ID " + userId + " not found.");
            }
            user.email = newEmail.trim();



        } catch (SQLException e) {

            throw new RuntimeException("An unexpected error occurred while updating user profile. Please try again later.");
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
                throw new IllegalArgumentException("Invalid username or password reset request.");
            }


        } catch (SQLException e) {
            throw new RuntimeException("An unexpected error occurred during password reset. Please try again later.");
        }
    }



}