package ru.yandex.javacource.malysheva.schedule.manager;

import ru.yandex.javacource.malysheva.schedule.tasks.Epic;
import ru.yandex.javacource.malysheva.schedule.tasks.Subtask;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final String HEADER = "id,type,name,status,description,epic";
    private int maxId = 0;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(HEADER);
            writer.newLine();

            for (Task task : getTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }

            for (Epic epic : getEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }

            for (Subtask subtask : getSubtasks()) {
                writer.write(toString(subtask));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка во время сохранения файла", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        int localMaxId = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine();

            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] parts = line.split(",");

                TaskType taskType = TaskType.valueOf(parts[1].toUpperCase());

                if (taskType == TaskType.EPIC) {
                    Epic epic = (Epic) taskFromString(line);

                    manager.addEpic(epic);

                } else if (taskType == TaskType.SUBTASK) {
                    Subtask subtask = (Subtask) taskFromString(line);

                    manager.addSubtask(subtask);

                } else if (taskType == TaskType.TASK) {
                    Task task = taskFromString(line);

                    manager.addTask(task);
                }

                int currentId = Integer.parseInt(parts[0].trim());
                if (currentId > localMaxId) {
                    localMaxId = currentId;
                }

                for (Map.Entry<Integer, Subtask> e : manager.subtasks.entrySet()) {
                    final Subtask subtask = e.getValue();
                    final Epic epic = manager.epics.get(subtask.getEpicId());
                    epic.addSubtaskId(subtask.getId());
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка при загрузке файла", e);
        }

        manager.maxId = localMaxId;

        return manager;
    }

    public static String toString(Task task) {
        return task.getId() + "," + task.getType() + "," + task.getTitle() + "," + task.getStatus() + "," +
                task.getDescription() + "," + (task.getType().equals(TaskType.SUBTASK) ? ((Subtask) task).getEpicId() : "");
    }

    public static Task taskFromString(String value) {
        final String[] values = value.split(",");
        final int id = Integer.parseInt(values[0]);
        final TaskType type = TaskType.valueOf(values[1]);
        final String name = values[2];
        final TaskStatus status = TaskStatus.valueOf(values[3]);
        final String description = values[4];

        if (type == TaskType.TASK) {
            Task task = new Task(type, name, status,description);
            task.setId(id);
            return task;
        }
        if (type == TaskType.SUBTASK) {
            final int epicId = Integer.parseInt(values[5]);
            Subtask subtask = new Subtask(type, name, status, description);
            subtask.setEpicId(epicId);
            return subtask;

        }
        Epic epic = new Epic(type, name, status, description);
        epic.setId(id);
        return epic;
    }

    @Override
    public int addTask(Task task) {
        super.addTask(task);
        save();
        return task.getId();
    }

    @Override
    public Integer addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
        return subtask.getId();
    }

    @Override
    public Integer addEpic(Epic epic) {
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
