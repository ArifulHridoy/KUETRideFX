package com.example.kuet_transportation_and_schedueling_system.util;

import com.example.kuet_transportation_and_schedueling_system.model.User;

/**
 * SessionManager - Manages current logged-in user session
 * Demonstrates: Singleton Pattern, Session Management
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Set current logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        Logger.getInstance().info("User logged in: " + user.getUsername());
    }

    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return currentUser != null && "admin".equals(currentUser.getRole());
    }

    /**
     * Check if current user is student
     */
    public boolean isStudent() {
        return currentUser != null && "student".equals(currentUser.getRole());
    }

    /**
     * Logout current user
     */
    public void logout() {
        if (currentUser != null) {
            Logger.getInstance().info("User logged out: " + currentUser.getUsername());
            currentUser = null;
        }
    }

    /**
     * Clear session
     */
    public void clearSession() {
        currentUser = null;
    }
}

