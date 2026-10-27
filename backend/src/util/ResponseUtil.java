package util;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper to produce uniform REST-style JSON responses across all HTTP routes.
 */
public class ResponseUtil {

    public static void sendJson(HttpExchange exchange, int statusCode, Object payload) throws IOException {
        String json = JsonUtil.toJson(payload);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendSuccess(HttpExchange exchange, int statusCode, String message, Object data) throws IOException {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        if (message != null) {
            resp.put("message", message);
        }
        if (data != null) {
            resp.put("data", data);
        }
        sendJson(exchange, statusCode, resp);
    }

    public static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", false);
        resp.put("message", message != null ? message : "An unexpected error occurred");
        sendJson(exchange, statusCode, resp);
    }
}
