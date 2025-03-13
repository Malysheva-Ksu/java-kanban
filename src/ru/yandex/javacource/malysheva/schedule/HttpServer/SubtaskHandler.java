package ru.yandex.javacource.malysheva.schedule.HttpServer;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.javacource.malysheva.schedule.manager.TaskManager;
import ru.yandex.javacource.malysheva.schedule.manager.TaskType;
import ru.yandex.javacource.malysheva.schedule.tasks.Duration;
import ru.yandex.javacource.malysheva.schedule.tasks.Subtask;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class SubtaskHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public SubtaskHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");

            switch (method) {
                case "POST":
                    handlePostSubtask(exchange);
                    break;
                case "GET":
                    if (pathParts.length > 2 && pathParts[2].matches("\\d+")) {
                        handleGetSubtask(exchange, Integer.parseInt(pathParts[2]));
                    } else {
                        handleGetSubtasks(exchange);
                    }
                    break;
                case "PUT":
                    if (pathParts.length > 2 && pathParts[2].matches("\\d+")) {
                        handlePutSubtask(exchange, Integer.parseInt(pathParts[2]));
                    } else {
                        sendErrorResponse(exchange, 400, "Invalid subtask ID");
                    }
                    break;
                case "DELETE":
                    if (pathParts.length > 2 && pathParts[2].matches("\\d+")) {
                        handleDeleteSubtask(exchange, Integer.parseInt(pathParts[2]));
                    } else {
                        handleDeleteSubtasks(exchange);
                    }
                    break;
                default:
                    sendErrorResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        } finally {
            exchange.close();
        }
    }

    private void handlePostSubtask(HttpExchange exchange) throws IOException {
        try {
            String jsonInput = readRequestBody(exchange);
            System.out.println("Received Subtask JSON: " + jsonInput);

            Subtask subtask = createSubtaskFromJson(jsonInput);
            int subtaskId = taskManager.addSubtask(subtask);

            sendResponse(exchange, String.valueOf(subtaskId), 201);
        } catch (JsonSyntaxException e) {
            sendErrorResponse(exchange, 400, "Invalid JSON: " + e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Error creating subtask: " + e.getMessage());
        }
    }

    private void handleGetSubtask(HttpExchange exchange, int subtaskId) throws IOException {
        Subtask subtask = taskManager.getSubtask(subtaskId);
        if (subtask == null) {
            sendErrorResponse(exchange, 404, "Subtask not found");
            return;
        }
        String jsonResponse = gson.toJson(subtask);
        sendResponse(exchange, jsonResponse, 200);
    }

    private void handleGetSubtasks(HttpExchange exchange) throws IOException {
        List<Subtask> subtasks = taskManager.getSubtasks();
        if (subtasks.isEmpty()) {
            sendErrorResponse(exchange, 404, "No subtasks found");
            return;
        }
        String jsonResponse = gson.toJson(subtasks);
        sendResponse(exchange, jsonResponse, 200);
    }

    private void sendResponse(HttpExchange exchange, String responseBody, int statusCode) throws IOException {
        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
        JsonObject errorResponse = new JsonObject();
        errorResponse.addProperty("status", statusCode);
        errorResponse.addProperty("error", errorMessage);

        String jsonErrorResponse = gson.toJson(errorResponse);
        byte[] responseBytes = jsonErrorResponse.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void handlePutSubtask(HttpExchange exchange, int subtaskId) throws IOException {
        try {
            String jsonInput = readRequestBody(exchange);
            Subtask existingSubtask = taskManager.getSubtask(subtaskId);

            if (existingSubtask == null) {
                sendErrorResponse(exchange, 404, "Subtask not found");
                return;
            }

            Subtask updatedSubtask = updateSubtaskFromJson(existingSubtask, jsonInput);
            taskManager.updateTask(updatedSubtask);

            sendResponse(exchange, "Subtask updated", 200);
        } catch (JsonSyntaxException e) {
            sendErrorResponse(exchange, 400, "Invalid JSON: " + e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Error updating subtask: " + e.getMessage());
        }
    }

    private void handleDeleteSubtask(HttpExchange exchange, int subtaskId) throws IOException {
        Subtask subtask = taskManager.getSubtask(subtaskId);
        if (subtask == null) {
            sendErrorResponse(exchange, 404, "Subtask not found");
            return;
        }
        taskManager.deleteSubtask(subtaskId);
        sendResponse(exchange, "Subtask deleted", 200);
    }

    private void handleDeleteSubtasks(HttpExchange exchange) throws IOException {
        taskManager.clearSubtasks();
        sendResponse(exchange, "All subtasks deleted", 200);
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {

            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            return jsonBuilder.toString();
        }
    }

    private Subtask createSubtaskFromJson(String jsonInput) {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(Subtask.class, new JsonDeserializer<Subtask>() {
                    @Override
                    public Subtask deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        JsonObject jsonObject = json.getAsJsonObject();

                        Subtask subtask = new Subtask(
                                TaskType.SUBTASK,
                                getStringOrDefault(jsonObject, "title", "Unnamed Subtask"),
                                parseTaskStatus(jsonObject),
                                getStringOrDefault(jsonObject, "description", ""),
                                parseDuration(jsonObject),
                                parseStartTime(jsonObject)
                        );

                        if (jsonObject.has("epicId")) {
                            subtask.setEpicId(jsonObject.get("epicId").getAsInt());
                        }

                        return subtask;
                    }
                });

        return gsonBuilder.create().fromJson(jsonInput, Subtask.class);
    }

    private Subtask updateSubtaskFromJson(Subtask existingSubtask, String jsonInput) {
        JsonObject jsonObject = JsonParser.parseString(jsonInput).getAsJsonObject();

        if (jsonObject.has("title")) {
            existingSubtask.setTitle(jsonObject.get("title").getAsString());
        }

        if (jsonObject.has("status")) {
            try {
                existingSubtask.setStatus(TaskStatus.valueOf(jsonObject.get("status").getAsString()));
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Invalid task status");
            }
        }

        if (jsonObject.has("description")) {
            existingSubtask.setDescription(jsonObject.get("description").getAsString());
        }

        if (jsonObject.has("startTime")) {
            try {
                LocalDateTime startTime = LocalDateTime.parse(jsonObject.get("startTime").getAsString());
                existingSubtask.setStartTime(startTime);
            } catch (DateTimeParseException e) {
                throw new JsonParseException("Invalid start time format");
            }
        }

        if (jsonObject.has("duration")) {
            try {
                JsonObject durationObject = jsonObject.getAsJsonObject("duration");
                Integer durationMinutes = durationObject.get("minutes").getAsInt();
                Duration duration = new Duration(durationMinutes);
                existingSubtask.setDuration(duration);
            } catch (Exception e) {
                throw new JsonParseException("Invalid duration format");
            }
        }

        if (jsonObject.has("epicId")) {
            try {
                int epicId = jsonObject.get("epicId").getAsInt();
                existingSubtask.setEpicId(epicId);
            } catch (NumberFormatException e) {
                throw new JsonParseException("Invalid epic ID");
            }
        }

        return existingSubtask;
    }

    private String getStringOrDefault(JsonObject jsonObject, String key, String defaultValue) {
        return jsonObject.has(key) ? jsonObject.get(key).getAsString() : defaultValue;
    }

    private TaskStatus parseTaskStatus(JsonObject jsonObject) {
        if (jsonObject.has("status")) {
            try {
                return TaskStatus.valueOf(jsonObject.get("status").getAsString());
            } catch (IllegalArgumentException e) {
                return TaskStatus.NEW;
            }
        }
        return TaskStatus.NEW;
    }

    private Duration parseDuration(JsonObject jsonObject) {
        if (jsonObject.has("duration")) {
            try {
                JsonObject durationObject = jsonObject.getAsJsonObject("duration");
                Integer durationMinutes = durationObject.get("minutes").getAsInt();
                return new Duration(durationMinutes);
            } catch (Exception e) {
                return new Duration(10);
            }
        }
        return new Duration(10);
    }

    private LocalDateTime parseStartTime(JsonObject jsonObject) {
        if (jsonObject.has("startTime")) {
            try {
                return LocalDateTime.parse(jsonObject.get("startTime").getAsString());
            } catch (DateTimeParseException e) {
                return LocalDateTime.now();
            }
        }
        return LocalDateTime.now();
    }
}