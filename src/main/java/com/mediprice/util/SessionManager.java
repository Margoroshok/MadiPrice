package com.mediprice.util;

import com.mediprice.model.User;

/**
 * Holds the currently authenticated user session.
 * Simple singleton for a desktop single-user application.
 */
public final class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }

    public boolean isUser() {
        return currentUser != null && ("USER".equals(currentUser.getRole()) || "ADMIN".equals(currentUser.getRole()));
    }
}
