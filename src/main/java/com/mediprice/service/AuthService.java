package com.mediprice.service;

import com.mediprice.dao.UserDAO;
import com.mediprice.model.User;
import com.mediprice.util.PasswordUtil;
import com.mediprice.util.SessionManager;
import com.mediprice.util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service handling authentication: login, registration, account locking.
 */
public class AuthService {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Registers a new user.
     * @return the new User on success
     * @throws ServiceException on validation or DB error
     */
    public User register(String username, String email, String password, String confirmPassword)
            throws ServiceException {
        // Validate inputs
        if (!ValidationUtil.isValidUsername(username)) {
            throw new ServiceException("Username must be 3–30 characters (letters, digits, underscores only).");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new ServiceException("Invalid email address.");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new ServiceException("Password must be at least 6 characters.");
        }
        if (!password.equals(confirmPassword)) {
            throw new ServiceException("Passwords do not match.");
        }

        try {
            if (userDAO.existsByUsername(username)) {
                throw new ServiceException("Username '" + username + "' is already taken.");
            }
            if (userDAO.existsByEmail(email)) {
                throw new ServiceException("Email '" + email + "' is already registered.");
            }

            User user = new User();
            user.setUsername(username.trim());
            user.setEmail(email.trim().toLowerCase());
            user.setPasswordHash(PasswordUtil.hashPassword(password));
            user.setRole("USER");

            int id = userDAO.insert(user);
            user.setId(id);
            LOGGER.info("New user registered: " + username);
            return user;

        } catch (SQLException e) {
            throw new ServiceException("Registration failed. Please try again.", e);
        }
    }

    /**
     * Authenticates a user.
     * @return authenticated User
     * @throws ServiceException on wrong credentials or locked account
     */
    public User login(String username, String password) throws ServiceException {
        if (!ValidationUtil.isNotEmpty(username) || !ValidationUtil.isNotEmpty(password)) {
            throw new ServiceException("Username and password are required.");
        }

        try {
            Optional<User> optUser = userDAO.findByUsername(username.trim());
            if (optUser.isEmpty()) {
                throw new ServiceException("Invalid username or password.");
            }

            User user = optUser.get();

            // Check if account is locked
            if (user.isLocked()) {
                long minutesLeft = java.time.Duration.between(
                        LocalDateTime.now(), user.getLockedUntil()).toMinutes() + 1;
                throw new ServiceException("Account is locked. Try again in " + minutesLeft + " minute(s).");
            }

            // Verify password
            if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                int newAttempts = user.getFailedAttempts() + 1;
                LocalDateTime lockUntil = null;

                if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                    lockUntil = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);
                    LOGGER.warning("Account locked after " + MAX_FAILED_ATTEMPTS + " failed attempts: " + username);
                }

                userDAO.updateFailedAttempts(user.getId(), newAttempts, lockUntil);
                int remaining = MAX_FAILED_ATTEMPTS - newAttempts;

                if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                    throw new ServiceException("Too many failed attempts. Account locked for " + LOCK_DURATION_MINUTES + " minutes.");
                } else {
                    throw new ServiceException("Invalid username or password. " + remaining + " attempt(s) remaining.");
                }
            }

            // Success - reset failed attempts
            userDAO.resetFailedAttempts(user.getId());
            SessionManager.getInstance().login(user);
            LOGGER.info("User logged in: " + username);
            return user;

        } catch (SQLException e) {
            throw new ServiceException("Login failed. Please try again.", e);
        }
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }
}
