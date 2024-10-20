package ru.yandex.javacource.malysheva.schedule.manager;


import ru.yandex.javacource.malysheva.schedule.tasks.Epic;
import ru.yandex.javacource.malysheva.schedule.tasks.Subtask;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;

import java.util.ArrayList;

public interface TaskManager {
    //Добавить задачу.
    int addTask(Task task);

    //Добавить подзадачу.
    Integer addSubtask(Subtask subtask);

    //Добавить эпик.
    Integer addEpic(Epic epic);

    //Получение списка задач определенного класса
    ArrayList<Task> getTasks();

    ArrayList<Epic> getEpics();

    ArrayList<Subtask> getSubtasks();

    //Получение списка всех подзадач определённого эпика.
    ArrayList<Subtask> getEpicSubtasks(Epic epic);

    //Удаление всех задач
    void clearAll();

    void clearTasks();

    void clearSubtasks();

    void clearEpics();

    //Получение по идентификатору
    Task getTask(int id);

    Subtask getSubtask(int id);

    Epic getEpic(int id);

    //удаление по идентификатору
    void deleteTask(int id);

    void deleteSubtask(int id);

    void deleteEpic(int id);

    void updateSubtask(Subtask subtask);

    void updateEpic(Epic epic);

    // проверить и обновить статус эпика
    void updateEpicStatus(Integer epicId);

    ArrayList<Task> getHistory();
}
