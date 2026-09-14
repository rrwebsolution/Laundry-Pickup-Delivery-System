package model;

import java.time.LocalDateTime;

/**
 * Abstract base class for every account in the system. Concrete roles
 * ({@link Administrator}, {@link Staff}, {@link Rider}) provide the
 * role-specific behaviour, demonstrating inheritance and polymorphism.
 */
public abstract class User {

    private int userId;
    private String fullName;
    private String username;
    private String passwordHash;
    private UserStatus status;
    private LocalDateTime dateCreated;

    protected User(int userId, String fullName, String username, String passwordHash, UserStatus status,
            LocalDateTime dateCreated) {
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
        this.passwordHash = passwordHash;
        this.status = status;
        this.dateCreated = dateCreated;
    }

    /** Each subclass reports its own role. */
    public abstract UserRole getRole();

    /** Each subclass describes what it is allowed to do (polymorphic behaviour). */
    public abstract String getAccessDescription();

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    @Override
    public String toString() {
        return fullName + " (" + getRole().getLabel() + ")";
    }
}
