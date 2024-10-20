package ru.yandex.javacource.malysheva.schedule.manager.tests;

import org.junit.jupiter.api.Test;
import ru.yandex.javacource.malysheva.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacource.malysheva.schedule.manager.Managers;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryHistoryManagerTest {

    @Test
    void historySizeLimit() {
        InMemoryTaskManager tManager = (InMemoryTaskManager) Managers.getDefault();

        for (int i = 0; i < 11; i++) {
            Task task = new Task("i", "i", TaskStatus.NEW);
            tManager.addTask(task);
            int id = task.getId();
            tManager.getTask(id);
        }
        assertEquals(10, tManager.getHistory().size());

    }

}