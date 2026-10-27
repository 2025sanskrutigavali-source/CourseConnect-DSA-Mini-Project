package repository;

import model.Course;
import model.Enrollment;
import model.User;
import util.JsonUtil;
import util.PasswordUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DataStore manages local JSON file persistence and in-memory data structures.
 * 
 * DSA IMPLEMENTATION:
 * - HashMap (courseMap): For O(1) instantaneous lookup, update, and deletion of courses by courseId.
 * - ArrayList (enrollments): For sequential collection and historical audit of student enrollments.
 * - HashSet (enrollmentIndex): For O(1) duplicate enrollment detection using composite keys (studentId + "#" + courseId).
 */
public class DataStore {
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.json";
    private static final String COURSES_FILE = DATA_DIR + File.separator + "courses.json";
    private static final String ENROLLMENTS_FILE = DATA_DIR + File.separator + "enrollments.json";

    // Primary In-Memory DSA Stores
    // 1. HashMap for O(1) Course Lookup
    private final Map<String, Course> courseMap = new ConcurrentHashMap<>();

    // 2. HashMap for O(1) User Lookup
    private final Map<String, User> userMap = new ConcurrentHashMap<>();

    // 3. ArrayList for sequential course enrollments
    private final List<Enrollment> enrollments = Collections.synchronizedList(new ArrayList<>());

    // 4. HashSet for O(1) Duplicate Enrollment Prevention
    // Composite key format: "studentId#courseId"
    private final Set<String> enrollmentIndex = Collections.synchronizedSet(new HashSet<>());

    private static final Object FILE_LOCK = new Object();

    public DataStore() {
        init();
    }

    private void init() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File uFile = new File(USERS_FILE);
        File cFile = new File(COURSES_FILE);
        File eFile = new File(ENROLLMENTS_FILE);

