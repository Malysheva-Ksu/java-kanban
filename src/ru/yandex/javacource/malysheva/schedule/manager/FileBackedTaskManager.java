package ru.yandex.javacource.malysheva.schedule.manager;

import ru.yandex.javacource.malysheva.schedule.tasks.Epic;
import ru.yandex.javacource.malysheva.schedule.tasks.Subtask;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;

import java.io.*;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final String HEADER = "id,type,name,status,description,epic";
    private int maxId = 0;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public int getNextId() {
        return ++maxId;
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(HEADER);
            writer.newLine();

            for (Task task : getTasks()) {
                writer.write(task.toString(task));
                writer.newLine();
            }

            for (Epic epic : getEpics()) {
                writer.write(epic.toString(epic));
                writer.newLine();
            }

            for (Subtask subtask : getSubtasks()) {
                writer.write(subtask.toString(subtask));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка во время сохранения файла", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine();

            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] parts = line.split(",");

                TaskType taskType = TaskType.valueOf(parts[1].toUpperCase());

                if (taskType == TaskType.EPIC) {
                    Epic epic = getEpic(line);

                        manager.addEpic(epic);

                } else if (taskType == TaskType.SUBTASK) {
                    Subtask subtask = getSubtask(line);

                        manager.addSubtask(subtask);
                } else if (taskType == TaskType.TASK) {
                    Task task = getTask(line);

                        manager.addTask(task);
                }

                int currentId = Integer.parseInt(parts[0].trim());
                if (currentId > manager.maxId) {
                    manager.maxId = currentId;
                }

                for (Map.Entry<Integer, Subtask> e : manager.subtasks.entrySet()) {
                    final Subtask subtask = e.getValue();
                    final Epic epic = manager.getEpics().get(subtask.getEpicId());
                    epic.addSubtaskId(subtask.getId());
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка при загрузке файла", e);
        }

        return manager;
    }

    private static Task getTask(String line) {
        String[] parts = line.split(",");

        int id = Integer.parseInt(parts[0].trim());
        TaskType type = TaskType.valueOf(parts[1].trim());
        String name = parts[2].trim();
        TaskStatus status = TaskStatus.valueOf(parts[3].trim());
        String description = parts[4].trim();

        Task task;
        task = new Task(name, description, status, type);
        task.setId(id);

        return task;
    }

    private static Subtask getSubtask(String line) {
        String[] parts = line.split(",");

        int id = Integer.parseInt(parts[0].trim());
        TaskType type = TaskType.valueOf(parts[1].trim());
        String name = parts[2].trim();
        TaskStatus status = TaskStatus.valueOf(parts[3].trim());
        String description = parts[4].trim();

        Subtask subtask;
        subtask = new Subtask(name, description, status, type);
        int epicId = Integer.parseInt(parts[5].trim());
        subtask.setId(id);
        subtask.setEpicId(epicId);

        return subtask;
    }

    private static Epic getEpic(String line) {
        String[] parts = line.split(",");

        int id = Integer.parseInt(parts[0].trim());
        TaskType type = TaskType.valueOf(parts[1].trim());
        String name = parts[2].trim();
        TaskStatus status = TaskStatus.valueOf(parts[3].trim());
        String description = parts[4].trim();

        Epic epic;
        epic = new Epic(name, description, status, type);
        epic.setId(id);

        return epic;
    }

    @Override
    public int addTask(Task task) {
        task.setId(getNextId());
        super.addTask(task);
        save();
        return task.getId();
    }

    @Override
    public Integer addSubtask(Subtask subtask) {
        subtask.setId(getNextId());
        super.addSubtask(subtask);
        save();
        return subtask.getId();
    }

    @Override
    public Integer addEpic(Epic epic) {
        epic.setId(getNextId());
        super.addEpic(epic);
        save();
        return epic.getId();
    }

    @Override
    public void clearAll() {
        super.clearAll();
        save();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateEpicStatus(Integer epicId) {
        super.updateEpicStatus(epicId);
        save();
    }

}
