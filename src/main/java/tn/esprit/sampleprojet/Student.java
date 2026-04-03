package tn.esprit.sampleprojet;

import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Student {
    int id;
    String username;
    private String passwordHash;
    String email;
    private String role;
    private Date createdAt;
    private Date lastLogin;
    private boolean isActive;






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






}
