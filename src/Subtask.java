public class Subtask extends Task{
    public String epicTitle;

    public Subtask(String title, String description, TaskStatus status, String epicTitle ) {
        super(title, description, status);
        this.epicTitle = epicTitle;
    }

}
