package model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Model representing a university course offering in CourseConnect.
 */
public class Course {
    private String courseId;     // e.g. "CS-101" (unique key)
    private String courseName;   // e.g. "Data Structures and Algorithms"
    private String department;   // e.g. "Computer Science"
    private String instructor;   // e.g. "Dr. Aris Thorne"
    private int credits;         // e.g. 4
    private int capacity;        // e.g. 35 (max students allowed)
    private int enrolledCount;   // e.g. 25
    private String description;  // Detailed course overview
    private String schedule;     // e.g. "Mon & Wed 10:00 - 11:30 AM"
    private String location;     // e.g. "Turing Hall, Room 104"

    public Course() {}

    public Course(String courseId, String courseName, String department, String instructor,
                  int credits, int capacity, int enrolledCount, String description,
                  String schedule, String location) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.department = department;
        this.instructor = instructor;
        this.credits = credits;
        this.capacity = capacity;
        this.enrolledCount = enrolledCount;
        this.description = description;
        this.schedule = schedule;
        this.location = location;
    }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(int enrolledCount) { this.enrolledCount = enrolledCount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getAvailableSeats() {
        return Math.max(0, capacity - enrolledCount);
    }

    public boolean isFull() {
        return enrolledCount >= capacity;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("courseId", courseId);
        map.put("courseName", courseName);
        map.put("department", department);
        map.put("instructor", instructor);
        map.put("credits", credits);
        map.put("capacity", capacity);
        map.put("enrolledCount", enrolledCount);
        map.put("availableSeats", getAvailableSeats());
        map.put("isFull", isFull());
        map.put("description", description);
        map.put("schedule", schedule);
        map.put("location", location);
        return map;
    }

    public static Course fromMap(Map<String, Object> map) {
        if (map == null) return null;
        Course c = new Course();
        c.setCourseId((String) map.get("courseId"));
        c.setCourseName((String) map.get("courseName"));
        c.setDepartment((String) map.get("department"));
        c.setInstructor((String) map.get("instructor"));
        
        Object cr = map.get("credits");
        c.setCredits(cr instanceof Number ? ((Number) cr).intValue() : 0);

        Object cap = map.get("capacity");
        c.setCapacity(cap instanceof Number ? ((Number) cap).intValue() : 0);

        Object enr = map.get("enrolledCount");
        c.setEnrolledCount(enr instanceof Number ? ((Number) enr).intValue() : 0);

        c.setDescription((String) map.get("description"));
        c.setSchedule((String) map.get("schedule"));
        c.setLocation((String) map.get("location"));
        return c;
    }
}
