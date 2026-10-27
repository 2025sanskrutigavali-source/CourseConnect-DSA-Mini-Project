package service;

import model.Course;
import model.Enrollment;
import model.User;
import repository.DataStore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service managing course enrollments, drops, and academic constraint enforcement.
 *
 * DSA IMPLEMENTATION DETAILS:
 * - C. HashSet (enrollmentIndex):
 *      Used for O(1) instantaneous check for duplicate enrollments via composite keys: "studentId#courseId".
 * - A. HashMap (courseMap):
 *      Used for O(1) retrieval and atomic updates to course capacity and seat tallies.
 * - B. ArrayList (enrollments):
 *      Used for maintaining ordered audit trails of enrollments and filtering student rosters.
 *
 * THREAD SAFETY:
 * Critical enrollment and drop methods are strictly synchronized on ENROLLMENT_MUTEX to guarantee
 * that simultaneous race conditions cannot oversubscribe course capacity.
 */
public class EnrollmentService {
    public static final int MAX_CREDIT_CAP = 18; // Maximum semester credit limit per student

    private final DataStore dataStore;
    private static final Object ENROLLMENT_MUTEX = new Object();

    public EnrollmentService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Checks if a student is already enrolled in a course in O(1) time using HashSet composite key.
     */
    public boolean isEnrolled(String studentId, String courseId) {
        if (studentId == null || courseId == null) return false;
        String key = studentId.trim() + "#" + courseId.trim();
        return dataStore.getEnrollmentIndex().contains(key);
    }

    /**
     * Calculates the total enrolled credits for a student.
     */
    public int calculateEnrolledCredits(String studentId) {
        if (studentId == null) return 0;
        int sum = 0;
        synchronized (dataStore.getEnrollments()) {
            for (Enrollment e : dataStore.getEnrollments()) {
                if (e.getStudentId().equalsIgnoreCase(studentId.trim())) {
                    sum += e.getCredits();
                }
            }
        }
        return sum;
    }

    /**
     * Retrieves all active enrollments for a specific student.
     */
    public List<Enrollment> getStudentEnrollments(String studentId) {
        List<Enrollment> result = new ArrayList<>();
        if (studentId == null) return result;
        synchronized (dataStore.getEnrollments()) {
            for (Enrollment e : dataStore.getEnrollments()) {
                if (e.getStudentId().equalsIgnoreCase(studentId.trim())) {
                    result.add(e);
                }
            }
        }
        return result;
    }

    /**
     * Retrieves set of course IDs the student is currently enrolled in (for badge rendering in catalogue).
     */
    public Set<String> getStudentEnrolledCourseIds(String studentId) {
        Set<String> set = new HashSet<>();
        if (studentId == null) return set;
        synchronized (dataStore.getEnrollments()) {
            for (Enrollment e : dataStore.getEnrollments()) {
                if (e.getStudentId().equalsIgnoreCase(studentId.trim())) {
                    set.add(e.getCourseId());
                }
            }
        }
        return set;
    }

    /**
     * Performs synchronized course enrollment with comprehensive rule enforcement.
     *
     * Enrollment Rules enforced:
     * 1. Course must exist (HashMap lookup O(1)).
     * 2. Student cannot enroll in same course twice (HashSet composite key check O(1)).
     * 3. Course must have available seats (enrolledCount < capacity).
     * 4. Credit cap check: Enrolling must not exceed 18 total credits.
     * 5. Atomically increments course enrollment count and decrements available seats.
     * 6. Appends enrollment record and persists data.
     */
    public Enrollment enrollStudent(User student, String courseId) {
        if (student == null || !student.isStudent()) {
            throw new IllegalArgumentException("Only authenticated student accounts can enroll in courses.");
        }
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID cannot be empty.");
        }

        courseId = courseId.trim();

