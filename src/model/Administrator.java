package model;

import java.time.LocalDateTime;

/** Full-access account: manages users, services, and views all reports. */
public class Administrator extends User {

    public Administrator(int userId, String fullName, String username, String passwordHash, UserStatus status,
            LocalDateTime dateCreated) {
        super(userId, fullName, username, passwordHash, status, dateCreated);
    }

    @Override
    public UserRole getRole() {
        return UserRole.ADMINISTRATOR;
    }

    @Override
    public String getAccessDescription() {
        return "Full system access: manage users, services, customers, orders, payments, and reports.";
    }
}
