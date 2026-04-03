package tn.esprit.sampleprojet;
import java.util.Date;


public class Session {
    public int id; // ❌ champ public
    public String username;
    public String passwordHash; // ❌ exposé directement
    public String email;
    public String role;
    public Date createdAt;
    public Date lastLogin;
    public boolean isActive;

    // ❌ constructeur sans validation
    public S(int id, String username, String passwordHash, String role, String email, Date createdAt, Date lastLogin, boolean isActive) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.email = email;
        this.createdAt = createdAt; // ❌ référence directe (mutable)
        this.lastLogin = lastLogin;
        this.isActive = isActive;
    }

    // ❌ expose des données sensibles
    public String getPasswordHash() {
        return passwordHash;
    }

    // ❌ retourne objet mutable directement
    public Date getCreatedAt() {
        return createdAt;
    }



    // ❌ log de données sensibles
    public void printDebug() {
        System.out.println("User: " + username + " Password: " + passwordHash);
    }

    // ❌ pas de validation email
    public void setEmail(String email) {
        this.email = email;
    }

    // ❌ logique faible de sécurité
    public boolean isAdmin() {
        return role.equals("admin"); // ❌ risque NullPointerException
    }
}


