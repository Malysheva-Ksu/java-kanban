package ru.yandex.javacource.malysheva.schedule.manager;

import ru.yandex.javacource.malysheva.schedule.tasks.Epic;
import ru.yandex.javacource.malysheva.schedule.tasks.Subtask;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected int generatorId = 0;
    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public int addTask(Task task) {
        final int id = ++generatorId;
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    @Override
    public Integer addSubtask(Subtask subtask) {
        if (subtask != null) {
            int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);

            if (epic == null) {
                return 0;
            }

            if (epicId == subtask.getId()) {
                return 0;
            }

            int id = ++generatorId;
            subtask.setId(id);
            subtasks.put(id, subtask);

            epic.addSubtaskId(id);
            epic.addSubtask(subtask);
            updateEpicStatus(epicId);

            return id;
        } else {
            return 0;
        }
    }

    @Override
    public Integer addEpic(Epic epic) {
        if (epic.getSubtaskIds() != null && epic.getSubtaskIds().contains(epic.getId())) {
            return 0;
        }

        int id = ++generatorId;
            epic.setId(id);
            epics.put(id, epic);

        return id;
    }

    @Override
    public List<Task> getTasks() {
        return tasks.values().stream().toList();
    }

    @Override
    public List<Epic> getEpics() {
        return epics.values().stream().toList();
    }

    @Override
    public List<Subtask> getSubtasks() {
        return subtasks.values().stream().toList();
    }

    @Override
    public List<Subtask> getEpicSubtasks(Epic epic) {
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .toList();
    }

    @Override
    public Task getTask(int id) {
        final Task task = tasks.get(id);
        historyManager.addTask(task);
        return task;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = (Subtask) subtasks.get(id);

            historyManager.addTask(subtask);
            return subtask;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);

            historyManager.addTask(epic);
            return epic;
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
        epics.values().forEach(epic -> {
            epic.cleanSubtaskIds();
            updateEpicStatus(epic.getId());
        });
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
        epic.removeSubtask(subtask);
        updateEpicStatus(epic.getId());
    }

    @Override
    public void deleteEpic(int id) {
        final Epic epic = epics.remove(id);
        if (epic == null) {
            return;
        }
        for (Integer subtaskId : epic.getSubtaskIds()) {
            subtasks.remove(subtaskId);
        }
    }

    @Override
    public void updateTask(Task task) {
        int id = task.getId();
        Task savedTask = tasks.get(id);

        if (savedTask == null) {
            return;
        }

        savedTask.setTitle(task.getTitle());
        savedTask.setDescription(task.getDescription());
        savedTask.setStatus(task.getStatus());
        savedTask.setDuration(task.getDuration());
        savedTask.setStartTime(task.getStartTime());

        tasks.put(id, savedTask);
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
        final Epic savedEpic = epics.get(epic.getId());
        if (savedEpic == null) {
            return;
        }
        epic.setSubtaskIds(savedEpic.getSubtaskIds());
        epic.setStatus(savedEpic.getStatus());
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateEpicStatus(Integer epicId) {
        Epic epic = epics.get(epicId);

        if (epic == null) return;

        List<TaskStatus> statuses = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .map(Subtask::getStatus)
                .toList();

        if (statuses.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }
        if (statuses.stream().allMatch(status -> status == TaskStatus.DONE)) {
            epic.setStatus(TaskStatus.DONE);
        } else if (statuses.stream().anyMatch(status -> status == TaskStatus.IN_PROGRESS) ||
                (statuses.stream().anyMatch(status -> status == TaskStatus.DONE) &&
                        statuses.stream().anyMatch(status -> status == TaskStatus.NEW))) {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        } else {
            epic.setStatus(TaskStatus.NEW);
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = historyManager.getHistory();
        return new ArrayList<>(history);
    }

}