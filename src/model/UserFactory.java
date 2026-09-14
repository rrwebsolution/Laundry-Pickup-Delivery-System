package model;

import java.time.LocalDateTime;

/** Creates the correct {@link User} subclass for a given role (Factory pattern / abstraction). */
public final class UserFactory {

    private UserFactory() {
    }

    public static User create(int userId, String fullName, String username, String passwordHash, UserRole role,
            UserStatus status, LocalDateTime dateCreated) {
        switch (role) {
            case ADMINISTRATOR:
                return new Administrator(userId, fullName, username, passwordHash, status, dateCreated);
            case STAFF:
                return new Staff(userId, fullName, username, passwordHash, status, dateCreated);
            case RIDER:
                return new Rider(userId, fullName, username, passwordHash, status, dateCreated);
            default:
                throw new IllegalArgumentException("Unsupported role: " + role);
        }
    }
}
