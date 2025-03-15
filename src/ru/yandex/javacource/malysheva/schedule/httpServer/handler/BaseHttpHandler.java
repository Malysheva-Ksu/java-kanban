package ru.yandex.javacource.malysheva.schedule.httpServer.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.javacource.malysheva.schedule.httpServer.adapter.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    protected void sendText(HttpExchange exchange, String responseText, int statusCode) throws IOException {
        byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    protected void sendJson(HttpExchange exchange, Object responseObject, int statusCode) throws IOException {
        String jsonResponse = gson.toJson(responseObject);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendJson(exchange, new ErrorResponse("Conflict with existing resources"), 409);
    }

    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        sendJson(exchange, new ErrorResponse(message), 400);
    }

    protected void sendServerError(HttpExchange exchange, String message) throws IOException {
        sendJson(exchange, new ErrorResponse(message), 500);
    }

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected Optional<Integer> extractIdFromPath(String path) {
        try {
            String[] pathParts = path.split("/");
            if (pathParts.length > 2) {
                return Optional.of(Integer.parseInt(pathParts[pathParts.length - 1]));
            }
            return Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}