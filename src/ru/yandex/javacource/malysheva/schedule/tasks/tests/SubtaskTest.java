package ru.yandex.javacource.malysheva.schedule.tasks.tests;

import org.junit.jupiter.api.Test;
import ru.yandex.javacource.malysheva.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacource.malysheva.schedule.tasks.Epic;
import ru.yandex.javacource.malysheva.schedule.tasks.Subtask;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SubtaskTest {

    @Test
    void subtasksEqualWithSameId() {
        InMemoryTaskManager tManager = new InMemoryTaskManager();
        Epic epic1 = new Epic("1","1",TaskStatus.NEW);
        tManager.addEpic(epic1);
        int epicId = epic1.getId();
        Subtask subtask1 = new Subtask("task1", "description1", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("task2", "description2", TaskStatus.NEW, epicId);
        Subtask subtask3 = new Subtask("task2", "description1", TaskStatus.NEW, epicId);

        tManager.addSubtask(subtask1);
        tManager.addSubtask(subtask2);
        tManager.addSubtask(subtask3);

        int id = subtask1.getId();
        subtask2.setId(id);

        assertEquals(subtask1, subtask2);
        assertNotEquals(subtask1, subtask3);
    }

}