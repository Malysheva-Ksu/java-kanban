package ru.yandex.javacource.malysheva.schedule.tasks;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String title, String description, TaskStatus status, int epicId) {
        super(title, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() { // Нужен ли set?
        return epicId;
    }

    @Override
    public String toString() {
        return "Эпик: " + getEpicId() + ", Название задачи: " + getTitle() + ", Описание задачи: " + getDescription() + ", Идентификационный номер: " +
                getId() + ", Статус: " + getStatus() + "./ ";
    }

}
