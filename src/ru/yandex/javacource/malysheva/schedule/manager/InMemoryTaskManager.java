package ru.yandex.javacource.malysheva.schedule.manager;

import ru.yandex.javacource.malysheva.schedule.tasks.Epic;
import ru.yandex.javacource.malysheva.schedule.tasks.Subtask;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int generatorId = 0;
    InMemoryHistoryManager history = new InMemoryHistoryManager();

    @Override
    public int addTask(Task task) {
        if (task != null) {
            int id = ++generatorId;

            while (tasks.containsKey(id)) {
                id = ++generatorId;
            }

            if (!tasks.containsKey(id)) {
                task.setId(id);
                tasks.put(id, task);
            }


            return id;
        } else {
            return 0;
        }
    }

    @Override
    public Integer addSubtask(Subtask subtask) {
        if (subtask != null) {
            int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);

            if (epic == null) {
                return 0;
            }

            int id = ++generatorId;

            while (subtasks.containsKey(id)) {
                id = ++generatorId;
            }

            if (!subtasks.containsKey(id)) {
                subtask.setId(id);
                subtasks.put(id, subtask);
            }

            if (epicId == subtask.getId()) {
                return 0;
            }

            epic.addSubtaskId(id);
            updateEpicStatus(epicId);

            return id;
        } else {
            return 0;
        }
    }

    @Override
    public Integer addEpic(Epic epic) {
        int id = ++generatorId;

        while (epics.containsKey(id)) {
            id = ++generatorId;
        }

        if (!epics.containsKey(id)) {
            epic.setId(id);
            epics.put(id, epic);
        }

        return id;
    }

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(Epic epic) {
        ArrayList<Subtask> subtasksInEpic = new ArrayList<>();

        for (int subId : epic.getSubtaskList()) {
            subtasksInEpic.add(subtasks.get(subId));
        }
        return subtasksInEpic;
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);

        if (task != null) {
            history.add(task);
            return task;
        }
        return null;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);

        if (subtask != null) {
            history.add(subtask);
            return subtask;
        }
        return null;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);

        if (epic != null) {
            history.add(epic);
            return epic;
        }
        return null;
    }

    @Override
    public void clearAll() {
        tasks.clear();
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void clearTasks() {
        tasks.clear();
    }

    @Override
    public void clearSubtasks() {
        for (Epic epic : epics.values()) {
            epic.cleanSubtaskIds();
            updateEpicStatus(epic.getId());
        }
        subtasks.clear();
    }

    @Override
    public void clearEpics() {
        subtasks.clear();
        epics.clear();
    }


    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);

        if (subtask == null) {
            return;
        }

        Epic epic = epics.get(subtask.getEpicId());
        epic.removeSubtask(id);
        updateEpicStatus(epic.getId());
    }

    @Override
    public void deleteEpic(int id) {
        final Epic epic = epics.remove(id);
        if (epic == null) {
            return;
        }
        for (Integer subtaskId : epic.getSubtaskList()) {
            subtasks.remove(subtaskId);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        int id = subtask.getId();
        int epicId = subtask.getEpicId();
        Subtask savedSubtask = subtasks.get(id);

        if (savedSubtask == null) {
            return;
        }

        Epic epic = epics.get(epicId);

        if (epic == null) {
            return;
        }

        subtasks.put(id, subtask);
        updateEpicStatus(epicId);
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic savedEpic = epics.get(epic.getId());

        if (savedEpic == null) {
            return;
        }

        savedEpic.setTitle(epic.getTitle());
        savedEpic.setDescription(epic.getDescription());
    }

    @Override
    public void updateEpicStatus(Integer epicId) {
        Epic epic = epics.get(epicId);

        if (epic == null) return;

        int checkInt = 0;
        for (int id : epic.getSubtaskList()) {
            Subtask subtask = subtasks.get(id);

            if (subtask.getStatus() == TaskStatus.NEW && !(checkInt == 2)) {
                checkInt = 1;
            } else if (subtask.getStatus() == TaskStatus.IN_PROGRESS && !(checkInt == 1)) {
                checkInt = 2;
            }

        }

        if (checkInt == 0) {
            epic.setStatus(TaskStatus.DONE);
        } else if (checkInt == 1) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    @Override
    public ArrayList<Task> getHistory() {
        return history.getHistory();
        }

    }





