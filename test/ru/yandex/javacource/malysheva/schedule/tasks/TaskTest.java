package ru.yandex.javacource.malysheva.schedule.tasks;

import org.junit.jupiter.api.Test;
import ru.yandex.javacource.malysheva.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacource.malysheva.schedule.manager.TaskType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TaskTest {

    @Test
    void tasksEqualWithSameId() {
        InMemoryTaskManager tManager = new InMemoryTaskManager();
        Task task1 = new Task("task1", "description1", TaskStatus.NEW, TaskType.TASK);
        Task task2 = new Task("task2", "description2", TaskStatus.NEW, TaskType.TASK);
        Task task3 = new Task("task1", "description1", TaskStatus.NEW, TaskType.TASK);

        tManager.addTask(task1);
        tManager.addTask(task2);
        tManager.addTask(task3);

        int id = task1.getId();
        task2.setId(id);

        assertEquals(task1, task2);
        assertNotEquals(task1, task3);
    }

}