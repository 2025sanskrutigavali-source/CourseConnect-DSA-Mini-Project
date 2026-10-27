package server;

import com.sun.net.httpserver.HttpServer;
import repository.DataStore;
import service.AdminService;
import service.AuthService;
import service.CourseService;
import service.EnrollmentService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Main HTTP server wrapper encapsulating the Java com.sun.net.httpserver.HttpServer instance.
 */
public class HttpServerApp {
    private final int port;
    private final String frontendDir;
    private HttpServer server;
    private ThreadPoolExecutor executor;

    public HttpServerApp(int port, String frontendDir) {
        this.port = port;
        this.frontendDir = frontendDir;
    }

    public void start() throws IOException {
        DataStore dataStore = new DataStore();
        AuthService authService = new AuthService(dataStore);
        CourseService courseService = new CourseService(dataStore);
        EnrollmentService enrollmentService = new EnrollmentService(dataStore);
        AdminService adminService = new AdminService(dataStore);

        // Bind server to all interfaces on specified port
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // 1. API Context for REST endpoints
        ApiHandler apiHandler = new ApiHandler(authService, courseService, enrollmentService, adminService);
        server.createContext("/api", apiHandler);

        // 2. Static File Context for UI assets
        StaticFileHandler staticHandler = new StaticFileHandler(frontendDir);
        server.createContext("/", staticHandler);

        // Multi-threaded executor
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(16);
        server.setExecutor(executor);

        server.start();

        System.out.println("==================================================================");
        System.out.println("   CourseConnect - Student Course Enrollment System (DSA Project) ");
        System.out.println("==================================================================");
        System.out.println(" Server successfully running on: http://localhost:" + port);
        System.out.println(" Serving frontend files from:    " + frontendDir);
        System.out.println(" Persistence data directory:     data/");
        System.out.println("------------------------------------------------------------------");
        System.out.println(" DEMO CREDENTIALS (Local demo only):");
        System.out.println("   Student 1:     student01 / student123");
        System.out.println("   Student 2:     student02 / student123");
        System.out.println("   Institution:   admin01   / admin123");
        System.out.println("==================================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(1);
        }
        if (executor != null) {
            executor.shutdown();
        }
        System.out.println("[HttpServerApp] Server stopped.");
    }
}
