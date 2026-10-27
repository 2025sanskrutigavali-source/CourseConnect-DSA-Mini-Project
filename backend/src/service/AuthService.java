package service;

import model.Session;
import model.User;
import repository.DataStore;
import util.PasswordUtil;

import java.util.Map;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Service managing user authentication, password verification, and session state.
 */
public class AuthService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    private final DataStore dataStore;
    // Map storing active in-memory user sessions: SessionToken -> Session
    private final Map<String, Session> activeSessions = new ConcurrentHashMap<>();

    public AuthService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public User register(String fullName, String email, String password,
                         String confirmPassword, String role) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (role == null || (!role.equalsIgnoreCase("STUDENT") && !role.equalsIgnoreCase("INSTITUTION"))) {
            throw new IllegalArgumentException("Invalid account type. Select Student or Institution.");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
        User user = new User(normalizedEmail, PasswordUtil.hashPassword(password), normalizedRole,
                fullName.trim(), normalizedEmail, "", "");
        dataStore.registerUser(user);
        return user;
    }

    /**
     * Authenticates a user by username, password, and expected role.
     * Returns a new Session if valid, or throws IllegalArgumentException with a clear message.
     */
    public Session login(String username, String password, String expectedRole) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username/Student ID cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        if (expectedRole == null || (!expectedRole.equalsIgnoreCase("STUDENT") && !expectedRole.equalsIgnoreCase("INSTITUTION"))) {
            throw new IllegalArgumentException("Invalid role selected. Must be Student or Institution.");
        }

        // O(1) user lookup in HashMap
        String normalizedUsername = username.trim();
        if (normalizedUsername.contains("@")) {
            normalizedUsername = normalizedUsername.toLowerCase(Locale.ROOT);
        }
        User user = dataStore.getUserMap().get(normalizedUsername);
        if (user == null) {
            throw new IllegalArgumentException("Invalid credentials. Account does not exist.");
        }

        // Validate role matches
        if (!user.getRole().equalsIgnoreCase(expectedRole.trim())) {
            throw new IllegalArgumentException("Role mismatch. You cannot log into " + expectedRole
                    + " portal with an account registered as " + user.getRole() + ".");
        }

        // Verify password hash using SHA-256 + salt
        boolean matches = PasswordUtil.verifyPassword(password, user.getPasswordHash());
        if (!matches) {
            throw new IllegalArgumentException("Invalid credentials. Incorrect password.");
        }

        // Authentication successful: create session
        String token = UUID.randomUUID().toString();
        Session session = new Session(token, user);
        activeSessions.put(token, session);

        System.out.println("[AuthService] User '" + username + "' (" + user.getRole() + ") logged in successfully. Token: " + token);
        return session;
    }

    /**
     * Invalidates an existing session on logout.
     */
    public boolean logout(String token) {
        if (token != null) {
            Session removed = activeSessions.remove(token);
            if (removed != null) {
                System.out.println("[AuthService] Session invalidated for user '" + removed.getUser().getUsername() + "'");
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves and updates an active session from token. Returns null if invalid or expired.
     */
    public Session getSession(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        Session session = activeSessions.get(token.trim());
        if (session != null) {
            if (session.isExpired()) {
                activeSessions.remove(token.trim());
                return null;
            }
            session.updateAccess();
            return session;
        }
        return null;
    }

    /**
     * Extracts session from Bearer token or cookie/custom header string.
     */
    public Session authenticateHeader(String authHeader) {
        if (authHeader == null) return null;
        String token = authHeader.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        return getSession(token);
    }
}
