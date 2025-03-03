package ru.yandex.javacource.malysheva.schedule.manager;
import ru.yandex.javacource.malysheva.schedule.tasks.Duration;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

import ru.yandex.javacource.malysheva.schedule.tasks.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }

    @Test
    public void testEpicStatus_AllNew() {
        Epic epic = new Epic(TaskType.EPIC, "Эпик 1", TaskStatus.NEW, "Описание эпик 1", new Duration(30),
                LocalDateTime.now());
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask(TaskType.SUBTASK, "Подзадача 1", TaskStatus.NEW, "Описание подзадачи 1",
                new Duration(30), LocalDateTime.now());
        subtask1.setEpicId(epicId);
        Subtask subtask2 = new Subtask(TaskType.SUBTASK, "Подзадача 2", TaskStatus.NEW, "Описание подзадачи 2",
                new Duration(30), LocalDateTime.now().plusMinutes(30));
        subtask2.setEpicId(epicId);

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.updateEpicStatus(epicId);
        assertEquals(TaskStatus.NEW, taskManager.getEpic(epicId).getStatus());
    }

    @Test
    public void testEpicStatus_AllDone() {
        Epic epic = new Epic(TaskType.EPIC, "Эпик 1", TaskStatus.NEW, "Описание эпик 1", new Duration(30),
                LocalDateTime.now());
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask(TaskType.SUBTASK, "Подзадача 1", TaskStatus.DONE, "Описание подзадачи 1",
                new Duration(30), LocalDateTime.now());
        subtask1.setEpicId(epicId);
        Subtask subtask2 = new Subtask(TaskType.SUBTASK, "Подзадача 2", TaskStatus.DONE, "Описание подзадачи 2",
                new Duration(30), LocalDateTime.now().plusMinutes(30));
        subtask2.setEpicId(epicId);

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.updateEpicStatus(epicId);
        assertEquals(TaskStatus.DONE, taskManager.getEpic(epicId).getStatus());
    }

    @Test
    public void testEpicStatus_InProgress() {
        Epic epic = new Epic(TaskType.EPIC, "Эпик 1", TaskStatus.NEW, "Описание эпик 1", new Duration(30),
                LocalDateTime.now());
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask(TaskType.SUBTASK, "Подзадача 1", TaskStatus.IN_PROGRESS, "Описание подзадачи 1",
                new Duration(30), LocalDateTime.now());
        subtask1.setEpicId(epicId);

        taskManager.addSubtask(subtask1);

        taskManager.updateEpicStatus(epicId);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epicId).getStatus());
    }

    @Test
    public void testEpicStatus_MixedStatus() {
        Epic epic = new Epic(TaskType.EPIC, "Эпик 1", TaskStatus.NEW, "Описание эпик 1", new Duration(30),
                LocalDateTime.now());
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask(TaskType.SUBTASK, "Подзадача 1", TaskStatus.NEW, "Описание подзадачи 1",
                new Duration(30), LocalDateTime.now());
        subtask1.setEpicId(epicId);
        Subtask subtask2 = new Subtask(TaskType.SUBTASK, "Подзадача 2", TaskStatus.DONE, "Описание подзадачи 2",
                new Duration(30), LocalDateTime.now().plusMinutes(30));
        subtask2.setEpicId(epicId);

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.updateEpicStatus(epicId);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epicId).getStatus());
    }

    @Test
    void testTaskImmutabilityOnAdd() {
        Task task = new Task(TaskType.TASK, "title", TaskStatus.NEW, "description", new Duration(10),
                LocalDateTime.now());
        int taskId = taskManager.addTask(task);

        Task addedTask = taskManager.getTask(taskId);

        assertEquals(task.getTitle(), addedTask.getTitle());
        assertEquals(task.getDescription(), addedTask.getDescription());
        assertEquals(task.getStatus(), addedTask.getStatus());
        assertEquals(task.getId(), addedTask.getId());
    }

    @Test
    void addEpicAsSubtask() {
        Epic epic = new Epic(TaskType.EPIC, "title", TaskStatus.NEW, "description", new Duration(10),
                LocalDateTime.now());
        int epicId = taskManager.addEpic(epic);

        Subtask subtask = new Subtask(TaskType.SUBTASK, "title", TaskStatus.NEW, "description", new Duration(10),
                LocalDateTime.now());
        subtask.setEpicId(epicId);
        subtask.setId(epicId);
        int result = taskManager.addSubtask(subtask);

        assertEquals(0, result);
    }

    @Test
    void addSubtaskAsEpic() {
        Epic epic = new Epic(TaskType.EPIC, "title", TaskStatus.NEW, "description", new Duration(10),
                LocalDateTime.now());
        int epicId = taskManager.addEpic(epic);

        Subtask subtask = new Subtask(TaskType.SUBTASK, "title", TaskStatus.NEW, "description", new Duration(10),
                LocalDateTime.now());
        subtask.setEpicId(epicId);
        int subtaskId = taskManager.addSubtask(subtask);

        epic.setId(subtask.getId());
        int result = taskManager.addEpic(epic);

        assertEquals(0, result);
    }


    @Test
    void addDifferentTypeOfTasksAndFindItById() {
        Task task = new Task(TaskType.TASK, "title", TaskStatus.NEW, "description", new Duration(10),
                LocalDateTime.now());
        int taskId = taskManager.addTask(task);
        Task task2 = taskManager.getTask(taskId);
        assertEquals(task, task2);

        Epic epic = new Epic(TaskType.EPIC, "title", TaskStatus.NEW, "description", new Duration(10),
                LocalDateTime.now().plusMinutes(10));
        int epicId = taskManager.addEpic(epic);
        Epic epic2 = taskManager.getEpic(epicId);
        assertEquals(epic, epic2);

        Subtask subtask = new Subtask(TaskType.SUBTASK, "title", TaskStatus.NEW, "description", new Duration(10),
                LocalDateTime.now().plusMinutes(20));
        subtask.setEpicId(epicId);
        int subtaskId = taskManager.addSubtask(subtask);
        Subtask subtask2 = taskManager.getSubtask(subtaskId);
        assertEquals(subtask, subtask2);
    }


}