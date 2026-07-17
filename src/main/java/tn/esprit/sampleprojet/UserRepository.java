package tn.esprit.sampleprojet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.sql.DataSource;

public class UserRepository {
  
    
   
        String sql = "SELECT u.id, u.username, u.email FROM users u WHERE u.id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.username = rs.getString("username");
                    user.email = rs.getString("email");
                    user.id = rs.getInt("id");
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    public void save(User user) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.email == null || user.email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (user.username == null || user.username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        String sql = "INSERT INTO users (email, username, password) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Hash the plain password - assume user.getPasswordHash() returns plain text
            String passwordToStore = hashPassword(user.getPasswordHash() != null ? user.getPasswordHash() : "");

            pstmt.setString(1, user.email);
            pstmt.setString(2, user.username);
            pstmt.setString(3, passwordToStore);

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.id = generatedKeys.getInt(1);
                }
            }
        }
    }

    

    public List<User> getUsersWithOrders(int limit, int offset) throws SQLException {
        if (limit <= 0) {
            limit = 100; // default limit
        }
        if (offset < 0) {
            offset = 0;
        }

        List<User> users = new ArrayList<>();
        String selectUsersSql = "SELECT u.id, u.username, u.email, COUNT(o.id) as order_count " +
                "FROM users u LEFT JOIN orders o ON u.id = o.user_id " +
                "GROUP BY u.id, u.username, u.email " +
                "ORDER BY u.id " +
                "LIMIT ? OFFSET ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectUsersSql)) {
            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.id = rs.getInt("id");
                    user.username = rs.getString("username");
                    user.email = rs.getString("email");
                    users.add(user);
                }
            }
        }
        return users;
    }
}