        // Critical Synchronization block: prevents concurrent over-enrollment
        synchronized (ENROLLMENT_MUTEX) {
            // Rule 1: Course existence check (HashMap O(1))
            Course course = dataStore.getCourseMap().get(courseId);
            if (course == null) {
                throw new IllegalArgumentException("Cannot enroll: Course '" + courseId + "' does not exist.");
            }

            // Rule 2: Duplicate enrollment check (HashSet O(1))
            String key = student.getUsername() + "#" + courseId;
            if (dataStore.getEnrollmentIndex().contains(key)) {
                throw new IllegalArgumentException("Duplicate Enrollment: You are already enrolled in "
                        + course.getCourseId() + " (" + course.getCourseName() + ").");
            }

            // Rule 3: Capacity constraint check
            if (course.isFull() || course.getAvailableSeats() <= 0) {
                throw new IllegalArgumentException("Enrollment Closed: " + course.getCourseId()
                        + " has reached maximum capacity (" + course.getCapacity() + " seats filled).");
            }

            // Rule 4: Maximum credit cap constraint (18 credits)
            int currentCredits = calculateEnrolledCredits(student.getUsername());
            int newTotalCredits = currentCredits + course.getCredits();
            if (newTotalCredits > MAX_CREDIT_CAP) {
                throw new IllegalArgumentException("Credit Limit Exceeded: Adding " + course.getCourseId()
                        + " (" + course.getCredits() + " credits) would bring your load to " + newTotalCredits
                        + " credits. The maximum semester limit is " + MAX_CREDIT_CAP + " credits. (Current: "
                        + currentCredits + " credits).");
            }

            // Rule 5: Update course enrollment count
            course.setEnrolledCount(course.getEnrolledCount() + 1);

            // Rule 6: Create and record enrollment
            String enrollmentId = UUID.randomUUID().toString();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            Enrollment enrollment = new Enrollment(
                    enrollmentId,
                    student.getUsername(),
                    student.getFullName(),
                    course.getCourseId(),
                    course.getCourseName(),
                    course.getCredits(),
                    timestamp
            );

            dataStore.getEnrollments().add(enrollment);
            dataStore.getEnrollmentIndex().add(key);

            // Persist updates
            dataStore.saveCourses();
            dataStore.saveEnrollments();

            System.out.println("[EnrollmentService] Student '" + student.getUsername()
                    + "' enrolled in " + courseId + ". Seats remaining: " + course.getAvailableSeats());
            return enrollment;
        }
    }

    /**
     * Performs synchronized course drop with seat release.
     *
     * Drop Rules enforced:
     * 1. Student must be currently enrolled in the course.
     * 2. Atomically decrements course enrollment count (never below 0).
     * 3. Removes enrollment record from ArrayList and HashSet.
     * 4. Persists updates to disk.
     */
    public boolean dropStudentCourse(User student, String courseId) {
        if (student == null || !student.isStudent()) {
            throw new IllegalArgumentException("Only authenticated student accounts can drop courses.");
        }
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID cannot be empty.");
        }

        courseId = courseId.trim();

        synchronized (ENROLLMENT_MUTEX) {
            String key = student.getUsername() + "#" + courseId;

            // Rule 1: Check enrollment existence
            if (!dataStore.getEnrollmentIndex().contains(key)) {
                throw new IllegalArgumentException("Cannot drop: You are not currently enrolled in course '" + courseId + "'.");
            }

            // Remove from ArrayList
            Enrollment removedRecord = null;
            synchronized (dataStore.getEnrollments()) {
                Iterator<Enrollment> it = dataStore.getEnrollments().iterator();
                while (it.hasNext()) {
                    Enrollment e = it.next();
                    if (e.getStudentId().equalsIgnoreCase(student.getUsername()) &&
                            e.getCourseId().equalsIgnoreCase(courseId)) {
                        removedRecord = e;
                        it.remove();
                        break;
                    }
                }
            }

            // Remove from HashSet
            dataStore.getEnrollmentIndex().remove(key);

            // Release seat in Course
            Course course = dataStore.getCourseMap().get(courseId);
            if (course != null) {
                int count = Math.max(0, course.getEnrolledCount() - 1);
                course.setEnrolledCount(count);
                dataStore.saveCourses();
            }

            dataStore.saveEnrollments();

            System.out.println("[EnrollmentService] Student '" + student.getUsername()
                    + "' dropped " + courseId + ". Available seats updated.");
            return true;
        }
    }

    /**
     * Institution view: Query and filter all institution enrollment records.
     */
    public List<Enrollment> getAllEnrollments(String search, String courseId) {
        List<Enrollment> result = new ArrayList<>();
        String query = (search != null) ? search.trim().toLowerCase() : "";
        String cIdFilter = (courseId != null && !courseId.trim().equalsIgnoreCase("ALL"))
                ? courseId.trim().toLowerCase() : null;

        synchronized (dataStore.getEnrollments()) {
            for (Enrollment e : dataStore.getEnrollments()) {
                if (!query.isEmpty()) {
                    boolean studentIdMatches = e.getStudentId().toLowerCase().contains(query);
                    boolean studentNameMatches = e.getStudentName() != null && e.getStudentName().toLowerCase().contains(query);
                    boolean courseIdMatches = e.getCourseId().toLowerCase().contains(query);
                    boolean courseNameMatches = e.getCourseName() != null && e.getCourseName().toLowerCase().contains(query);
                    if (!studentIdMatches && !studentNameMatches && !courseIdMatches && !courseNameMatches) {
                        continue;
                    }
                }

                if (cIdFilter != null && !cIdFilter.isEmpty()) {
                    if (!e.getCourseId().toLowerCase().equals(cIdFilter)) {
                        continue;
                    }
                }

                result.add(e);
            }
        }

        // Sort latest enrollments first
        result.sort((e1, e2) -> {
            if (e1.getEnrolledAt() == null || e2.getEnrolledAt() == null) return 0;
            return e2.getEnrolledAt().compareTo(e1.getEnrolledAt());
        });

        return result;
    }
}
