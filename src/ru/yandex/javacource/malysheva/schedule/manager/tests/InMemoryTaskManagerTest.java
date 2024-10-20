package ru.yandex.javacource.malysheva.schedule.manager.tests;


import ru.yandex.javacource.malysheva.schedule.tasks.Epic;
import ru.yandex.javacource.malysheva.schedule.tasks.Subtask;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;

import org.junit.jupiter.api.Test;
import ru.yandex.javacource.malysheva.schedule.manager.InMemoryTaskManager;

import static org.junit.jupiter.api.Assertions.assertEquals;


class InMemoryTaskManagerTest {


    @Test
    void addDifferentTypeOfTasksAndFindItById() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Task task = new Task("title", "description", TaskStatus.NEW);
        int taskId = taskManager.addTask(task);
        Task task2 = taskManager.getTask(taskId);
        assertEquals(task, task2);

        Epic epic = new Epic("title", "description", TaskStatus.NEW);
        int epicId = taskManager.addEpic(epic);
        Epic epic2 = taskManager.getEpic(epicId);
        assertEquals(epic, epic2);

        Subtask subtask = new Subtask("title", "description", TaskStatus.NEW, epicId);
        int subtaskId = taskManager.addSubtask(subtask);
        Subtask subtask2 = taskManager.getSubtask(subtaskId);
        assertEquals(subtask, subtask2);
    }


}