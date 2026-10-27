package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves static assets (HTML, CSS, JS, Images) from the frontend/ directory.
 * Includes path-traversal prevention.
 */
public class StaticFileHandler implements HttpHandler {
    private final String rootDir;

    public StaticFileHandler(String rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path == null || path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }

        // Prevent path traversal
        Path basePath = Paths.get(rootDir).toAbsolutePath().normalize();
        Path targetPath = Paths.get(rootDir, path).toAbsolutePath().normalize();

        if (!targetPath.startsWith(basePath)) {
            send403(exchange);
            return;
        }

        File file = targetPath.toFile();
        if (!file.exists() || file.isDirectory()) {
            // Fallback: If index.html exists, return it (for SPA routing), else 404
            File indexFile = new File(rootDir, "index.html");
            if (indexFile.exists()) {
                file = indexFile;
            } else {
                send404(exchange);
                return;
            }
        }

        String mimeType = getMimeType(file.getName());
        exchange.getResponseHeaders().set("Content-Type", mimeType);
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(200, file.length());

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = exchange.getResponseBody()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
        }
    }

    private String getMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".ico")) return "image/x-icon";
        if (lower.endsWith(".woff2")) return "font/woff2";
        if (lower.endsWith(".woff")) return "font/woff";
        if (lower.endsWith(".ttf")) return "font/ttf";
        return "application/octet-stream";
    }

    private void send404(HttpExchange exchange) throws IOException {
        String resp = "404 Not Found";
        exchange.sendResponseHeaders(404, resp.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(resp.getBytes());
        }
    }

    private void send403(HttpExchange exchange) throws IOException {
        String resp = "403 Forbidden";
        exchange.sendResponseHeaders(403, resp.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(resp.getBytes());
        }
    }
}
