package model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Model representing a registered application user (Student or Institution Administrator).
 */
public class User {
    private String username;
    private String passwordHash; // Format "salt:hash", never plaintext
    private String role;         // "STUDENT" or "INSTITUTION"
    private String fullName;
    private String email;
    private String department;
    private String academicYear; // e.g. "3rd Year" (null for institution)

    public User() {}

    public User(String username, String passwordHash, String role, String fullName,
                String email, String department, String academicYear) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.academicYear = academicYear;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public boolean isStudent() {
        return "STUDENT".equalsIgnoreCase(role);
    }

    public boolean isInstitution() {
        return "INSTITUTION".equalsIgnoreCase(role);
    }

    /**
     * Converts to Map for JSON serialization.
     * Note: passwordHash is excluded when sending user profile to frontend.
     */
    public Map<String, Object> toMap(boolean includePassword) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("username", username);
        if (includePassword) {
            map.put("passwordHash", passwordHash);
        }
        map.put("role", role);
        map.put("fullName", fullName);
        map.put("email", email);
        map.put("department", department);
        map.put("academicYear", academicYear);
        return map;
    }

    public static User fromMap(Map<String, Object> map) {
        if (map == null) return null;
        User u = new User();
        u.setUsername((String) map.get("username"));
        u.setPasswordHash((String) map.get("passwordHash"));
        u.setRole((String) map.get("role"));
        u.setFullName((String) map.get("fullName"));
        u.setEmail((String) map.get("email"));
        u.setDepartment((String) map.get("department"));
        u.setAcademicYear((String) map.get("academicYear"));
        return u;
    }
}
