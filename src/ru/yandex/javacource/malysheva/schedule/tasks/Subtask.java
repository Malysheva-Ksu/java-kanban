package ru.yandex.javacource.malysheva.schedule.tasks;

import ru.yandex.javacource.malysheva.schedule.manager.TaskType;

import java.util.Objects;

public class Subtask extends Task {
    private int epicId;

    public Subtask(TaskType type, String title, TaskStatus status, String description) {
    super(type, title, status, description);
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subtask subtask = (Subtask) o;
        return getId() == subtask.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
