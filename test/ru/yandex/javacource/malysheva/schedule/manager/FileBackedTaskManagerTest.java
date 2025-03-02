package ru.yandex.javacource.malysheva.schedule.manager;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.javacource.malysheva.schedule.tasks.Duration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacource.malysheva.schedule.tasks.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            tempFile = File.createTempFile("task_manager_test", ".csv");
            tempFile.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный файл", e);
        }

        return new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    public void deleteFile() {
        tempFile.delete();
    }

    @Test
    public void testSaveToFile_Success() {
        Task task = new Task(TaskType.TASK, "Задача 1", TaskStatus.NEW,
                "Описание задачи 1", new Duration(30), LocalDateTime.now());
        taskManager.addTask(task);

        assertDoesNotThrow(() -> {
            taskManager.save();
        }, "Сохранение задач должно выполняться без исключений");
    }


    @Test
    public void testEpicStatus_AllNew() {
        Epic epic = new Epic(TaskType.EPIC, "Эпик 1", TaskStatus.NEW, "Описание эпик 1", new Duration(30),
                LocalDateTime.now());
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask(TaskType.SUBTASK, "Подзадача 1", TaskStatus.NEW, "Описание подзадачи 1",
                new Duration(30), LocalDateTime.now());
        subtask1.setEpicId(epicId);
        Subtask subtask2 = new Subtask(TaskType.SUBTASK, "Подзадача 2", TaskStatus.NEW, "Описание подзадачи 2",
                new Duration(30), LocalDateTime.now().plusMinutes(30));
        subtask2.setEpicId(epicId);

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.updateEpicStatus(epicId);
        assertEquals(TaskStatus.NEW, taskManager.getEpic(epicId).getStatus());
    }

    @Test
    public void testEpicStatus_AllDone() {
        Epic epic = new Epic(TaskType.EPIC, "Эпик 1", TaskStatus.NEW, "Описание эпик 1", new Duration(30),
                LocalDateTime.now());
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask(TaskType.SUBTASK, "Подзадача 1", TaskStatus.DONE, "Описание подзадачи 1",
                new Duration(30), LocalDateTime.now());
        subtask1.setEpicId(epicId);
        Subtask subtask2 = new Subtask(TaskType.SUBTASK, "Подзадача 2", TaskStatus.DONE, "Описание подзадачи 2",
                new Duration(30), LocalDateTime.now().plusMinutes(30));
        subtask2.setEpicId(epicId);

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.updateEpicStatus(epicId);
        assertEquals(TaskStatus.DONE, taskManager.getEpic(epicId).getStatus());
    }

    @Test
    public void testEpicStatus_InProgress() {
        Epic epic = new Epic(TaskType.EPIC, "Эпик 1", TaskStatus.NEW, "Описание эпик 1", new Duration(30),
                LocalDateTime.now());
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask(TaskType.SUBTASK, "Подзадача 1", TaskStatus.IN_PROGRESS, "Описание подзадачи 1",
                new Duration(30), LocalDateTime.now());
        subtask1.setEpicId(epicId);

        taskManager.addSubtask(subtask1);

        taskManager.updateEpicStatus(epicId);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epicId).getStatus());
    }

    @Test
    public void testEpicStatus_MixedStatus() {
        Epic epic = new Epic(TaskType.EPIC, "Эпик 1", TaskStatus.NEW, "Описание эпик 1", new Duration(30),
                LocalDateTime.now());
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask(TaskType.SUBTASK, "Подзадача 1", TaskStatus.NEW, "Описание подзадачи 1",
                new Duration(30), LocalDateTime.now());
        subtask1.setEpicId(epicId);
        Subtask subtask2 = new Subtask(TaskType.SUBTASK, "Подзадача 2", TaskStatus.DONE, "Описание подзадачи 2",
                new Duration(30), LocalDateTime.now().plusMinutes(30));
        subtask2.setEpicId(epicId);

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.updateEpicStatus(epicId);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epicId).getStatus());
    }

    @Test
    public void testTasksOverlap() {
        Task task1 = new Task(TaskType.TASK, "Task 1", TaskStatus.NEW,
                "Description 1", new Duration(30),
                LocalDateTime.of(2025, 2, 28, 10, 0));

        Task task2 = new Task(TaskType.TASK, "Task 2", TaskStatus.NEW,
                "Description 2", new Duration(30),
                LocalDateTime.of(2025, 2, 28, 10, 15));

        taskManager.addTask(task1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskManager.addTask(task2);
        });

        assertEquals("Задача пересекается с существующей задачей: Task 1", exception.getMessage());
    }

    @Test
    public void testTasksDoNotOverlap() {
        Task task1 = new Task(TaskType.TASK, "Task 1", TaskStatus.NEW,
                "Description 1", new Duration(30),
                LocalDateTime.of(2025, 2, 28, 10, 0));

        Task task2 = new Task(TaskType.TASK, "Task 2", TaskStatus.NEW,
                "Description 2", new Duration(30),
                LocalDateTime.of(2025, 2, 28, 11, 0));

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        assertFalse(taskManager.isTasksOverlapping(task1, task2), "Tasks should not overlap.");
    }

    @Test
    public void testTasksContainEachOther() {
        Task task1 = new Task(TaskType.TASK, "Task 1", TaskStatus.NEW,
                "Description 1", new Duration(60),
                LocalDateTime.of(2025, 2, 28, 10, 0));

        Task task2 = new Task(TaskType.TASK, "Task 2", TaskStatus.NEW,
                "Description 2", new Duration(30),
                LocalDateTime.of(2025, 2, 28, 10, 15));

        taskManager.addTask(task1);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskManager.addTask(task2);
        });

        assertEquals("Задача пересекается с существующей задачей: Task 1", exception.getMessage());
    }

    @Test
    public void testSaveAndLoadEmptyFile() {
        taskManager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(0, loadedManager.getTasks().size());
        assertEquals(0, loadedManager.getEpics().size());
        assertEquals(0, loadedManager.getSubtasks().size());
    }

    @Test
    public void testAddAndSaveTasks() {
        Task task1 = new Task(TaskType.TASK, "Task 1", TaskStatus.NEW, "Description 1", new Duration(10),
                LocalDateTime.now());

        Epic epic1 = new Epic(TaskType.EPIC, "Epic 1", TaskStatus.NEW, "Description 3", new Duration(10),
                LocalDateTime.now().plusMinutes(30));

        taskManager.addTask(task1);
        taskManager.addEpic(epic1);

        int epicId = epic1.getId();

        Subtask subtask1 = new Subtask(TaskType.SUBTASK, "Subtask 1", TaskStatus.NEW, "Description 2", new Duration(10),
                LocalDateTime.now().plusMinutes(100));
        subtask1.setEpicId(epicId);
        taskManager.addSubtask(subtask1);

        epic1.calculateDuration();

        taskManager.save();

        File tempFile = taskManager.getFile();
        System.out.println("Содержимое файла после сохранения:");
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        tempFile = taskManager.getFile();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loadedManager.getTasks().size(), "Тестовая задача не была загружена.");
        assertEquals(task1.getTitle(), loadedManager.getTasks().get(0).getTitle(), "Загруженная задача имеет неправильное название.");

        assertEquals(1, loadedManager.getSubtasks().size(), "Подзадача не была загружена.");
        assertEquals(subtask1.getTitle(), loadedManager.getSubtasks().get(0).getTitle(), "Загруженная подзадача имеет неправильное название.");

        assertEquals(1, loadedManager.getEpics().size(), "Эпик не был загружен.");
        assertEquals(epic1.getTitle(), loadedManager.getEpics().get(0).getTitle(), "Загруженный эпик имеет неправильное название.");
    }

}

