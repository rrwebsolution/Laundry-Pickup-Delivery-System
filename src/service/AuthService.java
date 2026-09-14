package service;

import dao.UserDAO;
import model.User;
import model.UserStatus;
import util.PasswordUtil;

import java.sql.SQLException;

/** Handles login authentication against the users table. */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password) throws ValidationException, SQLException {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new ValidationException("Username and password are required.");
        }
        User user = userDAO.findByUsername(username.trim());
        if (user == null || !PasswordUtil.matches(password, user.getPasswordHash())) {
            throw new ValidationException("Invalid username or password.");
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new ValidationException("This account has been deactivated. Contact the administrator.");
        }
        return user;
    }
}
