package ru.yandex.javacource.malysheva.schedule.tasks;

import org.junit.jupiter.api.Test;
import ru.yandex.javacource.malysheva.schedule.manager.InMemoryTaskManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class EpicTest {

    @Test
    void epicsEqualWithSameId() {
        InMemoryTaskManager tManager = new InMemoryTaskManager();
        Epic epic1 = new Epic("task1", "description1", TaskStatus.NEW);
        Epic epic2 = new Epic("task2", "description2", TaskStatus.NEW);
        Epic epic3 = new Epic("task1", "description1", TaskStatus.NEW);

        tManager.addEpic(epic1);
        tManager.addEpic(epic2);
        tManager.addEpic(epic3);

        int id = epic1.getId();
        epic2.setId(id);

        assertEquals(epic1, epic2);
        assertNotEquals(epic1, epic3);
    }
}