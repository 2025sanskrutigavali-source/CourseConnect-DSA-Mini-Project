import model.Course;
import model.Enrollment;
import model.Session;
import model.User;
import repository.DataStore;
import service.AdminService;
import service.AuthService;
import service.CourseService;
import service.EnrollmentService;
import util.PasswordUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Comprehensive Automated Verification Suite for CourseConnect.
 * Evaluates all 20 test cases and requirements specified in the project specification.
 */
public class TestRunner {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("   CourseConnect - Automated Test & DSA Verification Suite        ");
        System.out.println("==================================================================");

        DataStore dataStore = new DataStore();
        AuthService authService = new AuthService(dataStore);
        CourseService courseService = new CourseService(dataStore);
        EnrollmentService enrollmentService = new EnrollmentService(dataStore);
        AdminService adminService = new AdminService(dataStore);

        // Test 1: Valid student login
        try {
            Session s = authService.login("student01", "student123", "STUDENT");
            assertCondition(s != null && s.getUser().isStudent(), "Test 1: Valid student login");
        } catch (Exception e) {
            fail("Test 1: Valid student login: " + e.getMessage());
        }

        // Test 2: Valid institution login
        try {
            Session s = authService.login("admin01", "admin123", "INSTITUTION");
            assertCondition(s != null && s.getUser().isInstitution(), "Test 2: Valid institution login");
        } catch (Exception e) {
            fail("Test 2: Valid institution login: " + e.getMessage());
        }

        // Test 3: Invalid password rejection
        try {
            authService.login("student01", "wrongpassword", "STUDENT");
            fail("Test 3: Invalid password should have been rejected");
        } catch (IllegalArgumentException e) {
            assertCondition(e.getMessage().contains("password") || e.getMessage().contains("credentials"),
                    "Test 3: Invalid password rejected with useful error: " + e.getMessage());
        }

        // Test 4: Role Mismatch protection (Student cannot login as Institution)
        try {
            authService.login("student01", "student123", "INSTITUTION");
            fail("Test 4: Student account logging into Institution role should fail");
        } catch (IllegalArgumentException e) {
            assertCondition(e.getMessage().contains("mismatch") || e.getMessage().contains("Role"),
                    "Test 4: Role protection prevents cross-role login: " + e.getMessage());
        }

        // Test 5: Search by course name
        try {
            List<Course> found = courseService.queryCourses("Data Structures", null, null, null);
            boolean ok = found.stream().anyMatch(c -> c.getCourseId().equals("CS-201"));
            assertCondition(ok, "Test 5: Search by course name finds CS-201");
        } catch (Exception e) {
            fail("Test 5: Search by course name failed: " + e.getMessage());
        }

        // Test 6: Search by course ID
        try {
            List<Course> found = courseService.queryCourses("CS-250", null, null, null);
            boolean ok = found.size() == 1 && found.get(0).getCourseId().equals("CS-250");
            assertCondition(ok, "Test 6: Search by course ID finds exact match CS-250");
        } catch (Exception e) {
            fail("Test 6: Search by course ID failed: " + e.getMessage());
        }

        // Test 7: Successful enrollment in open course
        User student02 = dataStore.getUserMap().get("student02");
        try {
            // student02 enrolls in CS-250
            int initialSeats = courseService.getCourseById("CS-250").getAvailableSeats();
            Enrollment enr = enrollmentService.enrollStudent(student02, "CS-250");
            int newSeats = courseService.getCourseById("CS-250").getAvailableSeats();
            assertCondition(enr != null && newSeats == initialSeats - 1,
                    "Test 7: Successful enrollment decrements available seats by 1");
        } catch (Exception e) {
            fail("Test 7: Successful enrollment failed: " + e.getMessage());
        }

        // Test 8: Duplicate enrollment rejection (HashSet composite key check)
        try {
            enrollmentService.enrollStudent(student02, "CS-250");
            fail("Test 8: Duplicate enrollment should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertCondition(e.getMessage().contains("Duplicate") || e.getMessage().contains("already enrolled"),
                    "Test 8: Duplicate enrollment rejected in O(1) via HashSet: " + e.getMessage());
        }

        // Test 9: Enrollment rejection when course is full
        try {
            Course fullCourse = courseService.getCourseById("CS-305"); // Cap 25, Enrolled 25
            enrollmentService.enrollStudent(student02, "CS-305");
            fail("Test 9: Enrolling in full course CS-305 should have been rejected");
        } catch (IllegalArgumentException e) {
            assertCondition(e.getMessage().contains("capacity") || e.getMessage().contains("Closed") || e.getMessage().contains("full"),
                    "Test 9: Full course enrollment rejected: " + e.getMessage());
        }

