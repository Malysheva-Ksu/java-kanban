package ru.yandex.javacource.malysheva.schedule.tasks;


import ru.yandex.javacource.malysheva.schedule.manager.TaskType;

import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private String title;
    private String description;
    private int id;
    private TaskStatus status;
    private TaskType type;
    private Duration duration;
    private LocalDateTime startTime;


    public Task(TaskType type, String title, TaskStatus status, String description, Duration duration, LocalDateTime startTime) {
        this.title = title;
        this.description = description;
        this.setStatus(status);
        this.type = type;
        this.duration = duration;
        this.startTime = startTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return startTime.plusMinutes(duration.getMinutes());
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setEpicId(int id) {
        System.out.println("Установить id эпика можно только для subtask");
    }

    public String getTitle() {
        return  title;
    }

    public void setTitle(String newTitle) {
        title = newTitle;
    }

    public TaskType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String newDescription) {
        description = newDescription;
    }

    public int getId() {
        return id;
    }

    public void setId(Integer newId) {
        id = newId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public static String toString(Task task) {
        return task.getId() + "," + task.getType() + "," + task.getTitle() + "," + task.getStatus() + "," +
                task.getDescription() + "," + (task.getType().equals(TaskType.SUBTASK) ? ((Subtask) task).getEpicId() : "");
    }

    @Override
    public String toString() {
        return "Тип задачи: " + type + " Название: " + title + " Статус: " + status + " Описание: " + description
                + " ID: " + id + " Продолжительность: " + duration + " Время начала: " + startTime
                + ", Время завершения: " + getEndTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }




}
