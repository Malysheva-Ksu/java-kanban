package ru.yandex.javacource.malysheva.schedule.manager;

import ru.yandex.javacource.malysheva.schedule.tasks.Epic;
import ru.yandex.javacource.malysheva.schedule.tasks.Subtask;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;




public class TaskManager {
    public HashMap<Integer, Task> tasks = new HashMap<>();
    public HashMap<Integer, Epic> epics = new HashMap<>();
    public HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int generatorId = 0;


    //Добавить задачу.
    public Integer addNewTask(Task task) {
        int id = ++generatorId;
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    //Добавить подзадачу.
    public Integer addSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return null;
        }
        int id = ++generatorId;
        subtask.setId(id);
        subtasks.put(id, subtask);
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epicId);
        return id;
    }

    //Добавить эпик.
    public Integer addEpic(Epic epic) {
        int id = ++generatorId;
        epic.setId(id);
        tasks.put(id, epic);
        return id;
    }

    // проверить и обновить статус эпика
    public void updateEpicStatus(Integer epicId) {
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
        return  subtasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    //удаление по идентификатору
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    public void deleteSubtask(int id) {
        subtasks.remove(id);
    }

    public void deleteEpic(int id) {
        epics.remove(id);
    }

    public void updateTask(Task task) {
        int id = task.getId();
        Task savedTask = tasks.get(id);
        if (savedTask == null) {
            return;
        }
        tasks.put(id, task);
    }

    public void updateSubtask(Subtask subtask) {
        int id = subtask.getId();
        Subtask savedSubtask = subtasks.get(id);
        if (savedSubtask == null) {
            return;
        }
        subtasks.put(id, subtask);
    }

}
