package service;

import model.Course;
import repository.DataStore;

import java.util.*;

/**
 * Service handling Course domain operations, querying, searching, sorting, and administrative lifecycle.
 *
 * DSA IMPLEMENTATION DETAILS:
 * - A. HashMap (courseMap):
 *      Used for O(1) instantaneous lookup, addition, updating, and deletion of courses by courseId.
 * - B. ArrayList:
 *      Used to maintain sequential lists of courses for display, filtering, and sorting.
 * - D. Searching:
 *      Linear scan O(N) over catalogue evaluating case-insensitive substring matching against
 *      Course ID and Course Title, with department and credit predicate filtering.
 * - E. Sorting:
 *      Custom Comparator implementations executing O(N log N) TimSort (adaptive merge sort)
 *      across Course Title, Course ID, Credit Value, or Available Capacity.
 */
public class CourseService {
    private final DataStore dataStore;

    public CourseService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * O(1) Course Lookup using HashMap.
     */
    public Course getCourseById(String courseId) {
        if (courseId == null) return null;
        return dataStore.getCourseMap().get(courseId.trim());
    }

    /**
     * Retrieves all courses as an ArrayList.
     */
    public List<Course> getAllCourses() {
        return new ArrayList<>(dataStore.getCourseMap().values());
    }

    /**
     * Filters, searches, and sorts courses.
     *
     * DSA Complexity Analysis:
     * - Filtering & Searching: O(N) where N is the number of total courses.
     * - Sorting: O(K log K) where K is the number of filtered candidate courses (TimSort/MergeSort).
     * - Overall Time Complexity: O(N + K log K) <= O(N log N).
     * - Space Complexity: O(K) for the filtered ArrayList.
     */
    public List<Course> queryCourses(String search, String department, Integer credits, String sortBy) {
        List<Course> result = new ArrayList<>();
        String query = (search != null) ? search.trim().toLowerCase() : "";
        String deptFilter = (department != null && !department.trim().equalsIgnoreCase("ALL"))
                ? department.trim().toLowerCase() : null;

        // Step 1: Linear Search and Predicate Filtering
        for (Course course : dataStore.getCourseMap().values()) {
            // Check text query in Course ID or Course Name
            if (!query.isEmpty()) {
                boolean idMatches = course.getCourseId().toLowerCase().contains(query);
                boolean nameMatches = course.getCourseName().toLowerCase().contains(query);
                boolean instructorMatches = course.getInstructor() != null &&
                        course.getInstructor().toLowerCase().contains(query);
                if (!idMatches && !nameMatches && !instructorMatches) {
                    continue;
                }
            }

            // Check department match
            if (deptFilter != null && !deptFilter.isEmpty()) {
                if (!course.getDepartment().toLowerCase().contains(deptFilter)) {
                    continue;
                }
            }

            // Check credits match
            if (credits != null && credits > 0) {
                if (course.getCredits() != credits) {
                    continue;
                }
            }

            result.add(course);
        }

        // Step 2: Sorting via Custom Comparators
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            Comparator<Course> comparator = getComparator(sortBy.trim().toLowerCase());
            if (comparator != null) {
                result.sort(comparator);
            }
        } else {
            // Default sort: Course ID ascending
            result.sort(Comparator.comparing(Course::getCourseId));
        }

