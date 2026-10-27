import server.HttpServerApp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * End-to-end integration test validating the HTTP server, endpoints, and status codes.
 */
public class HttpIntegrationTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        int testPort = 8089;
        HttpServerApp server = new HttpServerApp(testPort, "frontend");

        try {
            server.start();
            System.out.println("[*] Server started on test port " + testPort + ". Running HTTP endpoint tests...");

            String baseUrl = "http://localhost:" + testPort;

            // 1. Static HTML serving test
            HttpResponse staticRes = sendRequest(baseUrl + "/", "GET", null, null);
            assertCondition(staticRes.code == 200 && staticRes.body.contains("CourseConnect"),
                    "HTTP Test 1: GET / serves index.html (200 OK)");

            // 2. CSS file serving test
            HttpResponse cssRes = sendRequest(baseUrl + "/css/styles.css", "GET", null, null);
            assertCondition(cssRes.code == 200 && cssRes.body.contains("--primary: #059669"),
                    "HTTP Test 2: GET /css/styles.css serves stylesheet (200 OK)");

            // 3. Student Login API test
            HttpResponse loginRes = sendRequest(baseUrl + "/api/auth/login", "POST",
                    "{\"username\":\"student01\",\"password\":\"student123\",\"role\":\"STUDENT\"}", null);
            assertCondition(loginRes.code == 200 && loginRes.body.contains("\"token\":"),
                    "HTTP Test 3: POST /api/auth/login returns 200 and session token");

            // Extract student token
            String studentToken = extractJsonValue(loginRes.body, "token");

            // 4. Institution Login API test
            HttpResponse adminLoginRes = sendRequest(baseUrl + "/api/auth/login", "POST",
                    "{\"username\":\"admin01\",\"password\":\"admin123\",\"role\":\"INSTITUTION\"}", null);
            assertCondition(adminLoginRes.code == 200 && adminLoginRes.body.contains("\"token\":"),
                    "HTTP Test 4: POST /api/auth/login for institution returns 200");

            String adminToken = extractJsonValue(adminLoginRes.body, "token");

            // 5. Get Courses API test
            HttpResponse coursesRes = sendRequest(baseUrl + "/api/courses", "GET", null, studentToken);
            assertCondition(coursesRes.code == 200 && coursesRes.body.contains("CS-201"),
                    "HTTP Test 5: GET /api/courses returns 200 with course catalogue");

            // 6. Student Enrollments API test
            HttpResponse enrollRes = sendRequest(baseUrl + "/api/student/enrollments", "GET", null, studentToken);
            assertCondition(enrollRes.code == 200 && enrollRes.body.contains("\"enrollments\":"),
                    "HTTP Test 6: GET /api/student/enrollments returns 200 with student's enrollments");

            // 7. Protected Route Access Control: Student attempting to access Admin Stats
            HttpResponse studentAccessAdminRes = sendRequest(baseUrl + "/api/admin/stats", "GET", null, studentToken);
            assertCondition(studentAccessAdminRes.code == 403,
                    "HTTP Test 7: Student account blocked from GET /api/admin/stats (403 Forbidden)");

            // 8. Admin accessing Admin Stats
            HttpResponse adminStatsRes = sendRequest(baseUrl + "/api/admin/stats", "GET", null, adminToken);
            assertCondition(adminStatsRes.code == 200 && adminStatsRes.body.contains("\"totalCourses\":"),
                    "HTTP Test 8: Institution admin accessing GET /api/admin/stats returns 200 and metrics");

            // 9. Unauthenticated request to protected student route
            HttpResponse noAuthRes = sendRequest(baseUrl + "/api/student/profile", "GET", null, null);
            assertCondition(noAuthRes.code == 401,
                    "HTTP Test 9: Unauthenticated request to GET /api/student/profile returns 401 Unauthorized");

            System.out.println("==================================================================");
            System.out.println(" HTTP INTEGRATION TEST RESULTS: " + passed + " PASSED, " + failed + " FAILED");
            System.out.println("==================================================================");

        } catch (Exception e) {
            e.printStackTrace();
            failed++;
        } finally {
            server.stop();
        }

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static HttpResponse sendRequest(String urlStr, String method, String jsonBody, String token) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        if (jsonBody != null) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
        }

        int code = conn.getResponseCode();
        BufferedReader reader;
        if (code >= 200 && code < 400) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();

        return new HttpResponse(code, sb.toString());
    }

    private static String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
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

    private static class HttpResponse {
        int code;
        String body;
        HttpResponse(int c, String b) {
            this.code = c;
            this.body = b;
        }
    }
}
