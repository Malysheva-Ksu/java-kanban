package ru.yandex.javacource.malysheva.schedule.tasks;


import java.util.Objects;

public class Task {
    private String title;
    private String description;
    private int id;
    private TaskStatus status;


    public Task(String title, String description, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.setStatus(status);
    }

    public String getTitle() {
        return  title;
    }

    public void setTitle(String newTitle) {
        title = newTitle;
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

    @Override
    public String toString() {
        return "Название задачи: " + title + ", Описание задачи: " + description + ", Идентификационный номер: " +
                id + ", Статус: " + getStatus() + "./ ";
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
        return Objects.hash(title, description, id, status); // Включение id в hashCode
    }




}
