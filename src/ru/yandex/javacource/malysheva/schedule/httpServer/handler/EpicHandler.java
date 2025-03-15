package ru.yandex.javacource.malysheva.schedule.httpServer.handler;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacource.malysheva.schedule.manager.NotFoundException;
import ru.yandex.javacource.malysheva.schedule.manager.TaskManager;
import ru.yandex.javacource.malysheva.schedule.manager.TaskType;
import ru.yandex.javacource.malysheva.schedule.tasks.Duration;
import ru.yandex.javacource.malysheva.schedule.tasks.Epic;
import ru.yandex.javacource.malysheva.schedule.tasks.Subtask;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EpicHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public EpicHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");

            try {
                switch (method) {
                    case "GET":
                        handleGetEpics(exchange, pathParts);
                        break;
                    case "POST":
                        handlePostEpic(exchange);
                        break;
                    case "PUT":
                        handlePutEpic(exchange, pathParts);
                        break;
                    case "DELETE":
                        handleDeleteEpic(exchange, pathParts);
                        break;
                    default:
                        sendErrorResponse(exchange, 405, "Method not allowed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendErrorResponse(exchange, 500, "Internal Server Error");
            } finally {
                exchange.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");

        JsonObject errorResponse = new JsonObject();
        errorResponse.addProperty("status", statusCode);
        errorResponse.addProperty("error", errorMessage);

        String jsonErrorResponse = gson.toJson(errorResponse);

        byte[] responseBytes = jsonErrorResponse.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void sendResponse(HttpExchange exchange, String responseBody, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");

        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private TaskStatus parseTaskStatus(JsonObject obj) {
        try {
            if (obj.has("status") && !obj.get("status").isJsonNull()) {
                return TaskStatus.valueOf(obj.get("status").getAsString());
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Некорректный статус: " +
                    (obj.has("status") ? obj.get("status").getAsString() : "null"));
        }
        return TaskStatus.NEW;
    }

    private String getStringOrDefault(JsonObject obj, String key, String defaultValue) {
        return obj.has(key) && !obj.get(key).isJsonNull()
                ? obj.get(key).getAsString()
                : defaultValue;
    }

    private void handleDeleteEpic(HttpExchange exchange, String[] pathParts) throws IOException {
        try {
            if (pathParts.length == 2) {
                taskManager.clearEpics();
                sendResponse(exchange, "All epics deleted", 200);
            } else if (pathParts.length == 3) {
                try {
                    int epicId = Integer.parseInt(pathParts[2]);
                    Epic epic = taskManager.getEpic(epicId);

                    taskManager.deleteEpic(epicId);
                    sendResponse(exchange, "Epic deleted", 200);
                } catch (NumberFormatException e) {
                    sendErrorResponse(exchange, 400, "Invalid epic ID");
                }
            }
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGetEpics(HttpExchange exchange, String[] pathParts) throws IOException {
        if (pathParts.length == 2 || pathParts.length == 3) {
            List<Epic> epics = taskManager.getEpics();
            String response = gson.toJson(epics);
            sendResponse(exchange, response, 200);
        } else if (pathParts.length == 3) {
            try {
                int epicId = Integer.parseInt(pathParts[2]);
                Epic epic = taskManager.getEpic(epicId);
                if (epic != null) {
                    String response = gson.toJson(epic);
                    sendResponse(exchange, response, 200);
                } else {
                    sendErrorResponse(exchange, 404, "Epic not found");
                }
            } catch (NumberFormatException e) {
                sendErrorResponse(exchange, 400, "Invalid epic ID");
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            sendErrorResponse(exchange, 404, "Not found");
        }
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(reader);

        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            jsonBuilder.append(line);
        }

        String jsonInput = jsonBuilder.toString();
        System.out.println("Received Epic JSON: " + jsonInput);

        if (jsonInput.trim().isEmpty()) {
            sendErrorResponse(exchange, 400, "Empty request body");
            return;
        }

        try {
            JsonObject epicData = JsonParser.parseString(jsonInput).getAsJsonObject();

            String title = getStringOrDefault(epicData, "title", "Untitled Epic");
            String description = getStringOrDefault(epicData, "description", "");

            TaskStatus status = parseTaskStatus(epicData);

            Epic epic = new Epic(
                    TaskType.EPIC,
                    title,
                    status,
                    description,
                    new Duration(0),
                    LocalDateTime.now()
            );

            int epicId = taskManager.addEpic(epic);

            String response = String.valueOf(epicId);
            sendResponse(exchange, response, 201);

        } catch (JsonSyntaxException | IllegalStateException e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
            sendErrorResponse(exchange, 400, "Invalid JSON: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handlePutEpic(HttpExchange exchange, String[] pathParts) throws IOException {
        try {
            if (pathParts.length < 3) {
                sendErrorResponse(exchange, 400, "Invalid epic ID");
                return;
            }

            int epicId;
            try {
                epicId = Integer.parseInt(pathParts[2]);
            } catch (NumberFormatException e) {
                sendErrorResponse(exchange, 400, "Invalid epic ID");
                return;
            }

            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(reader);

            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            String jsonInput = jsonBuilder.toString();
            System.out.println("Received Epic Update JSON: " + jsonInput);

            JsonObject epicData = JsonParser.parseString(jsonInput).getAsJsonObject();

            String title = getStringOrDefault(epicData, "title", "Untitled Epic");
            String description = getStringOrDefault(epicData, "description", "");
            TaskStatus status = parseTaskStatus(epicData);

            List<Integer> subtaskIds = new ArrayList<>();

            if (epicData.has("subtaskIds") && !epicData.get("subtaskIds").isJsonNull()) {
                if (epicData.get("subtaskIds").isJsonArray()) {
                    JsonArray subtaskIdsArray = epicData.getAsJsonArray("subtaskIds");
                    for (JsonElement elem : subtaskIdsArray) {
                        try {
                            int subtaskId = elem.getAsInt();
                            subtaskIds.add(subtaskId);
                        } catch (Exception e) {
                            System.err.println("Invalid subtask ID format: " + elem);
                        }
                    }
                }
            }

            Epic existingEpic = (Epic) taskManager.getEpic(epicId);
            if (existingEpic == null) {
                sendErrorResponse(exchange, 404, "Epic not found");
                return;
            }

            existingEpic.setTitle(title);
            existingEpic.setDescription(description);
            existingEpic.setStatus(status);

            existingEpic.cleanSubtaskIds();

            for (Integer subtaskId : subtaskIds) {
                Subtask subtask = (Subtask) taskManager.getSubtask(subtaskId);
                if (subtask != null) {
                    subtask.setEpicId(epicId);
                    taskManager.updateTask(subtask);
                    existingEpic.addSubtaskId(subtaskId);
                }
            }

            taskManager.updateTask(existingEpic);
            sendResponse(exchange, "Epic updated successfully", 200);

        } catch (JsonSyntaxException | IllegalStateException e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
            sendErrorResponse(exchange, 400, "Invalid JSON: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}