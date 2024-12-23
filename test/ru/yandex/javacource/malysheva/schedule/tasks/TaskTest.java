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
        Task task1 = new Task(TaskType.TASK,"task1", TaskStatus.NEW, "description1");
        Task task2 = new Task(TaskType.TASK,"task2", TaskStatus.NEW,"description2");
        Task task3 = new Task(TaskType.TASK,"task1", TaskStatus.NEW,"description1");

        tManager.addTask(task1);
        tManager.addTask(task2);
        tManager.addTask(task3);

        int id = task1.getId();
        task2.setId(id);

        assertEquals(task1, task2);
        assertNotEquals(task1, task3);
    }

}