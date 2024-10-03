package ru.yandex.javacource.malysheva.schedule.tasks;

import java.util.ArrayList;

import ru.yandex.javacource.malysheva.schedule.manager.TaskStatus;

public class Epic extends Task {
    private ArrayList<Integer> SubtaskList; //в листе хранятся идентификаторы сабтасков

    public Epic(String title, String description, TaskStatus status) {
        super(title, description, status);
        // epicSubtaskList = new ArrayList<>();

    }

    public void addSubtaskId(Integer subtaskId) {
        SubtaskList.add(subtaskId);
    }

    public ArrayList<Integer> getSubtaskList() {
        return SubtaskList;
    }



}
