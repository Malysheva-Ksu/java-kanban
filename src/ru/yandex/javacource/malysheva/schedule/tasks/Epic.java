package ru.yandex.javacource.malysheva.schedule.tasks;

import ru.yandex.javacource.malysheva.schedule.manager.TaskType;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subtaskIds = new ArrayList<>();

    public Epic(TaskType type, String title, TaskStatus status, String description) {
        super(type, title, status, description);
    }

    public void cleanSubtaskIds() {
        subtaskIds.clear();
    }

    public void removeSubtask(int id) {
        subtaskIds.remove(id);
    }

    public void addSubtaskId(Integer subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void setSubtaskIds(ArrayList<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public ArrayList<Integer> getSubtaskList() {
        if (!subtaskIds.isEmpty()) {
            return subtaskIds;
        }
        return null;
    }

}
