package model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Model representing an active course enrollment record binding a student to a course.
 */
public class Enrollment {
    private String enrollmentId;
    private String studentId;     // Student's username
    private String studentName;   // Cached student display name
    private String courseId;      // Enrolled course ID
    private String courseName;    // Cached course title
    private int credits;          // Cached course credit weight
    private String enrolledAt;    // ISO timestamp or readable date

    public Enrollment() {}

    public Enrollment(String enrollmentId, String studentId, String studentName,
                      String courseId, String courseName, int credits, String enrolledAt) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.courseId = courseId;
        this.courseName = courseName;
        this.credits = credits;
        this.enrolledAt = enrolledAt;
    }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(String enrolledAt) { this.enrolledAt = enrolledAt; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enrollmentId", enrollmentId);
        map.put("studentId", studentId);
        map.put("studentName", studentName);
        map.put("courseId", courseId);
        map.put("courseName", courseName);
        map.put("credits", credits);
        map.put("enrolledAt", enrolledAt);
        return map;
    }

    public static Enrollment fromMap(Map<String, Object> map) {
        if (map == null) return null;
        Enrollment e = new Enrollment();
        e.setEnrollmentId((String) map.get("enrollmentId"));
        e.setStudentId((String) map.get("studentId"));
        e.setStudentName((String) map.get("studentName"));
        e.setCourseId((String) map.get("courseId"));
        e.setCourseName((String) map.get("courseName"));
        Object cr = map.get("credits");
        e.setCredits(cr instanceof Number ? ((Number) cr).intValue() : 0);
        e.setEnrolledAt((String) map.get("enrolledAt"));
        return e;
    }
}
