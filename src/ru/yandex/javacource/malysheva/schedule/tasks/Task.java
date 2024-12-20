package ru.yandex.javacource.malysheva.schedule.tasks;


import ru.yandex.javacource.malysheva.schedule.manager.TaskType;

import java.util.Objects;

public class Task {
    private String title;
    private String description;
    private int id;
    private TaskStatus status;
    private TaskType type;
    private Integer epicId;


    public Task(String title, String description, TaskStatus status, TaskType type) {
        this.title = title;
        this.description = description;
        this.setStatus(status);
        this.type = type;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int id) {
        System.out.println("Установить id эпика можно только для subtask");
    }

    public static Task fromString (String value) {
        String[] parts = value.split(",");

        if (parts.length < 5) {
            throw new IllegalArgumentException("недостаточно данный для создания задачи");
        }

            int id = Integer.parseInt(parts[0].trim());
            TaskType type = TaskType.valueOf(parts[1].trim());
            String name = parts[2].trim();
            TaskStatus status = TaskStatus.valueOf(parts[3].trim());
            String description = parts[4].trim();

            Task task = new Task(name, description, status, type);

            if (parts.length == 6) {
                int epicId = Integer.parseInt(parts[5].trim());
                task.epicId = epicId;
            }
            return task;
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

    @Override
    public String toString() {
        return id + ", " + type + ", " + title + ", " + status + ", " + description;
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