        // Test 10: Successful course drop releases seat
        try {
            int seatsBeforeDrop = courseService.getCourseById("CS-250").getAvailableSeats();
            boolean dropped = enrollmentService.dropStudentCourse(student02, "CS-250");
            int seatsAfterDrop = courseService.getCourseById("CS-250").getAvailableSeats();
            assertCondition(dropped && seatsAfterDrop == seatsBeforeDrop + 1,
                    "Test 10: Dropping course releases seat and decrements enrolledCount");
        } catch (Exception e) {
            fail("Test 10: Successful course drop failed: " + e.getMessage());
        }

        // Test 11: Invalid course ID enrollment rejection
        try {
            enrollmentService.enrollStudent(student02, "NON-EXISTENT-999");
            fail("Test 11: Nonexistent course enrollment should fail");
        } catch (IllegalArgumentException e) {
            assertCondition(e.getMessage().contains("does not exist"),
                    "Test 11: Nonexistent course rejected: " + e.getMessage());
        }

        // Test 12: Institution adds a course
        try {
            Course newCourse = new Course("CS-499", "Senior Capstone Design", "Computer Science",
                    "Prof. Claude Shannon", 4, 20, 0,
                    "Comprehensive capstone project integrating system architecture.",
                    "Thu 03:00 PM - 06:00 PM", "Turing 105");
            courseService.addCourse(newCourse);
            Course retrieved = courseService.getCourseById("CS-499");
            assertCondition(retrieved != null && retrieved.getCapacity() == 20,
                    "Test 12: Institution adds new course CS-499 to HashMap store");
        } catch (Exception e) {
            fail("Test 12: Institution adds course failed: " + e.getMessage());
        }

        // Test 13: Institution edits an existing course
        try {
            Course c = courseService.getCourseById("CS-499");
            c.setCourseName("Senior Capstone Design & Thesis");
            c.setCapacity(25);
            courseService.updateCourse("CS-499", c);
            Course updated = courseService.getCourseById("CS-499");
            assertCondition(updated.getCourseName().contains("Thesis") && updated.getCapacity() == 25,
                    "Test 13: Institution updates course name and capacity successfully");
        } catch (Exception e) {
            fail("Test 13: Institution edits course failed: " + e.getMessage());
        }

        // Test 14: Invalid course input validation
        try {
            Course invalidCourse = new Course("BAD-1", "", "Computer Science", "Prof", 0, -5, 0, "", "", "");
            courseService.addCourse(invalidCourse);
            fail("Test 14: Invalid course input with 0 credits and -5 capacity should fail");
        } catch (IllegalArgumentException e) {
            assertCondition(true, "Test 14: Backend validation properly rejects invalid course inputs: " + e.getMessage());
        }

        // Test 15: Institution deletes a course with no enrollments
        try {
            courseService.deleteCourse("CS-499");
            assertCondition(courseService.getCourseById("CS-499") == null,
                    "Test 15: Institution deletes course with 0 enrollments successfully");
        } catch (Exception e) {
            fail("Test 15: Delete course with no enrollments failed: " + e.getMessage());
        }

        // Test 16: Deletion of course with active enrollments is strictly prevented
        try {
            courseService.deleteCourse("CS-101"); // Has enrollments
            fail("Test 16: Course with active enrollments should not be deleted");
        } catch (IllegalArgumentException e) {
            assertCondition(e.getMessage().contains("enrolled") || e.getMessage().contains("enrollments"),
                    "Test 16: Course deletion prevented when active enrollments exist: " + e.getMessage());
        }

        // Test 17: Capacity cannot be set below existing active enrollments
        try {
            Course cs101 = courseService.getCourseById("CS-101");
            int enrolled = cs101.getEnrolledCount();
            Course modified = new Course(cs101.getCourseId(), cs101.getCourseName(), cs101.getDepartment(),
                    cs101.getInstructor(), cs101.getCredits(), enrolled - 1, enrolled,
                    cs101.getDescription(), cs101.getSchedule(), cs101.getLocation());
            courseService.updateCourse("CS-101", modified);
            fail("Test 17: Setting capacity below current enrollments should fail");
        } catch (IllegalArgumentException e) {
            assertCondition(e.getMessage().contains("Cannot reduce capacity"),
                    "Test 17: Capacity reduction below current enrolled count blocked: " + e.getMessage());
        }

        // Test 18: Data persistence preserves records across restarts
        try {
            DataStore freshStore = new DataStore();
            freshStore.loadAll();
            boolean hasCourses = freshStore.getCourseMap().size() >= 10;
            boolean hasUsers = freshStore.getUserMap().containsKey("student01");
            boolean hasEnrollments = !freshStore.getEnrollments().isEmpty();
            assertCondition(hasCourses && hasUsers && hasEnrollments,
                    "Test 18: Stored data files successfully survive and reload from disk");
        } catch (Exception e) {
            fail("Test 18: Data persistence reload failed: " + e.getMessage());
        }

