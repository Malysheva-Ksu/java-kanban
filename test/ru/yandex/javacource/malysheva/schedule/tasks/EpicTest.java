package ru.yandex.javacource.malysheva.schedule.tasks;

import org.junit.jupiter.api.Test;
import ru.yandex.javacource.malysheva.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacource.malysheva.schedule.manager.TaskType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class EpicTest {

    @Test
    void epicsEqualWithSameId() {
        InMemoryTaskManager tManager = new InMemoryTaskManager();
        Epic epic1 = new Epic(TaskType.EPIC, "task1", TaskStatus.NEW, "description1", new Duration(10),
                LocalDateTime.now());
        Epic epic2 = new Epic(TaskType.EPIC, "task2", TaskStatus.NEW, "description2", new Duration(10),
                LocalDateTime.now().plusMinutes(10));
        Epic epic3 = new Epic(TaskType.EPIC, "task1", TaskStatus.NEW, "description1", new Duration(10),
                LocalDateTime.now().plusMinutes(20));

        tManager.addEpic(epic1);
        tManager.addEpic(epic2);
        tManager.addEpic(epic3);

        int id = epic1.getId();
        epic2.setId(id);

        assertEquals(epic1, epic2);
        assertNotEquals(epic1, epic3);
    }
}