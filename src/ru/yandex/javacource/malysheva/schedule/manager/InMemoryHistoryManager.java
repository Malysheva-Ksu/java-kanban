package ru.yandex.javacource.malysheva.schedule.manager;

import ru.yandex.javacource.malysheva.schedule.tasks.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    ArrayList<Task> historyManagerList = new ArrayList<>();

    @Override
    public void add(Task task) {
      //  if (!historyManagerList.contains(task)) , будущий код для избегания повторений
            historyManagerList.add(task);
    }

    @Override
    public ArrayList<Task> getHistory() {
        if (historyManagerList.size() > 10) {
            int taskTracker = 0;
            for (int i = historyManagerList.size() - 1; i >= 0; i--) {
                if (taskTracker < 10) {
                    taskTracker++;
                } else {
                    historyManagerList.remove(i);
                }
            }
        }
        return historyManagerList;
    }

}

