package ru.yandex.javacource.malysheva.schedule.manager;


import org.junit.jupiter.api.BeforeEach;
import ru.yandex.javacource.malysheva.schedule.tasks.Epic;
import ru.yandex.javacource.malysheva.schedule.tasks.Subtask;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class InMemoryTaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    void newManager() {
        taskManager = Managers.getDefault();
    }

    @Test
    void testTaskImmutabilityOnAdd() {
        Task task = new Task(TaskType.TASK, "title", TaskStatus.NEW, "description");
        int taskId = taskManager.addTask(task);

        Task addedTask = taskManager.getTask(taskId);

        assertEquals(task.getTitle(), addedTask.getTitle());
        assertEquals(task.getDescription(), addedTask.getDescription());
        assertEquals(task.getStatus(), addedTask.getStatus());
        assertEquals(task.getId(), addedTask.getId());
    }

    @Test
    void addEpicAsSubtask() {
        Epic epic = new Epic(TaskType.EPIC, "title", TaskStatus.NEW, "description");
        int epicId = taskManager.addEpic(epic);

        Subtask subtask = new Subtask(TaskType.SUBTASK, "title", TaskStatus.NEW, "description");
        subtask.setEpicId(epicId);
        subtask.setId(epicId);
        int result = taskManager.addSubtask(subtask);

        assertEquals(0, result);
    }

    @Test
    void addSubtaskAsEpic() {
        Epic epic = new Epic(TaskType.EPIC, "title", TaskStatus.NEW, "description");
        int epicId = taskManager.addEpic(epic);

        Subtask subtask = new Subtask(TaskType.SUBTASK, "title", TaskStatus.NEW, "description");
        subtask.setEpicId(epicId);
        int subtaskId = taskManager.addSubtask(subtask);

        epic.setId(subtask.getId());
        int result = taskManager.addEpic(epic);

        assertEquals(0, result);
    }


    @Test
    void addDifferentTypeOfTasksAndFindItById() {
        Task task = new Task(TaskType.TASK, "title", TaskStatus.NEW, "description");
        int taskId = taskManager.addTask(task);
        Task task2 = taskManager.getTask(taskId);
        assertEquals(task, task2);

        Epic epic = new Epic(TaskType.EPIC, "title", TaskStatus.NEW, "description");
        int epicId = taskManager.addEpic(epic);
        Epic epic2 = taskManager.getEpic(epicId);
        assertEquals(epic, epic2);

        Subtask subtask = new Subtask(TaskType.SUBTASK, "title", TaskStatus.NEW, "description");
        subtask.setEpicId(epicId);
        int subtaskId = taskManager.addSubtask(subtask);
        Subtask subtask2 = taskManager.getSubtask(subtaskId);
        assertEquals(subtask, subtask2);
    }


}