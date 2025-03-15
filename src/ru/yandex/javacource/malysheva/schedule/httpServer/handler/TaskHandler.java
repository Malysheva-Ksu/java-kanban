package ru.yandex.javacource.malysheva.schedule.httpServer.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.javacource.malysheva.schedule.httpServer.adapter.DurationTimeAdapter;
import ru.yandex.javacource.malysheva.schedule.httpServer.adapter.LocalDateTimeAdapter;
import ru.yandex.javacource.malysheva.schedule.manager.NotFoundException;
import ru.yandex.javacource.malysheva.schedule.manager.TaskManager;
import ru.yandex.javacource.malysheva.schedule.manager.TaskType;
import ru.yandex.javacource.malysheva.schedule.tasks.Duration;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskHandler implements HttpHandler {
    private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    private static final int DEFAULT_DURATION_MINUTES = 10;
    private static final String EMPTY_DESCRIPTION = "";
    private static final boolean DEBUG_MODE = true;

    private static final Pattern ID_PATTERN = Pattern.compile("/tasks/(\\d+)");

    private final TaskManager taskManager;
    private final Gson gson;

    public TaskHandler(TaskManager taskManager, Gson httpTaskServer) {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTimeAdapter())
                .setPrettyPrinting()
                .create();
    }

    private void log(String message) {
        if (DEBUG_MODE) {
            System.out.println("[TaskHandler] " + message);
        }
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        try {
            if (pathParts.length == 2 && pathParts[1].equals("tasks")) {
                List<Task> tasks = taskManager.getAllTasks();

                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                        .registerTypeAdapter(Duration.class, new DurationTimeAdapter())
                        .create();

                String jsonResponse = gson.toJson(tasks);

                sendResponse(exchange, 200, jsonResponse);
            } else {
                sendResponse(exchange, 404, "Not Found");
            }
        } catch (Exception e) {
            log("Error in GET request: " + e.getMessage());
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void logError(String message, Throwable throwable) {
        if (DEBUG_MODE) {
            System.err.println("[TaskHandler] ERROR: " + message);
            if (throwable != null) {
                throwable.printStackTrace();
            }
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            log("Request Method: " + method);
            log("Request Path: " + path);

            if ("POST".equals(method) || "PUT".equals(method)) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                log("Request Body: " + requestBody);
            }

            Integer taskId = extractTaskId(path);

            switch (method) {
                case "GET":
                    if (taskId != null) {
                        handleGetTaskById(exchange, taskId);
                    } else {
                        handleGetAllTasks(exchange);
                    }
                    break;
                case "POST":
                    handlePostRequest(exchange);
                    break;
                case "PUT":
                    handlePutRequest(exchange);
                    break;
                case "DELETE":
                    if (taskId != null) {
                        handleDeleteTaskById(exchange, taskId);
                    } else {
                        handleDeleteAllTasks(exchange);
                    }
                    break;
                default:
                    sendResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            logError("Internal server error", e);
            sendResponse(exchange, 500, "Internal server error: " + e.getMessage());
        } finally {
            exchange.close();
        }
    }

    private void handleDeleteTaskById(HttpExchange exchange, Integer taskId) throws IOException {
        try {
            if (taskId == null || taskId <= 0) {
                log("Invalid task ID for deletion: " + taskId);
                sendResponse(exchange, 400, "Invalid task ID");
                return;
            }

            Task existingTask = taskManager.getTask(taskId);

            log("Attempting to delete task with ID: " + taskId);

            try {
                taskManager.deleteTask(taskId);
                String responseBody = gson.toJson(Map.of(
                        "message", "Task deleted successfully",
                        "taskId", taskId,
                        "deletedTaskDetails", Map.of(
                                "title", existingTask.getTitle(),
                                "status", existingTask.getStatus(),
                                "startTime", existingTask.getStartTime().toString()
                        )
                ));

                log("Successfully deleted task: ID " + taskId);

                sendResponse(exchange, 200, responseBody);

            } catch (Exception e) {
                logError("Error deleting task with ID " + taskId, e);
                sendResponse(exchange, 500, "Error deleting task: " + e.getMessage());
            }

        } catch (Exception e) {
            logError("Unexpected error in deleteTaskById", e);
            sendResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void handleDeleteAllTasks(HttpExchange exchange) throws IOException {
        try {
            log("Attempting to delete all tasks");

            int taskCount = taskManager.getTasks().size();

            try {
                taskManager.clearTasks();

                log("Successfully deleted " + taskCount + " tasks");

                String responseBody = gson.toJson(Map.of(
                        "message", "All tasks deleted successfully",
                        "deletedCount", taskCount
                ));

                sendResponse(exchange, 200, responseBody);
            } catch (Exception e) {
                logError("Error deleting all tasks", e);
                sendResponse(exchange, 500, "Error deleting tasks: " + e.getMessage());
            }
        } catch (Exception e) {
            logError("Unexpected error in deleteAllTasks", e);
            sendResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }


    private Integer extractTaskId(String path) {
        Matcher matcher = ID_PATTERN.matcher(path);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskManager.getTasks();
        sendResponse(exchange, 200, gson.toJson(tasks));
    }

    private void handleGetTaskById(HttpExchange exchange, int id) throws IOException, NotFoundException {
        Task task = taskManager.getTask(id);
        if (task != null) {
            sendResponse(exchange, 200, gson.toJson(task));
        } else {
            sendResponse(exchange, 404, "Task not found");
        }
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            log("Received POST request body: '" + requestBody + "'");

            Task task;
            if (requestBody.trim().isEmpty() || requestBody.trim().equals("{}")) {
                log("Empty request body, creating default task");
                task = new Task(
                        TaskType.TASK,
                        "Задача 1",
                        TaskStatus.NEW,
                        "Описание задачи 1",
                        new Duration(30),
                        LocalDateTime.now()
                );
            } else {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                        .registerTypeAdapter(Duration.class, new DurationTimeAdapter())
                        .create();

                task = gson.fromJson(requestBody, Task.class);

                if (task.getTitle() == null) task.setTitle("Задача");
                if (task.getDescription() == null) task.setDescription("Описание задачи");
                if (task.getStatus() == null) task.setStatus(TaskStatus.NEW);
                if (task.getType() == null) task.setType(TaskType.TASK);
                if (task.getDuration() == null) task.setDuration(new Duration(30));
                if (task.getStartTime() == null) task.setStartTime(LocalDateTime.now());
            }

            int taskId = taskManager.addTask(task);
            task.setId(taskId);

            log("Created task: " + new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .registerTypeAdapter(Duration.class, new DurationTimeAdapter())
                    .create()
                    .toJson(task)
            );

            sendResponse(exchange, 201, String.valueOf(taskId));

        } catch (JsonSyntaxException e) {
            log("JSON parsing error: " + e.getMessage());
            sendResponse(exchange, 400, "Invalid JSON format: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log("Task validation error: " + e.getMessage());
            sendResponse(exchange, 400, "Invalid task data: " + e.getMessage());
        } catch (Exception e) {
            log("Unexpected error: " + e.getMessage());
            sendResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private Task createDefaultTask() {
        Task defaultTask = new Task(TaskType.TASK,
                "default task",
                TaskStatus.NEW,
                "default task",
                new Duration(10),
                LocalDateTime.now()
                );
        return defaultTask;
    }


    private Task parseTaskFromJson(String json) {
        try {
            Task task = gson.fromJson(json, Task.class);
            enrichTaskWithDefaultValues(task);

            log("Parsed Task: " + gson.toJson(task));

            return task;
        } catch (JsonSyntaxException e) {
            logError("JSON Parsing Error", e);
            throw new IllegalArgumentException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    private void enrichTaskWithDefaultValues(Task task) {
        if (task == null) {
            log("Received null task, throwing exception");
            throw new IllegalArgumentException("Task cannot be null");
        }

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.NEW);
            log("Set default status: NEW");
        }

        if (task.getStartTime() == null) {
            task.setStartTime(LocalDateTime.now());
            log("Set default start time: " + task.getStartTime());
        }

        if (task.getDuration() == null) {
            task.setDuration(new Duration(DEFAULT_DURATION_MINUTES));
            log("Set default duration: " + DEFAULT_DURATION_MINUTES + " minutes");
        }

        if (task.getDescription() == null) {
            task.setDescription(EMPTY_DESCRIPTION);
            log("Set default empty description");
        }
    }

    private void handlePutRequest(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            if (requestBody.trim().isEmpty() || requestBody.trim().equals("{}")) {
                log("Received empty request body for PUT");
                sendResponse(exchange, 400, "Request body cannot be empty");
                return;
            }

            Task task = parseTaskFromJson(requestBody);

            if (task.getId() <= 0) {
                log("Invalid task ID for update");
                sendResponse(exchange, 400, "Task ID is required for update");
                return;
            }

            Task existingTask = taskManager.getTask(task.getId());
            if (existingTask == null) {
                log("Task not found for update: ID " + task.getId());
                sendResponse(exchange, 404, "Task not found");
                return;
            }

            validateTask(task);

            try {
                taskManager.updateTask(task);
                log("Task updated successfully: ID " + task.getId());
                sendResponse(exchange, 200, "Task updated successfully");
            } catch (Exception e) {
                logError("Error updating task", e);
                sendResponse(exchange, 500, "Error updating task: " + e.getMessage());
            }

        } catch (JsonSyntaxException e) {
            logError("JSON parsing error in PUT request", e);
            sendResponse(exchange, 400, "Invalid JSON format: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logError("Validation error in PUT request", e);
            sendResponse(exchange, 400, "Invalid task data: " + e.getMessage());
        } catch (Exception e) {
            logError("Unexpected error in PUT request", e);
            sendResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void validateTask(Task task) {
        List<String> validationErrors = new ArrayList<>();

        if (task == null) {
            validationErrors.add("Task cannot be null");
        } else {
            if (task.getId() <= 0) {
                validationErrors.add("Task ID must be a positive number");
            }

            if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
                validationErrors.add("Task title is required and cannot be empty");
            }

            if (task.getStartTime() == null) {
                validationErrors.add("Start time is required");
            }

            if (task.getDuration() == null || task.getDuration().getMinutes() <= 0) {
                validationErrors.add("Duration is required and must be positive");
            }

            if (task.getStatus() == null) {
                validationErrors.add("Task status is required");
            }
        }

        if (!validationErrors.isEmpty()) {
            String errorMessage = "Validation failed: " + String.join("; ", validationErrors);
            log(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        try {
            log("Sending response: Status " + statusCode + ", Body: " + response);

            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", CONTENT_TYPE_JSON);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
                os.flush();
            }

            log("Response sent successfully");
        } catch (IOException e) {
            logError("Error sending response", e);
            throw e;
        }
    }
}