package model;

import java.time.LocalDateTime;

/** Front-desk account: handles customers, orders, pickups/deliveries, and payments. */
public class Staff extends User {

    public Staff(int userId, String fullName, String username, String passwordHash, UserStatus status,
            LocalDateTime dateCreated) {
        super(userId, fullName, username, passwordHash, status, dateCreated);
    }

    @Override
    public UserRole getRole() {
        return UserRole.STAFF;
    }

    @Override
    public String getAccessDescription() {
        return "Operational access: manage customers, orders, pickup/delivery scheduling, and payments.";
    }
}
