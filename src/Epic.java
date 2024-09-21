import java.util.ArrayList;

public class Epic extends Task {
    public ArrayList<Subtask> epicSubtaskList;

    public Epic(String title, String description, TaskStatus status) {
        super(title, description, status);
        epicSubtaskList = new ArrayList<>();

    }


}
