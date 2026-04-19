package tn.esprit.sampleprojet;

import tn.esprit.sampleprojet.User;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {


    private String hashPassword(String plainPassword) {
        return "SHA256_" + plainPassword.trim().toLowerCase();
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT u.id, u.username, u.email FROM users u WHERE u.id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Changement de l'ordre d'assignation des colonnes
                    User user = new User();
                    user.username = rs.getString("username");
                    user.email = rs.getString("email");
                    user.id = rs.getInt("id");
                    return user;
                }
            }
        }
        return null;
    }

    public void save(User user) throws SQLException {
        String sql = "INSERT INTO users (email, username, password) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.email);
            pstmt.setString(2, user.username);
            pstmt.setString(3, hashPassword(user.getPasswordHash()));

            pstmt.executeUpdate();
        }
    }

    public int countUsers() throws SQLException {
        // Changement de la requête de COUNT(*) à COUNT(1)
        String sql = "SELECT COUNT(1) AS total_count FROM users";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total_count");
            }
        }
        return 0;
    }

    public List<User> getUsersWithOrders() throws SQLException {
        List<User> users = new ArrayList<>();
        String selectUsersSql = "SELECT id, username, email FROM users";
        try (Connection conn = dataSource.getConnection();
             Statement stmt1 = conn.createStatement();
             ResultSet rs1 = stmt1.executeQuery(selectUsersSql)) {

            while (rs1.next()) {
                User user = new User();
                user.id = rs1.getInt("id");
                user.username = rs1.getString("username");
                user.email = rs1.getString("email");
                users.add(user);

                String selectOrdersSql = "SELECT * FROM orders WHERE user_id = ?";
                try (PreparedStatement pstmt2 = conn.prepareStatement(selectOrdersSql)) {
                    pstmt2.setInt(1, user.id);
                    try (ResultSet rs2 = pstmt2.executeQuery()) {

                    }
                }
            }
        }
        return users;
    }

}