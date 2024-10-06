package ru.yandex.javacource.malysheva.schedule.manager;

import ru.yandex.javacource.malysheva.schedule.tasks.Epic;
import ru.yandex.javacource.malysheva.schedule.tasks.Subtask;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;




public class TaskManager {
    public HashMap<Integer, Task> tasks = new HashMap<>();
    public HashMap<Integer, Epic> epics = new HashMap<>();
    public HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int generatorId = 0;


    //Добавить задачу.
    public int addTask(Task task) {
        if (task != null) {
            int id = ++generatorId;

            task.setId(id);
            tasks.put(id, task);

            return id;
        } else {
            return 0;
        }
    }

    //Добавить подзадачу.
    public Integer addSubtask(Subtask subtask) {
        if (subtask != null) {
            int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);

            if (epic == null) {
                return 0;
            }

            int id = ++generatorId;
            subtask.setId(id);
            subtasks.put(id, subtask);
            epic.addSubtaskId(id);
            updateEpicStatus(epicId);

            return id;
        } else {
            return 0;
        }
    }

    //Добавить эпик.
    public Integer addEpic(Epic epic) {
        int id = ++generatorId;
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }


    //Получение списка задач определенного класса
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    //Получение списка всех подзадач определённого эпика.
    public ArrayList<Subtask> getEpicSubtasks(Epic epic) {
        ArrayList <Subtask> subtasksInEpic = new ArrayList<>();
        for (int subId: epic.getSubtaskList()){
            subtasksInEpic.add(subtasks.get(subId));
        }
        return subtasksInEpic;
    }

    //Удаление всех задач
    public void clearAllTasks() {
        tasks.clear();
        subtasks.clear();
        epics.clear();
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void clearSubtasks() {
        for (Epic epic : epics.values()) {
            epic.cleanSubtaskIds();
            updateEpicStatus(epic.getId());
        }
        subtasks.clear();
    }

    public void clearEpics() {
        epics.clear();
        subtasks.clear();
    }

    //Получение по идентификатору
    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    //удаление по идентификатору
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) {
            return;
        }
        Epic epic = epics.get(subtask.getEpicId());
        epic.removeSubtask(id);
        updateEpicStatus(epic.getId());
    }


    public void deleteEpic(int id) {
        final Epic epic = epics.remove(id);
        if (epic == null) {
            return;
        }
        for (Integer subtaskId : epic.getSubtaskList()) {
            subtasks.remove(subtaskId);
        }
    }

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

    public void updateEpic(Epic epic) {
        Epic savedEpic = epics.get(epic.getId());
        if (savedEpic == null) {
            return;
        }
        savedEpic.setTitle(epic.getTitle());
        savedEpic.setDescription(epic.getDescription());
    }

    // проверить и обновить статус эпика
    private void updateEpicStatus(Integer epicId) {
        Epic epic = epics.get(epicId);
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


}
