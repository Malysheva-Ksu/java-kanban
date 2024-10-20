package ru.yandex.javacource.malysheva.schedule.tasks.tests;

import org.junit.jupiter.api.Test;
import ru.yandex.javacource.malysheva.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TaskTest {

    @Test
    void tasksEqualWithSameId() {
        InMemoryTaskManager tManager = new InMemoryTaskManager();
        Task task1 = new Task("task1", "description1", TaskStatus.NEW);
        Task task2 = new Task("task2", "description2", TaskStatus.NEW);
        Task task3 = new Task("task1", "description1", TaskStatus.NEW);

        tManager.addTask(task1);
        tManager.addTask(task2);
        tManager.addTask(task3);

        int id = task1.getId();
        task2.setId(id);

        assertEquals(task1, task2);
        assertNotEquals(task1, task3);
    }

}