import java.util.ArrayList;
import java.util.HashMap;


public class TaskManager {
    public static HashMap<Integer, Task> tasks = new HashMap<>();
    public static HashMap<Integer, Epic> epics = new HashMap<>();
    public static HashMap<Integer, Subtask> subtasks = new HashMap<>();

    public static HashMap<Integer, Task> allTasks = new HashMap<>();


    /*Добавить подзадачу. Добавить ее в список эпика. Добавить в список всех задач
    с идентификационным номером*/
    public static void addSubtask(String title, String description, TaskStatus status, String epicTitle) {
        if (!epics.isEmpty()) {
            Subtask subtask = new Subtask(title, description, status, epicTitle);

            for (int key: epics.keySet()) {
                if ((epics.get(key).title).equals(epicTitle)) {
                    epics.get(key).epicSubtaskList.add(subtask);

                }
            }

            subtask.id = subtask.hashCode();
            allTasks.put(subtask.id, subtask);
            subtasks.put(subtask.id, subtask);
        }
    }

    //Добавить задачу. Добавить ее в список всех задач с идент. номером и в список tasks
    public static void addTask(String title, String description, TaskStatus status){
        Task task = new Task(title, description, status);
        task.id = task.hashCode();
        allTasks.put(task.id, task);
        tasks.put(task.id, task);
    }

    //Добавить эпик. Добавить в список всех задач с идент. номером и в список epics
    public static void addEpic(String title, String description, TaskStatus status){
        Epic epic = new Epic(title, description,status);
        epic.id = epic.hashCode();
        allTasks.put(epic.id, epic);
        epics.put(epic.id, epic);
    }

    //Получение списка всех задач. Возвращает строку
    public static String allTasksToString(){
        String allTasksResult = "";
        for (int key : allTasks.keySet()){
            allTasksResult += allTasks.get(key).toString();
        }
        return allTasksResult;
    }

    //Получение списка задач определенного класса
    public static ArrayList<Task> getTasksList(){
        ArrayList<Task> tasksList = new ArrayList<>();
        for (int taskKey: tasks.keySet()){
            tasksList.add(tasks.get(taskKey));
        }
        return tasksList;
    }

    public static ArrayList<Epic> getEpicsList(){
        ArrayList <Epic> epicsList = new ArrayList<>();
        for (int epicKey: epics.keySet()){
            epicsList.add(epics.get(epicKey));
        }
        return epicsList;
    }

    public static ArrayList<Subtask> getSubtasksList(){
        ArrayList <Subtask> subtaskList = new ArrayList<>();
        for (int subKey: subtasks.keySet()){
            subtaskList.add(subtasks.get(subKey));
        }
        return subtaskList;
    }

    //Получение списка всех подзадач определённого эпика.
    static ArrayList <Subtask> getEpicSubtasks(Epic epic){
        return epic.epicSubtaskList;
    }

    //Удаление всех задач
    public void clearTaskList (){
        allTasks.clear();
    }

    //Получение по идентификатору
    public Task getTaskFromNumber (int id){
        return allTasks.get(id);
    }

    //удаление по идентификатору
    public void deleteTask (int id){
        allTasks.remove(id);
    }

    //обновление статуса task
    public static void taskStatusUpdate (int id, TaskStatus newStatus) {
        if (tasks.get(id) != null) {
            (tasks.get(id)).setStatus(newStatus);
        }
    }

    //обновление статуса subtask
    public static void subtaskStatusUpdate (int id, TaskStatus newStatus) {
        subtasks.get(id).setStatus(newStatus);
        for (int epicKey : epics.keySet()) {
            for (int j = 0; j < epics.get(epicKey).epicSubtaskList.size(); j++) {
                if (epics.get(epicKey).epicSubtaskList.get(j).id == id) {
                    (epics.get(epicKey)).setStatus(TaskStatus.NEW);
                    for (int i = 0; i < epics.get(epicKey).epicSubtaskList.size(); i++) {
                        if (epics.get(epicKey).epicSubtaskList.get(i).getStatus() == TaskStatus.IN_PROGRESS) {
                            epics.get(epicKey).setStatus(TaskStatus.IN_PROGRESS);
                        } else if (epics.get(epicKey).epicSubtaskList.get(i).getStatus() == TaskStatus.DONE &&
                                epics.get(epicKey).getStatus() != TaskStatus.IN_PROGRESS) {
                            epics.get(epicKey).setStatus(TaskStatus.DONE);
                        }
                    }
                }
            }
        }
    }

    //обновление всего.
    public static void taskAllUpdate(int id, String updateTitle, String updateDescription, TaskStatus updateStatus) {
        allTasks.get(id).title = updateTitle;
        allTasks.get(id).description = updateDescription;
        if (allTasks.get(id).getClass() == Task.class) {
            allTasks.get(id).setStatus(updateStatus);
        } else if (allTasks.get(id).getClass() == Subtask.class) {
            allTasks.get(id).setStatus(updateStatus);
            for (int epicKey: epics.keySet()) {
                for (int k =0; k<epics.get(epicKey).epicSubtaskList.size(); k++) {
                    if (epics.get(epicKey).epicSubtaskList.get(k).equals(allTasks.get(id))) {
                        epics.get(epicKey).setStatus(TaskStatus.NEW);
                        for (Subtask subtasks : epics.get(epicKey).epicSubtaskList) {
                            if (subtasks.getStatus() == TaskStatus.IN_PROGRESS) {
                                epics.get(epicKey).setStatus(TaskStatus.IN_PROGRESS);
                            } else if (epics.get(epicKey).getStatus() != TaskStatus.IN_PROGRESS && subtasks.getStatus() == TaskStatus.DONE) {
                                epics.get(epicKey).setStatus(TaskStatus.DONE);
                            }
                        }
                    }
                }
            }
        }
    }


}
