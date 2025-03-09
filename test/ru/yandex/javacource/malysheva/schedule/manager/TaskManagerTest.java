package ru.yandex.javacource.malysheva.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacource.malysheva.schedule.tasks.Duration;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    @BeforeEach
    public void setUp() {
        taskManager = createTaskManager();
    }

    protected abstract T createTaskManager();

    @Test
    public void testAddTask() {
        Task task = new Task(TaskType.TASK, "Задача 1", TaskStatus.NEW, "Описание 1", new Duration(30),
                LocalDateTime.now());
        int id = taskManager.addTask(task);
        assertNotEquals(0, id);
        assertEquals(task, taskManager.getTask(id));
    }

    @Test
    public void testUpdateTask() {
        Task task = new Task(TaskType.TASK, "Задача 1", TaskStatus.NEW, "Описание 1", new Duration(30),
                LocalDateTime.now());
        int id = taskManager.addTask(task);
        assertEquals(task, taskManager.getTask(id));

        task.setTitle("Обновленная задача");
        taskManager.updateTask(task);
        assertEquals(task, taskManager.getTask(id));
    }

    @Test
    public void testDeleteTask() {
        Task task = new Task(TaskType.TASK, "Задача 1", TaskStatus.NEW, "Описание 1", new Duration(30),
                LocalDateTime.now());
        int id = taskManager.addTask(task);
        taskManager.deleteTask(id);
        assertNull(taskManager.getTask(id));
    }
}



