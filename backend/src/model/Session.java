package model;

/**
 * Model representing an authenticated user session in memory.
 */
public class Session {
    private final String token;
    private final User user;
    private final long createdAt;
    private long lastAccessedAt;

    private static final long SESSION_TIMEOUT_MS = 24 * 60 * 60 * 1000L; // 24 hours

    public Session(String token, User user) {
        this.token = token;
        this.user = user;
        this.createdAt = System.currentTimeMillis();
        this.lastAccessedAt = this.createdAt;
    }

    public String getToken() { return token; }
    public User getUser() { return user; }
    public long getCreatedAt() { return createdAt; }
    public long getLastAccessedAt() { return lastAccessedAt; }

    public void updateAccess() {
        this.lastAccessedAt = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - lastAccessedAt) > SESSION_TIMEOUT_MS;
    }
}
