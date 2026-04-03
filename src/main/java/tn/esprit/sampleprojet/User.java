package tn.esprit.sampleprojet;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import jakarta.annotation.Nonnull;


public class User {
    int id;
    String username;
    private String passwordHash;
    String email;
    private String role;
    private Date createdAt;
    private Date lastLogin;
    private boolean isActive;
    private List<String> permissions;
    private List<String> nonpermissions;


    // Constructor signature remains unchanged as per architectural rules.
    public User(int id, String username, String passwordHash, String email,
                String role, Date createdAt, Date lastLogin, boolean isActive) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash; // Assign to passwordHash
        this.email = email;
        this.role = role;
        this.createdAt = (createdAt != null) ? new Date(createdAt.getTime()) : null; // Defensive copy
        this.lastLogin = (lastLogin != null) ? new Date(lastLogin.getTime()) : null; // Defensive copy
        this.isActive = isActive; // Initialize isActive field
        this.permissions = new ArrayList<>(); // Initialize permissions list to prevent NullPointerException
        this.nonpermissions = new ArrayList<>(); // Initialize nonpermissions
    }

    public User() {

    }

    // Getters for all private fields to maintain encapsulation
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Date getCreatedAt() {
        return (createdAt != null) ? new Date(createdAt.getTime()) : null;
    }

    public Date getLastLogin() {
        return (lastLogin != null) ? new Date(lastLogin.getTime()) : null;
    }

    public boolean isActive() {
        return isActive;
    }

    public List<String> getPermissions() {
        return new ArrayList<>(permissions); // Return a defensive copy
    }

    public List<String> getNonpermissions() {
        return new ArrayList<>(nonpermissions); // Return a defensive copy
    }

    // Setter for permissions (if needed, otherwise permissions should be managed internally or via constructor)
    public void setPermissions(List<String> permissions) {
        this.permissions = (permissions != null) ? new ArrayList<>(permissions) : new ArrayList<>();
    }


    public void setNonpermissions(List<String> nonpermissions) {
        this.nonpermissions = (nonpermissions != null) ? new ArrayList<>(nonpermissions) : new ArrayList<>();
    }

    public boolean hasRole(String targetRole) {
        return this.role != null && this.role.equalsIgnoreCase(targetRole);
    }

    // 2. Permission Validation
    public boolean hasPermission(@Nonnull String permission) {
        // `permissions` list is now guaranteed to be initialized in the constructor.
        return isActive && permissions.contains(permission) && !nonpermissions.contains(permission);
    }

    // 3. Update Login State
    // Updates the timestamp and ensures the account is flagged as active
    public void recordSuccessfulLogin() {
        this.lastLogin = new Date();
        this.isActive = true;
    }

    @Override
    public String toString() {
        // Removed permissions and nonpermissions from toString for privacy/security.
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                ", lastLogin=" + lastLogin +
                ", isActive=" + isActive +
                '}';
    }
}