package ru.yandex.javacource.malysheva.schedule.manager;

import ru.yandex.javacource.malysheva.schedule.tasks.Task;

import java.util.ArrayList;

public interface HistoryManager {

    public void add(Task task);

    public ArrayList<Task> getHistory();

}
