package ru.yandex.javacource.malysheva.schedule.tasks;

import org.junit.jupiter.api.Test;
import ru.yandex.javacource.malysheva.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacource.malysheva.schedule.manager.TaskType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SubtaskTest {

    @Test
    void subtasksEqualWithSameId() {
        InMemoryTaskManager tManager = new InMemoryTaskManager();
        Epic epic1 = new Epic(TaskType.EPIC, "1", TaskStatus.NEW, "1", new Duration(10), LocalDateTime.now());
        tManager.addEpic(epic1);
        int epicId = epic1.getId();
        Subtask subtask1 = new Subtask(TaskType.SUBTASK, "task1", TaskStatus.NEW, "description1",
                new Duration(10), LocalDateTime.now());
        Subtask subtask2 = new Subtask(TaskType.SUBTASK, "task2", TaskStatus.NEW, "description2",
                new Duration(10), LocalDateTime.now().plusMinutes(10));
        Subtask subtask3 = new Subtask(TaskType.SUBTASK, "task2", TaskStatus.NEW, "description1",
                new Duration(10), LocalDateTime.now().plusMinutes(20));

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