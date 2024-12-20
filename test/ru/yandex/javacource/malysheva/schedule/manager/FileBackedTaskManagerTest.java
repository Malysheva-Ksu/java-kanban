package ru.yandex.javacource.malysheva.schedule.manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacource.malysheva.schedule.tasks.Epic;
import ru.yandex.javacource.malysheva.schedule.tasks.Subtask;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBackedTaskManagerTest {
    private FileBackedTaskManager manager;
    private File tempFile;

    @BeforeEach
    public void setUp() throws IOException {
        tempFile = File.createTempFile("task_manager_test", ".csv");
        manager = new FileBackedTaskManager(tempFile.getAbsolutePath());
    }

    @AfterEach
    public void deleteFile() {
        tempFile.delete();
    }

    @Test
    public void testSaveAndLoadEmptyFile() {
        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(0, loadedManager.getTasks().size());
        assertEquals(0, loadedManager.getEpics().size());
        assertEquals(0, loadedManager.getSubtasks().size());
    }

    @Test
    public void testAddAndSaveTasks() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK);
        Epic epic1 = new Epic("Epic 1", "Description 3", TaskStatus.NEW, TaskType.EPIC);

        manager.addTask(task1);
        manager.addEpic(epic1);

        int epicId = epic1.getId();

        Subtask subtask1 = new Subtask("Subtask 1", "Description 2", TaskStatus.NEW, TaskType.SUBTASK);
        subtask1.setEpicId(epicId);
        manager.addSubtask(subtask1);

        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loadedManager.getTasks().size());
        assertEquals(task1.getTitle(), loadedManager.getTasks().get(0).getTitle());

        assertEquals(1, loadedManager.getSubtasks().size());
        assertEquals(subtask1.getTitle(), loadedManager.getSubtasks().get(0).getTitle());

        assertEquals(1, loadedManager.getEpics().size());
        assertEquals(epic1.getTitle(), loadedManager.getEpics().get(0).getTitle());
    }
}

