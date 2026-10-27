package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Course;
import model.Enrollment;
import model.Session;
import model.User;
import service.AdminService;
import service.AuthService;
import service.CourseService;
import service.EnrollmentService;
import util.JsonUtil;
import util.ResponseUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Handles all REST API routes (/api/*) in CourseConnect.
 * Enforces role-based route access, request validation, and uniform JSON responses.
 */
public class ApiHandler implements HttpHandler {
    private final AuthService authService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final AdminService adminService;

    public ApiHandler(AuthService authService,
                      CourseService courseService,
                      EnrollmentService enrollmentService,
                      AdminService adminService) {
        this.authService = authService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.adminService = adminService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getRawQuery());

        // Handle CORS preflight if needed
        if (method.equals("OPTIONS")) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            // ==========================================
            // 1. AUTHENTICATION ROUTES (/api/auth/*)
            // ==========================================
            if (path.equals("/api/auth/login") && method.equals("POST")) {
                handleLogin(exchange);
                return;
            }
            if (path.equals("/api/auth/register") && method.equals("POST")) {
                handleRegister(exchange);
                return;
            }
            if (path.equals("/api/auth/logout") && method.equals("POST")) {
                handleLogout(exchange);
                return;
            }
            if (path.equals("/api/auth/me") && method.equals("GET")) {
                handleMe(exchange);
                return;
            }

            // ==========================================
            // 2. COURSE CATALOGUE ROUTES (/api/courses*)
            // ==========================================
            if (path.equals("/api/courses")) {
                if (method.equals("GET")) {
                    handleGetCourses(exchange, queryParams);
                    return;
                } else if (method.equals("POST")) {
                    handleAddCourse(exchange); // Admin only
                    return;
                }
            }

            if (path.startsWith("/api/courses/")) {
                String courseId = URLDecoder.decode(path.substring("/api/courses/".length()), StandardCharsets.UTF_8);
                if (method.equals("GET")) {
                    handleGetCourseDetails(exchange, courseId);
                    return;
                } else if (method.equals("PUT")) {
                    handleUpdateCourse(exchange, courseId); // Admin only
                    return;
                } else if (method.equals("DELETE")) {
                    handleDeleteCourse(exchange, courseId); // Admin only
                    return;
                }
            }

            // ==========================================
            // 3. STUDENT PORTAL ROUTES (/api/student/*)
            // ==========================================
            if (path.startsWith("/api/student/")) {
                Session session = resolveSession(exchange);
                if (session == null) {
                    ResponseUtil.sendError(exchange, 401, "Authentication required. Please log in.");
                    return;
                }
                if (!session.getUser().isStudent()) {
                    ResponseUtil.sendError(exchange, 403, "Access denied. Student portal only.");
                    return;
                }

                if (path.equals("/api/student/enrollments") && method.equals("GET")) {
                    handleStudentEnrollments(exchange, session.getUser());
                    return;
                }
                if (path.equals("/api/student/enroll") && method.equals("POST")) {
                    handleStudentEnroll(exchange, session.getUser());
                    return;
                }
                if (path.equals("/api/student/drop") && method.equals("POST")) {
                    handleStudentDrop(exchange, session.getUser());
                    return;
                }
                if (path.equals("/api/student/profile") && method.equals("GET")) {
                    handleStudentProfile(exchange, session.getUser());
                    return;
                }
            }

            // ==========================================
            // 4. INSTITUTION ADMIN ROUTES (/api/admin/*)
            // ==========================================
            if (path.startsWith("/api/admin/")) {
                Session session = resolveSession(exchange);
                if (session == null) {
                    ResponseUtil.sendError(exchange, 401, "Authentication required. Please log in as Institution.");
                    return;
                }
                if (!session.getUser().isInstitution()) {
                    ResponseUtil.sendError(exchange, 403, "Access denied. Institution administrator privileges required.");
                    return;
                }

                if (path.equals("/api/admin/stats") && method.equals("GET")) {
                    handleAdminStats(exchange);
                    return;
                }
                if (path.equals("/api/admin/enrollments") && method.equals("GET")) {
                    handleAdminEnrollments(exchange, queryParams);
                    return;
                }
                if (path.equals("/api/admin/students") && method.equals("GET")) {
                    handleAdminStudents(exchange);
                    return;
                }
            }

            // Route Not Found
            ResponseUtil.sendError(exchange, 404, "Endpoint not found: " + method + " " + path);
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            System.err.println("[ApiHandler] Server error on " + path + ": " + e.getMessage());
            e.printStackTrace();
            ResponseUtil.sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    // ==========================================
    // AUTH HANDLERS
    // ==========================================

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);
        String username = (String) req.get("username");
        String password = (String) req.get("password");
        String role = (String) req.get("role");

        Session session = authService.login(username, password, role);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", session.getToken());
        data.put("user", session.getUser().toMap(false));

        ResponseUtil.sendSuccess(exchange, 200, "Login successful", data);
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);
        User user = authService.register(
                (String) req.get("fullName"),
                (String) req.get("email"),
                (String) req.get("password"),
                (String) req.get("confirmPassword"),
                (String) req.get("role"));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user", user.toMap(false));
        ResponseUtil.sendSuccess(exchange, 201, "Account registered successfully.", data);
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null) {
            String token = authHeader.replace("Bearer ", "").trim();
            authService.logout(token);
        }
        ResponseUtil.sendSuccess(exchange, 200, "Logged out successfully", null);
    }

    private void handleMe(HttpExchange exchange) throws IOException {
        Session session = resolveSession(exchange);
        if (session == null) {
            ResponseUtil.sendError(exchange, 401, "No active session.");
            return;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user", session.getUser().toMap(false));
        ResponseUtil.sendSuccess(exchange, 200, "Active session verified", data);
    }

    // ==========================================
    // COURSE CATALOGUE HANDLERS
    // ==========================================

    private void handleGetCourses(HttpExchange exchange, Map<String, String> query) throws IOException {
        String search = query.get("search");
        String dept = query.get("department");
        String sort = query.get("sort");
        Integer credits = null;
        if (query.get("credits") != null && !query.get("credits").trim().isEmpty()) {
            try {
                credits = Integer.parseInt(query.get("credits").trim());
            } catch (NumberFormatException ignored) {}
        }

        List<Course> courses = courseService.queryCourses(search, dept, credits, sort);

        // Check if caller is an authenticated student to flag "isEnrolled"
        Session session = resolveSession(exchange);
        Set<String> enrolledCourseIds = Collections.emptySet();
        if (session != null && session.getUser().isStudent()) {
            enrolledCourseIds = enrollmentService.getStudentEnrolledCourseIds(session.getUser().getUsername());
        }

        List<Map<String, Object>> courseList = new ArrayList<>();
        for (Course c : courses) {
            Map<String, Object> map = c.toMap();
            map.put("isEnrolled", enrolledCourseIds.contains(c.getCourseId()));
            courseList.add(map);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("courses", courseList);
        data.put("totalCount", courseList.size());
        ResponseUtil.sendSuccess(exchange, 200, null, data);
    }

    private void handleGetCourseDetails(HttpExchange exchange, String courseId) throws IOException {
        Course c = courseService.getCourseById(courseId);
        if (c == null) {
            ResponseUtil.sendError(exchange, 404, "Course '" + courseId + "' not found.");
            return;
        }

        Session session = resolveSession(exchange);
        Map<String, Object> map = c.toMap();
        if (session != null && session.getUser().isStudent()) {
            map.put("isEnrolled", enrollmentService.isEnrolled(session.getUser().getUsername(), c.getCourseId()));
        }

        ResponseUtil.sendSuccess(exchange, 200, null, map);
    }

    private void handleAddCourse(HttpExchange exchange) throws IOException {
        Session session = resolveSession(exchange);
        if (session == null || !session.getUser().isInstitution()) {
            ResponseUtil.sendError(exchange, 403, "Access denied. Only institution administrators can create courses.");
            return;
        }

        String body = readBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);
        Course course = Course.fromMap(req);

        Course created = courseService.addCourse(course);
        ResponseUtil.sendSuccess(exchange, 201, "Course added successfully", created.toMap());
    }

    private void handleUpdateCourse(HttpExchange exchange, String courseId) throws IOException {
        Session session = resolveSession(exchange);
        if (session == null || !session.getUser().isInstitution()) {
            ResponseUtil.sendError(exchange, 403, "Access denied. Only institution administrators can modify courses.");
            return;
        }

        String body = readBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);
        Course updated = Course.fromMap(req);

        Course saved = courseService.updateCourse(courseId, updated);
        ResponseUtil.sendSuccess(exchange, 200, "Course updated successfully", saved.toMap());
    }

    private void handleDeleteCourse(HttpExchange exchange, String courseId) throws IOException {
        Session session = resolveSession(exchange);
        if (session == null || !session.getUser().isInstitution()) {
            ResponseUtil.sendError(exchange, 403, "Access denied. Only institution administrators can delete courses.");
            return;
        }

        courseService.deleteCourse(courseId);
        ResponseUtil.sendSuccess(exchange, 200, "Course '" + courseId + "' deleted successfully", null);
    }

    // ==========================================
    // STUDENT PORTAL HANDLERS
    // ==========================================

    private void handleStudentEnrollments(HttpExchange exchange, User student) throws IOException {
        List<Enrollment> list = enrollmentService.getStudentEnrollments(student.getUsername());
        int totalCredits = enrollmentService.calculateEnrolledCredits(student.getUsername());

        List<Map<String, Object>> enrollments = new ArrayList<>();
        for (Enrollment e : list) {
            Map<String, Object> m = e.toMap();
            // Enrich with current course location and schedule from courseMap
            Course c = courseService.getCourseById(e.getCourseId());
            if (c != null) {
                m.put("schedule", c.getSchedule());
                m.put("location", c.getLocation());
                m.put("instructor", c.getInstructor());
                m.put("department", c.getDepartment());
            }
            enrollments.add(m);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enrollments", enrollments);
        data.put("enrolledCount", enrollments.size());
        data.put("totalCredits", totalCredits);
        data.put("maxCredits", EnrollmentService.MAX_CREDIT_CAP);
        data.put("availableCreditCapacity", Math.max(0, EnrollmentService.MAX_CREDIT_CAP - totalCredits));

        ResponseUtil.sendSuccess(exchange, 200, null, data);
    }

    private void handleStudentEnroll(HttpExchange exchange, User student) throws IOException {
        String body = readBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);
        String courseId = (String) req.get("courseId");

        Enrollment enrollment = enrollmentService.enrollStudent(student, courseId);
        ResponseUtil.sendSuccess(exchange, 200, "Successfully enrolled in " + courseId + "!", enrollment.toMap());
    }

    private void handleStudentDrop(HttpExchange exchange, User student) throws IOException {
        String body = readBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);
        String courseId = (String) req.get("courseId");

        enrollmentService.dropStudentCourse(student, courseId);
        ResponseUtil.sendSuccess(exchange, 200, "Successfully dropped course " + courseId + ".", null);
    }

    private void handleStudentProfile(HttpExchange exchange, User student) throws IOException {
        int enrolledCount = enrollmentService.getStudentEnrollments(student.getUsername()).size();
        int totalCredits = enrollmentService.calculateEnrolledCredits(student.getUsername());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user", student.toMap(false));
        data.put("enrolledCoursesCount", enrolledCount);
        data.put("totalCredits", totalCredits);
        data.put("maxCredits", EnrollmentService.MAX_CREDIT_CAP);
        data.put("creditsRemaining", Math.max(0, EnrollmentService.MAX_CREDIT_CAP - totalCredits));

        ResponseUtil.sendSuccess(exchange, 200, null, data);
    }

    // ==========================================
    // INSTITUTION ADMIN HANDLERS
    // ==========================================

    private void handleAdminStats(HttpExchange exchange) throws IOException {
        Map<String, Object> stats = adminService.getSystemStatistics();
        ResponseUtil.sendSuccess(exchange, 200, null, stats);
    }

    private void handleAdminEnrollments(HttpExchange exchange, Map<String, String> query) throws IOException {
        String search = query.get("search");
        String courseId = query.get("courseId");

        List<Enrollment> list = enrollmentService.getAllEnrollments(search, courseId);
        List<Map<String, Object>> enrollments = new ArrayList<>();
        for (Enrollment e : list) {
            enrollments.add(e.toMap());
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enrollments", enrollments);
        data.put("totalCount", enrollments.size());
        ResponseUtil.sendSuccess(exchange, 200, null, data);
    }

    private void handleAdminStudents(HttpExchange exchange) throws IOException {
        List<Map<String, Object>> students = adminService.getRegisteredStudents();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("students", students);
        data.put("totalCount", students.size());
        ResponseUtil.sendSuccess(exchange, 200, null, data);
    }

    // ==========================================
    // UTILITY METHODS
    // ==========================================

    private Session resolveSession(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            // Also check query param or cookie
            String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
            if (cookieHeader != null) {
                for (String part : cookieHeader.split(";")) {
                    String[] kv = part.trim().split("=");
                    if (kv.length == 2 && kv[0].equals("sessionToken")) {
                        authHeader = kv[1];
                        break;
                    }
                }
            }
        }
        return authService.authenticateHeader(authHeader);
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int read;
            while ((read = is.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private Map<String, String> parseQueryParams(String rawQuery) {
        Map<String, String> map = new HashMap<>();
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return map;
        }
        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String val = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, val);
            } else if (idx < 0 && !pair.isEmpty()) {
                map.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
            }
        }
        return map;
    }
}