        // Test 19: Session Logout invalidation
        try {
            Session s = authService.login("student01", "student123", "STUDENT");
            String token = s.getToken();
            assertCondition(authService.getSession(token) != null, "Test 19a: Session is active before logout");
            authService.logout(token);
            assertCondition(authService.getSession(token) == null, "Test 19b: Logout invalidates session token");
        } catch (Exception e) {
            fail("Test 19: Logout invalidation failed: " + e.getMessage());
        }

        // Test 20: Credit Cap Limit Enforcement (Max 18 Credits)
        try {
            // Check student01 credits
            User student01 = dataStore.getUserMap().get("student01");
            int currentCreds = enrollmentService.calculateEnrolledCredits("student01");
            // Enroll in remaining available courses until 18 credits reached
            // Let's test that enrolling beyond 18 credits is rejected
            // E.g. CS-320 (3 cr), EE-102 (4 cr), AI-301 (4 cr)
            try {
                enrollmentService.enrollStudent(student01, "CS-320"); // 11 + 3 = 14
                enrollmentService.enrollStudent(student01, "EE-102");  // 14 + 4 = 18 (Exact max!)
                // Now try adding AI-301 (4 cr) -> would be 22 cr -> should fail!
                enrollmentService.enrollStudent(student01, "AI-301");
                fail("Test 20: Enrolling beyond 18 credits should have been rejected");
            } catch (IllegalArgumentException e) {
                assertCondition(e.getMessage().contains("Credit Limit Exceeded") || e.getMessage().contains("18 credits"),
                        "Test 20: Maximum credit cap (18 credits) enforced: " + e.getMessage());
            } finally {
                // Clean up extra enrollments
                try { enrollmentService.dropStudentCourse(student01, "CS-320"); } catch (Exception ignored) {}
                try { enrollmentService.dropStudentCourse(student01, "EE-102"); } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            fail("Test 20: Credit cap test failed: " + e.getMessage());
        }

        // Bonus Test 21: Thread-Safety / Concurrency test on Seat Capacity
        try {
            System.out.println("[*] Running Concurrency Test: 20 simultaneous threads racing for 2 remaining seats...");
            String raceCourseId = "RACE-" + (System.currentTimeMillis() % 100000);
            Course raceCourse = new Course(raceCourseId, "Concurrency Test Lab", "Computer Science",
                    "Prof. Dijkstra", 3, 2, 0, "Test course", "TBA", "Lab 1");
            courseService.addCourse(raceCourse);

            int numThreads = 10;
            ExecutorService exec = Executors.newFixedThreadPool(numThreads);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(numThreads);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger rejectCount = new AtomicInteger(0);

            for (int i = 0; i < numThreads; i++) {
                final int idx = i;
                User tempStudent = new User("race_student_" + idx, PasswordUtil.hashPassword("pwd"),
                        "STUDENT", "Student " + idx, "s" + idx + "@test.edu", "CS", "1st Year");
                dataStore.getUserMap().put(tempStudent.getUsername(), tempStudent);

                exec.submit(() -> {
                    try {
                        startLatch.await(); // Wait for all threads to synchronize start
                        enrollmentService.enrollStudent(tempStudent, raceCourseId);
                        successCount.incrementAndGet();
                    } catch (IllegalArgumentException e) {
                        rejectCount.incrementAndGet();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        endLatch.countDown();
                    }
                });
            }

            startLatch.countDown(); // Fire simultaneous race
            endLatch.await();
            exec.shutdown();

            Course finalCourse = courseService.getCourseById(raceCourseId);
            assertCondition(finalCourse.getEnrolledCount() == 2 && successCount.get() == 2 && rejectCount.get() == 8,
                    "Test 21 (Concurrency): Exactly 2 enrollments succeeded, 8 rejected, enrolledCount = 2 / capacity = 2");

            // Clean up: drop enrollments first, then delete course
            for (int i = 0; i < numThreads; i++) {
                User u = dataStore.getUserMap().get("race_student_" + i);
                if (u != null) {
                    try { enrollmentService.dropStudentCourse(u, raceCourseId); } catch (Exception ignored) {}
                    dataStore.getUserMap().remove("race_student_" + i);
                }
            }
            courseService.deleteCourse(raceCourseId);
        } catch (Exception e) {
            fail("Test 21 (Concurrency) failed: " + e.getMessage());
        }

        System.out.println("==================================================================");
        System.out.println(" TEST RESULTS: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("==================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertCondition(boolean condition, String message) {
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + message);
        } else {
            failed++;
            System.err.println("  [FAIL] " + message);
        }
    }

    private static void fail(String message) {
        failed++;
        System.err.println("  [FAIL] " + message);
    }
}
