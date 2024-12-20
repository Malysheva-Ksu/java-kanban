package ru.yandex.javacource.malysheva.schedule.tasks;

import org.junit.jupiter.api.Test;
import ru.yandex.javacource.malysheva.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacource.malysheva.schedule.manager.TaskType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SubtaskTest {

    @Test
    void subtasksEqualWithSameId() {
        InMemoryTaskManager tManager = new InMemoryTaskManager();
        Epic epic1 = new Epic("1","1",TaskStatus.NEW, TaskType.EPIC);
        tManager.addEpic(epic1);
        int epicId = epic1.getId();
        Subtask subtask1 = new Subtask("task1", "description1", TaskStatus.NEW, TaskType.SUBTASK);
        Subtask subtask2 = new Subtask("task2", "description2", TaskStatus.NEW, TaskType.SUBTASK);
        Subtask subtask3 = new Subtask("task2", "description1", TaskStatus.NEW, TaskType.SUBTASK);

        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);
        subtask3.setEpicId(epicId);

        tManager.addSubtask(subtask1);
        tManager.addSubtask(subtask2);
        tManager.addSubtask(subtask3);

        int id = subtask1.getId();
        subtask2.setId(id);

        assertEquals(subtask1, subtask2);
        assertNotEquals(subtask1, subtask3);
    }

}