        return result;
    }

    /**
     * Factory for Course Comparators.
     * Demonstrates DSA sorting criteria in Java.
     */
    private Comparator<Course> getComparator(String sortKey) {
        switch (sortKey) {
            case "name_asc":
            case "name":
                return (c1, c2) -> c1.getCourseName().compareToIgnoreCase(c2.getCourseName());
            case "name_desc":
                return (c1, c2) -> c2.getCourseName().compareToIgnoreCase(c1.getCourseName());
            case "id_asc":
            case "id":
                return (c1, c2) -> c1.getCourseId().compareToIgnoreCase(c2.getCourseId());
            case "id_desc":
                return (c1, c2) -> c2.getCourseId().compareToIgnoreCase(c1.getCourseId());
            case "credits_asc":
                return Comparator.comparingInt(Course::getCredits);
            case "credits_desc":
                return (c1, c2) -> Integer.compare(c2.getCredits(), c1.getCredits());
            case "seats_desc":
                return (c1, c2) -> Integer.compare(c2.getAvailableSeats(), c1.getAvailableSeats());
            case "seats_asc":
                return Comparator.comparingInt(Course::getAvailableSeats);
            default:
                return Comparator.comparing(Course::getCourseId);
        }
    }

    // ==========================================
    // Course Management (Institution Privileged)
    // ==========================================

    /**
     * Adds a new course offering.
     * Validates uniqueness via HashMap O(1) lookup and verifies data constraints.
     */
    public synchronized Course addCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course payload cannot be empty.");
        }
        String id = course.getCourseId();
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID is required (e.g. 'CS-401').");
        }
        id = id.trim().toUpperCase();
        course.setCourseId(id);

        // O(1) duplicate check in HashMap
        if (dataStore.getCourseMap().containsKey(id)) {
            throw new IllegalArgumentException("A course with ID '" + id + "' already exists in the catalogue.");
        }

        validateCourseFields(course);

        course.setEnrolledCount(0); // Brand new course starts with 0 enrollments
        dataStore.getCourseMap().put(id, course);
        dataStore.saveCourses();
        System.out.println("[CourseService] Created new course: " + id + " - " + course.getCourseName());
        return course;
    }

    /**
     * Updates an existing course offering.
     * Rule: Capacity cannot be set below current enrolled count!
     */
    public synchronized Course updateCourse(String courseId, Course updated) {
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID is required.");
        }
        Course existing = getCourseById(courseId.trim());
        if (existing == null) {
            throw new IllegalArgumentException("Course '" + courseId + "' does not exist.");
        }

        validateCourseFields(updated);

        // Rule: Capacity cannot be reduced below current active student enrollments
        if (updated.getCapacity() < existing.getEnrolledCount()) {
            throw new IllegalArgumentException("Cannot reduce capacity to " + updated.getCapacity()
                    + ". There are currently " + existing.getEnrolledCount() + " students actively enrolled.");
        }

        existing.setCourseName(updated.getCourseName());
        existing.setDepartment(updated.getDepartment());
        existing.setInstructor(updated.getInstructor());
        existing.setCredits(updated.getCredits());
        existing.setCapacity(updated.getCapacity());
        existing.setDescription(updated.getDescription());
        if (updated.getSchedule() != null && !updated.getSchedule().trim().isEmpty()) {
            existing.setSchedule(updated.getSchedule());
        }
        if (updated.getLocation() != null && !updated.getLocation().trim().isEmpty()) {
            existing.setLocation(updated.getLocation());
        }

        dataStore.saveCourses();
        System.out.println("[CourseService] Updated course: " + courseId);
        return existing;
    }

    /**
     * Deletes a course.
     * Rule: Cannot delete a course with active enrollments!
     */
    public synchronized void deleteCourse(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID is required.");
        }
        Course existing = getCourseById(courseId.trim());
        if (existing == null) {
            throw new IllegalArgumentException("Course '" + courseId + "' does not exist.");
        }

        // Rule: Active enrollments guard
        if (existing.getEnrolledCount() > 0) {
            throw new IllegalArgumentException("Cannot delete course '" + courseId
                    + "' because it currently has " + existing.getEnrolledCount()
                    + " enrolled students. Enrollments must be dropped first.");
        }

        dataStore.getCourseMap().remove(existing.getCourseId());
        dataStore.saveCourses();
        System.out.println("[CourseService] Deleted course: " + courseId);
    }

    private void validateCourseFields(Course c) {
        if (c.getCourseName() == null || c.getCourseName().trim().isEmpty()) {
            throw new IllegalArgumentException("Course Name cannot be empty.");
        }
        if (c.getDepartment() == null || c.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Department cannot be empty.");
        }
        if (c.getInstructor() == null || c.getInstructor().trim().isEmpty()) {
            throw new IllegalArgumentException("Instructor name cannot be empty.");
        }
        if (c.getCredits() < 1 || c.getCredits() > 6) {
            throw new IllegalArgumentException("Credits must be a positive integer between 1 and 6.");
        }
        if (c.getCapacity() < 1) {
            throw new IllegalArgumentException("Maximum capacity must be at least 1 seat.");
        }
    }
}
