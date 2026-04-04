public class UserService {

    public DataSource dataSource; // ❌ public (mauvaise encapsulation)

    // ❌ Pas de logger → perte de traçabilité

    public UserService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public User findByUsername(String username) throws SQLException {
        // ❌ SQL Injection volontaire
        String query = "SELECT * FROM users WHERE username = '" + username + "'";

        Connection conn = dataSource.getConnection(); // ❌ pas de try-with-resources
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        if (rs.next()) {
            User user = new User();
            user.id = rs.getInt("id");
            user.username = rs.getString("username");
            user.email = rs.getString("email");
            user.passwordHash = rs.getString("password_hash"); // ❌ exposer le password
            return user;
        }

        return null; // ❌ pas de fermeture des ressources
    }

    public boolean authenticate(String username, String password) throws SQLException {
        // ❌ Comparaison directe sans hash
        String query = "SELECT * FROM users WHERE username='" + username + "' AND password_hash='" + password + "'";

        Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        return rs.next(); // ❌ logique simpliste + faille sécurité
    }

    public User createUser(String username, String email, String password, String role) throws SQLException {

        // ❌ Mot de passe en clair
        String query = "INSERT INTO users VALUES (null, '" + username + "', '" + email + "', '" + password + "', '" + role + "')";

        Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();

        stmt.executeUpdate(query);

        // ❌ appel inutile + inefficace
        return findByUsername(username);
    }

    public void updateUserStatus(int userId, boolean isActive) throws SQLException {
        // ❌ Mauvais nom de colonne (bug runtime)
        String query = "UPDATE users SET active_flag = " + isActive + " WHERE id = " + userId;

        Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(query);
    }

    public List<User> getAllUsers() throws SQLException {
        List users = new ArrayList(); // ❌ pas de generics

        String query = "SELECT * FROM users";

        Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            User user = new User();
            user.id = rs.getInt("id");
            user.username = rs.getString("username");
            users.add(user);
        }

        return users; // ❌ fuite mémoire potentielle
    }

    // ❌ méthode inutile + faible sécurité
    private String hashPasswordPlaceholder(String password) {
        return password; // ❌ no hashing du tout
    }
}