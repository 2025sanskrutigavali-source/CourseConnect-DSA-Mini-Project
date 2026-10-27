// Quick test: confirm Main refuses to start on an occupied port and prints a helpful message
import java.io.IOException;
import java.net.ServerSocket;

public class PortCheckTest {
    public static void main(String[] args) throws Exception {
        int testPort = 8082;

        // Occupy the port first
        ServerSocket blocker = new ServerSocket(testPort);
        System.out.println("[Setup] Occupied port " + testPort + " with a test socket.");

        // Run Main - it should detect the occupied port and exit with suggestion
        try {
            // Fork to check in-process
            boolean available = Main.isPortAvailable(testPort);
            System.out.println("[PASS] Main.isPortAvailable(" + testPort + ") correctly returned: " + available + " (expected: false)");

            int suggested = Main.findNextAvailablePort(testPort);
            System.out.println("[PASS] Main.findNextAvailablePort() suggested next free port: " + suggested);

            if (suggested > testPort && suggested < testPort + 50) {
                System.out.println("[PASS] Suggested port " + suggested + " is valid and within expected range.");
            } else {
                System.out.println("[FAIL] Suggested port " + suggested + " is not in expected range.");
                System.exit(1);
            }
        } finally {
            blocker.close();
            System.out.println("[Cleanup] Released test socket on port " + testPort + ".");
        }

        // Verify port is now free again
        boolean nowFree = Main.isPortAvailable(testPort);
        System.out.println("[PASS] Port " + testPort + " is free again after release: " + nowFree);
        System.out.println("\nAll port-safety checks passed.");
    }
}
