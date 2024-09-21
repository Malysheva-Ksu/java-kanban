import java.util.Objects;

public class Task {
    public  String title;
    public String description;
    public int id;
    private TaskStatus status;


    public Task(String title, String description, TaskStatus status){
        this.title = title;
        this.description = description;
        this.setStatus(status);
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
        return Objects.equals(title, task.title) &&
                Objects.equals(description, task.description);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        if (title != null) {
            hash = hash + title.hashCode();
        }
        hash = hash * 31;

        if (description != null) {
            hash = hash + description.hashCode();
        }
        return hash;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
