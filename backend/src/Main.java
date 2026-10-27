import server.HttpServerApp;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * Entry point for CourseConnect application.
 * Default configured port: 8082.
 * Includes port availability verification and collision suggestion logic.
 */
public class Main {
    public static final int DEFAULT_PORT = 8082;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        String frontendDir = "frontend";

        for (int i = 0; i < args.length; i++) {
            if ("--port".equals(args[i]) && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[++i]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port number specified. Defaulting to " + DEFAULT_PORT + ".");
                }
            } else if ("--frontend".equals(args[i]) && i + 1 < args.length) {
                frontendDir = args[++i];
            }
        }

        // 1. Verify frontend directory exists
        File front = new File(frontendDir);
        if (!front.exists()) {
            System.err.println("[Warning] Frontend directory '" + frontendDir + "' does not exist in: "
                    + new File(".").getAbsolutePath());
        }

        // 2. Pre-flight check: Check whether the target port is available
        if (!isPortAvailable(port)) {
            System.err.println("==================================================================");
            System.err.println(" [ERROR] Port " + port + " is already occupied by another running process!");
            System.err.println("==================================================================");
            System.err.println(" CourseConnect respects existing services (such as Jenkins on 8080 or");
            System.err.println(" other applications on 8081) and will NOT terminate active processes.");
            System.err.println();
            int suggestedPort = findNextAvailablePort(port);
            if (suggestedPort != -1) {
                System.err.println(" [SUGGESTION] Port " + suggestedPort + " is free and available.");
                System.err.println(" You can start CourseConnect on port " + suggestedPort + " with:");
                System.err.println("    java -cp backend\\bin Main --port " + suggestedPort + " --frontend " + frontendDir);
                System.err.println("    Or update run.bat / run.sh with --port " + suggestedPort);
            } else {
                System.err.println(" [SUGGESTION] Please specify an available port with: --port <port-number>");
            }
            System.err.println("==================================================================");
            System.exit(1);
        }

        // 3. Start HttpServerApp on the verified available port
        try {
            HttpServerApp app = new HttpServerApp(port, frontendDir);
            app.start();

            // Register shutdown hook for graceful termination
            Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
        } catch (Exception e) {
            System.err.println("Failed to start CourseConnect server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Checks if a local TCP port is available for binding.
     */
    public static boolean isPortAvailable(int port) {
        if (port < 1024 || port > 65535) {
            return false;
        }
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Finds the next sequentially available port starting after startingPort.
     */
    public static int findNextAvailablePort(int startingPort) {
        for (int p = startingPort + 1; p < startingPort + 50; p++) {
            if (isPortAvailable(p)) {
                return p;
            }
        }
        return -1;
    }
}
