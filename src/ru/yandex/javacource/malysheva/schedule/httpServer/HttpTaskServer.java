package ru.yandex.javacource.malysheva.schedule.httpServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.javacource.malysheva.schedule.httpServer.adapter.LocalDateTimeAdapter;
import ru.yandex.javacource.malysheva.schedule.httpServer.handler.*;
import ru.yandex.javacource.malysheva.schedule.manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private final TaskManager taskManager;
    private HttpServer httpServer;
    private final Gson gson;

    public HttpTaskServer(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(java.time.LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public Gson getGson() {
        return gson;
    }

    public void start() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
            httpServer.createContext("/tasks", new TaskHandler(taskManager, this.getGson()));
            httpServer.createContext("/subtasks", new SubtaskHandler(taskManager, this.getGson()));
            httpServer.createContext("/epics", new EpicHandler(taskManager, this.getGson()));
            httpServer.createContext("/history", new HistoryHandler(taskManager, this.getGson()));
            httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager, this.getGson()));
            httpServer.setExecutor(null);
            httpServer.start();
            System.out.println("HTTP Server started on port 8080");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            System.out.println("HTTP Server stopped.");
        }
    }
}