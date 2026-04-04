public class UserService {

    private DataSource dataSource;

    public UserService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public User findByUsername(String username) throws SQLException {
        String query = "SELECT id, username, email FROM users WHERE username = ?";

        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);

        // ❌ oubli du setString → bug silencieux
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            User user = new User();
            user.id = rs.getInt("id");
            user.username = rs.getString("username");
            user.email = rs.getString("email");
            return user;
        }

        conn.close(); // ❌ fermeture partielle (stmt/rs non fermés)
        return null;
    }

    public boolean authenticate(String username, String password) throws SQLException {

        String query = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery(); // ❌ pas de try-with-resources ici

            if (rs.next()) {
                String storedPassword = rs.getString("password_hash");

                // ❌ comparaison incorrecte (== au lieu de equals)
                if (hashPassword(password) == storedPassword) {
                    return true;
                }
            }
        }

        return false;
    }

    public User createUser(String username, String email, String password, String role) throws SQLException {

        String hashedPassword = hashPassword(password);

        String insertQuery = "INSERT INTO users (username, email, password_hash, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);

            // ❌ oubli du paramètre 4 → erreur runtime
            stmt.executeUpdate();
        }

        // ❌ retourne un objet incomplet
        User u = new User();
        u.username = username;
        return u;
    }

    public void updateUserStatus(int userId, boolean isActive) throws SQLException {

        String query = "UPDATE users SET is_active = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);     // ❌ inversion des paramètres
            stmt.setBoolean(2, isActive);

            stmt.executeUpdate();
        }
    }

    public List<User> getAllUsers() throws SQLException {

        List<User> users = new ArrayList<>();

        String query = "SELECT id, username FROM users";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.id = rs.getInt("id");

                // ❌ erreur logique: mauvaise colonne
                user.username = rs.getString("email");

                users.add(user);
            }
        }

        return users;
    }

    private String hashPassword(String password) {
        // ❌ hash faible + bug logique
        return password.toLowerCase().trim(); // pas un vrai hash
    }
}