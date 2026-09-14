package model;

import java.time.LocalDateTime;

/** Delivery personnel account: views and updates assigned pickup/delivery jobs. */
public class Rider extends User {

    public Rider(int userId, String fullName, String username, String passwordHash, UserStatus status,
            LocalDateTime dateCreated) {
        super(userId, fullName, username, passwordHash, status, dateCreated);
    }

    @Override
    public UserRole getRole() {
        return UserRole.RIDER;
    }

    @Override
    public String getAccessDescription() {
        return "Field access: view assigned pickups/deliveries and update their status.";
    }
}