        // If any core file is missing, seed initial dataset
        if (!uFile.exists() || !cFile.exists() || !eFile.exists()) {
            seedDemoData();
        } else {
            loadAll();
        }
    }

    private void seedDemoData() {
        System.out.println("[DataStore] Initializing and seeding demo dataset...");

        // 1. Demo Users
        // Student 1
        User student01 = new User("student01",
                PasswordUtil.hashPassword("student123"),
                "STUDENT",
                "Alex Mercer",
                "alex.mercer@university.edu",
                "Computer Science & Eng.",
                "3rd Year");

        // Student 2
        User student02 = new User("student02",
                PasswordUtil.hashPassword("student123"),
                "STUDENT",
                "Sophia Chen",
                "sophia.chen@university.edu",
                "Electrical Engineering",
                "2nd Year");

        // Institution Admin
        User admin01 = new User("admin01",
                PasswordUtil.hashPassword("admin123"),
                "INSTITUTION",
                "Dr. Eleanor Vance",
                "registrar@university.edu",
                "Academic Registrar Office",
                "Faculty Staff");

        userMap.put(student01.getUsername(), student01);
        userMap.put(student02.getUsername(), student02);
        userMap.put(admin01.getUsername(), admin01);

        // 2. Demo Courses
        List<Course> demoCourses = Arrays.asList(
            new Course("CS-101", "Introduction to Computer Science", "Computer Science",
                    "Prof. Alan Turing", 4, 30, 28,
                    "Fundamental principles of algorithmic thinking, procedural abstraction, and object-oriented paradigms in Java.",
                    "Mon & Wed • 08:30 AM - 10:00 AM", "Turing Hall 101"),

            new Course("CS-201", "Data Structures and Algorithms", "Computer Science",
                    "Dr. Aris Thorne", 4, 35, 25,
                    "Rigorous examination of trees, graphs, heaps, dynamic programming, asymptotic complexity, and amortized analysis.",
                    "Mon & Wed • 10:00 AM - 11:30 AM", "Turing Hall 104"),

            new Course("CS-250", "Web Systems & Modern Architecture", "Computer Science",
                    "Marcus Vance", 3, 30, 18,
                    "Client-server protocols, stateless REST design, asynchronous event loops, reactive frontend frameworks, and security.",
                    "Fri • 09:00 AM - 12:00 PM (Lab)", "Hopper Center 210"),

            new Course("CS-305", "Operating Systems & Concurrency", "Computer Science",
                    "Prof. Elena Rostova", 4, 25, 25,
                    "Kernel structures, virtual memory management, CPU scheduling, POSIX multi-threading, deadlocks, and file systems.",
                    "Tue & Thu • 01:00 PM - 02:30 PM", "Lovelace Science Lab 302"),

            new Course("CS-320", "Database Management Systems", "Computer Science",
                    "Dr. Maya Lin", 3, 40, 20,
                    "Relational calculus, SQL query optimization, B-Tree indexing, ACID transactions, and write-ahead logging.",
                    "Tue & Thu • 10:30 AM - 12:00 PM", "Turing Hall 205"),

            new Course("MATH-201", "Linear Algebra & Discrete Structures", "Mathematics",
                    "Dr. David Hilbert", 3, 45, 30,
                    "Vector spaces, matrix decomposition, spectral theorem, combinatorics, and discrete mathematical proof techniques.",
                    "Mon, Wed & Fri • 01:00 PM - 02:00 PM", "Euler Memorial Hall 102"),

            new Course("MATH-210", "Probability and Statistics for Engineers", "Mathematics",
                    "Dr. Sarah Gauss", 3, 35, 15,
                    "Continuous random variables, joint distributions, central limit theorem, Bayesian inference, and hypothesis testing.",
                    "Tue & Thu • 03:00 PM - 04:30 PM", "Euler Memorial Hall 108"),

            new Course("EE-102", "Digital Systems & Logic Design", "Electrical Engineering",
                    "Prof. Claude Shannon", 4, 30, 12,
                    "Boolean algebra, Karnaugh maps, synchronous finite-state machines, hardware description languages, and FPGA labs.",
                    "Mon & Thu • 02:30 PM - 04:00 PM", "Maxwell Engineering Lab 14"),

            new Course("AI-301", "Introduction to Machine Learning", "Data Science",
                    "Dr. Geoffrey Hinton", 4, 25, 20,
                    "Supervised and unsupervised learning, gradient descent optimization, regularized regression, and neural network basics.",
                    "Wed & Fri • 02:00 PM - 03:30 PM", "Hopper Center 304"),

            new Course("ENG-105", "Technical Communication & Ethics", "Humanities",
                    "Prof. Robert Harper", 2, 50, 35,
                    "Engineering ethics, intellectual property, technical specifications, and oral presentation methodologies.",
                    "Wed • 04:00 PM - 06:00 PM", "Humanities Hall 201")
        );

        for (Course c : demoCourses) {
            courseMap.put(c.getCourseId(), c);
        }

        // 3. Seed Initial Enrollments for student01
        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        addSeedEnrollment("student01", "Alex Mercer", "CS-101", "Introduction to Computer Science", 4, nowStr);
        addSeedEnrollment("student01", "Alex Mercer", "CS-201", "Data Structures and Algorithms", 4, nowStr);
        addSeedEnrollment("student01", "Alex Mercer", "MATH-201", "Linear Algebra & Discrete Structures", 3, nowStr);

        // Seed Initial Enrollments for student02
        addSeedEnrollment("student02", "Sophia Chen", "CS-101", "Introduction to Computer Science", 4, nowStr);
        addSeedEnrollment("student02", "Sophia Chen", "EE-102", "Digital Systems & Logic Design", 4, nowStr);

        // Save everything to disk
        saveAll();
        System.out.println("[DataStore] Seeding complete. Stored " + userMap.size() + " users, "
                + courseMap.size() + " courses, and " + enrollments.size() + " enrollments.");
    }

    private void addSeedEnrollment(String studentId, String studentName, String courseId,
                                  String courseName, int credits, String date) {
        String id = UUID.randomUUID().toString();
        Enrollment e = new Enrollment(id, studentId, studentName, courseId, courseName, credits, date);
        enrollments.add(e);
        enrollmentIndex.add(studentId + "#" + courseId);
    }

    /**
     * Loads persisted JSON records from disk into in-memory collections.
     */
    public synchronized void loadAll() {
        synchronized (FILE_LOCK) {
            try {
                // 1. Load Users
                Path uPath = Paths.get(USERS_FILE);
                if (Files.exists(uPath)) {
                    String json = Files.readString(uPath, StandardCharsets.UTF_8);
                    List<Object> list = JsonUtil.parseArray(json);
                    userMap.clear();
                    for (Object item : list) {
                        if (item instanceof Map) {
                            @SuppressWarnings("unchecked")
                            User u = User.fromMap((Map<String, Object>) item);
                            if (u != null && u.getUsername() != null) {
                                userMap.put(u.getUsername(), u);
                            }
                        }
                    }
                }

                // 2. Load Courses into HashMap
                Path cPath = Paths.get(COURSES_FILE);
                if (Files.exists(cPath)) {
                    String json = Files.readString(cPath, StandardCharsets.UTF_8);
                    List<Object> list = JsonUtil.parseArray(json);
                    courseMap.clear();
                    for (Object item : list) {
                        if (item instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Course c = Course.fromMap((Map<String, Object>) item);
                            if (c != null && c.getCourseId() != null) {
                                courseMap.put(c.getCourseId(), c);
                            }
                        }
                    }
                }

                // 3. Load Enrollments into ArrayList and build HashSet index
                Path ePath = Paths.get(ENROLLMENTS_FILE);
                if (Files.exists(ePath)) {
                    String json = Files.readString(ePath, StandardCharsets.UTF_8);
                    List<Object> list = JsonUtil.parseArray(json);
                    enrollments.clear();
                    enrollmentIndex.clear();
                    for (Object item : list) {
                        if (item instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Enrollment e = Enrollment.fromMap((Map<String, Object>) item);
                            if (e != null && e.getStudentId() != null && e.getCourseId() != null) {
                                enrollments.add(e);
                                enrollmentIndex.add(e.getStudentId() + "#" + e.getCourseId());
                            }
                        }
                    }
                }
                System.out.println("[DataStore] Loaded " + userMap.size() + " users, "
                        + courseMap.size() + " courses, " + enrollments.size() + " enrollments from disk.");
            } catch (Exception e) {
                System.err.println("[DataStore] Error loading files from disk: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Atomically saves all data collections to their respective JSON files.
     */
    public synchronized void saveAll() {
        saveUsers();
        saveCourses();
        saveEnrollments();
    }

    public void saveUsers() {
        synchronized (FILE_LOCK) {
            try {
                List<Map<String, Object>> list = new ArrayList<>();
                for (User u : userMap.values()) {
                    list.add(u.toMap(true));
                }
                String json = JsonUtil.toPrettyJson(list);
                Files.writeString(Paths.get(USERS_FILE), json, StandardCharsets.UTF_8);
            } catch (IOException e) {
                System.err.println("[DataStore] Error saving users: " + e.getMessage());
            }
        }
    }

    public void registerUser(User user) {
        synchronized (FILE_LOCK) {
            String email = user.getEmail().trim().toLowerCase(Locale.ROOT);
            for (User existing : userMap.values()) {
                if (existing.getEmail() != null && existing.getEmail().trim().equalsIgnoreCase(email)) {
                    throw new IllegalArgumentException("An account with this email already exists.");
                }
            }
            if (userMap.containsKey(user.getUsername())) {
                throw new IllegalArgumentException("An account with this username already exists.");
            }

            List<Map<String, Object>> records = new ArrayList<>();
            for (User existing : userMap.values()) {
                records.add(existing.toMap(true));
            }
            records.add(user.toMap(true));
            try {
                Files.writeString(Paths.get(USERS_FILE), JsonUtil.toPrettyJson(records), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException("Could not save the new account. Please try again.", e);
            }
            userMap.put(user.getUsername(), user);
        }
    }

    public void saveCourses() {
        synchronized (FILE_LOCK) {
            try {
                List<Map<String, Object>> list = new ArrayList<>();
                for (Course c : courseMap.values()) {
                    list.add(c.toMap());
                }
                String json = JsonUtil.toPrettyJson(list);
                Files.writeString(Paths.get(COURSES_FILE), json, StandardCharsets.UTF_8);
            } catch (IOException e) {
                System.err.println("[DataStore] Error saving courses: " + e.getMessage());
            }
        }
    }

    public void saveEnrollments() {
        synchronized (FILE_LOCK) {
            try {
                List<Map<String, Object>> list = new ArrayList<>();
                synchronized (enrollments) {
                    for (Enrollment e : enrollments) {
                        list.add(e.toMap());
                    }
                }
                String json = JsonUtil.toPrettyJson(list);
                Files.writeString(Paths.get(ENROLLMENTS_FILE), json, StandardCharsets.UTF_8);
            } catch (IOException e) {
                System.err.println("[DataStore] Error saving enrollments: " + e.getMessage());
            }
        }
    }

    // ==========================================
    // DSA Accessors
    // ==========================================

    /**
     * HashMap Course Store: Provides O(1) direct lookup by unique courseId.
     */
    public Map<String, Course> getCourseMap() {
        return courseMap;
    }

    /**
     * HashMap User Store: Provides O(1) user profile lookup by username.
     */
    public Map<String, User> getUserMap() {
        return userMap;
    }

    /**
     * ArrayList Enrollment Store: Sequential list of all enrollments.
     */
    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    /**
     * HashSet Enrollment Index: Provides O(1) duplicate check with "studentId#courseId".
     */
    public Set<String> getEnrollmentIndex() {
        return enrollmentIndex;
    }
}
