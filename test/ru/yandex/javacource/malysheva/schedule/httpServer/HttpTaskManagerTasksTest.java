package ru.yandex.javacource.malysheva.schedule.httpServer;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacource.malysheva.schedule.httpServer.adapter.DurationTimeAdapter;
import ru.yandex.javacource.malysheva.schedule.httpServer.adapter.LocalDateTimeAdapter;
import ru.yandex.javacource.malysheva.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacource.malysheva.schedule.manager.TaskManager;
import ru.yandex.javacource.malysheva.schedule.manager.TaskType;
import ru.yandex.javacource.malysheva.schedule.tasks.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    public HttpTaskManagerTasksTest() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTimeAdapter())
                .create();
    }

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();

        taskServer = new HttpTaskServer(manager);
        taskServer.start();

        client = HttpClient.newBuilder().build();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    private void clearLocalTasks() {
        manager.clearTasks();
    }

    @Test
    void testAddTask() throws IOException, InterruptedException {
        clearLocalTasks();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTimeAdapter())
                .serializeNulls()
                .create();

        Task task = new Task(
                TaskType.TASK,
                "Задача 1",
                TaskStatus.NEW,
                "Описание задачи 1",
                new Duration(30),
                LocalDateTime.now()
        );

        String taskJson = gson.toJson(task);

        System.out.println("Отправляемый JSON: " + taskJson);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("Статус ответа: " + postResponse.statusCode());
        System.out.println("Тело ответа: " + postResponse.body());

        assertEquals(201, postResponse.statusCode(), "Ожидался статус код 201 при создании задачи");

        int taskId = Integer.parseInt(postResponse.body());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode(), "Ожидался статус код 200 при получении задачи");

        Task retrievedTask = gson.fromJson(getResponse.body(), Task.class);

        assertEquals("Задача 1", retrievedTask.getTitle(), "Название задачи не совпадает");
        assertEquals("Описание задачи 1", retrievedTask.getDescription(), "Описание задачи не совпадает");
        assertEquals(TaskStatus.NEW, retrievedTask.getStatus(), "Статус задачи не совпадает");
    }


    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        clearLocalTasks();

        Task task = new Task(TaskType.TASK, "Задача 1", TaskStatus.NEW,
                "Описание задачи 1", new Duration(30),
                LocalDateTime.now());

        String taskJson = gson.toJson(task);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest postRequest = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();
        client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        HttpRequest getRequest = HttpRequest.newBuilder().uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Expected status code 200 for successful retrieval");

        List<Task> tasksFromResponse = gson.fromJson(
                response.body(),
                new TypeToken<List<Task>>() {}.getType()
        );

        assertEquals(1, tasksFromResponse.size(), "Expected one task from the response");
        assertEquals("Задача 1", tasksFromResponse.get(0).getTitle(), "Unexpected task name from response");
    }

    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        clearLocalTasks();

        Epic epic = new Epic(
                TaskType.EPIC,
                "Epic",
                TaskStatus.NEW,
                "epic Description",
                new Duration(10),
                LocalDateTime.now()
        );
        epic.setId(1);

        String epicJson = gson.toJson(epic);

        Subtask subtask = new Subtask(
                TaskType.SUBTASK,
                "Subtask",
                TaskStatus.NEW,
                "Subtask Description",
                new Duration(10),
                LocalDateTime.now()
        );
        subtask.setId(2);
        subtask.setEpicId(1);

        HttpRequest epicCreateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> epicCreateResponse = client.send(epicCreateRequest,
                HttpResponse.BodyHandlers.ofString());

        String subtaskJson = gson.toJson(subtask);
        HttpRequest subtaskRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> subtaskResponse = client.send(subtaskRequest,
                HttpResponse.BodyHandlers.ofString());

        epic.setTitle("Updated Epic");
        epic.setDescription("Updated epic Description");
        epic.addSubtaskId(2);

        String epicUpdateJson = gson.toJson(epic);

        HttpRequest epicUpdateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/1"))
                .PUT(HttpRequest.BodyPublishers.ofString(epicUpdateJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> epicUpdateResponse = client.send(epicUpdateRequest,
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, epicUpdateResponse.statusCode());
    }
}