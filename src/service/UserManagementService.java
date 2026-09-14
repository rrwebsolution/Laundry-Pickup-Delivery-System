package service;

import dao.UserDAO;
import event.DataChangeEvent;
import event.DataChangeManager;
import model.User;
import model.UserFactory;
import model.UserRole;
import model.UserStatus;
import util.PasswordUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class UserManagementService {

    private final UserDAO userDAO = new UserDAO();

    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }

    public int addUser(String fullName, String username, String plainPassword, UserRole role, UserStatus status)
            throws ValidationException, SQLException {
        validate(fullName, username, plainPassword);
        if (userDAO.usernameExists(username.trim())) {
            throw new ValidationException("Username already exists. Choose another.");
        }
        User user = UserFactory.create(0, fullName.trim(), username.trim(), PasswordUtil.hash(plainPassword), role,
                status, LocalDateTime.now());
        int id = userDAO.create(user);
        DataChangeManager.notifyListeners(DataChangeEvent.USER_CHANGED);
        return id;
    }

    /** Updates a user; pass null/blank plainPassword to keep the existing password unchanged. */
    public void updateUser(User existing, String fullName, String username, String plainPassword, UserRole role,
            UserStatus status) throws ValidationException, SQLException {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Full name is required.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username is required.");
        }
        existing.setFullName(fullName.trim());
        existing.setUsername(username.trim());
        existing.setStatus(status);
        if (plainPassword != null && !plainPassword.trim().isEmpty()) {
            existing.setPasswordHash(PasswordUtil.hash(plainPassword));
        }
        User updated = UserFactory.create(existing.getUserId(), existing.getFullName(), existing.getUsername(),
                existing.getPasswordHash(), role, existing.getStatus(), existing.getDateCreated());
        userDAO.update(updated);
        DataChangeManager.notifyListeners(DataChangeEvent.USER_CHANGED);
    }

    public void deleteUser(int userId) throws SQLException {
        userDAO.delete(userId);
        DataChangeManager.notifyListeners(DataChangeEvent.USER_CHANGED);
    }

    private void validate(String fullName, String username, String plainPassword) throws ValidationException {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Full name is required.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username is required.");
        }
        if (plainPassword == null || plainPassword.length() < 4) {
            throw new ValidationException("Password must be at least 4 characters.");
        }
    }
}
