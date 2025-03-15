package ru.yandex.javacource.malysheva.schedule.manager;

import ru.yandex.javacource.malysheva.schedule.tasks.*;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.time.LocalDateTime;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final String HEADER = "id,type,name,status,description,epic";
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime,
            Comparator.nullsLast(Comparator.naturalOrder())));

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic,duration,startTime");
            writer.newLine();

            for (Task task : getTasks()) {
                writer.write(taskToString(task));
                writer.newLine();
            }

            for (Epic epic : getEpics()) {
                writer.write(taskToString(epic));
                writer.newLine();
            }

            for (Subtask subtask : getSubtasks()) {
                writer.write(taskToString(subtask));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    private String taskToString(Task task) {
        String epicId = task instanceof Subtask ?
                String.valueOf(((Subtask) task).getEpicId()) : "";

        return String.format("%d,%s,%s,%s,%s,%s,%d,%s",
                task.getId(),
                task.getType(),
                task.getTitle(),
                task.getStatus(),
                task.getDescription(),
                epicId,
                task.getDuration() != null ? task.getDuration().getMinutes() : 0,
                task.getStartTime() != null ? task.getStartTime().toString() : ""
        );
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        int localMaxId = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine();

            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] parts = line.split(",");

                TaskType taskType;
                try {
                    taskType = TaskType.valueOf(parts[1].toUpperCase());
                } catch (IllegalArgumentException e) {
                    continue;
                }

                Task task;
                try {
                    task = fromString(line);
                } catch (Exception e) {
                    continue;
                }

                if (taskType == TaskType.EPIC) {
                    manager.addEpic((Epic) task);
                } else if (taskType == TaskType.SUBTASK) {
                    manager.addSubtask((Subtask) task);
                } else if (taskType == TaskType.TASK) {
                    manager.addTask(task);
                }

                if (parts[0].trim().isEmpty()) {
                    continue;
                }

                int currentId;
                try {
                    currentId = Integer.parseInt(parts[0].trim());
                } catch (NumberFormatException e) {
                    continue;
                }

                if (currentId > localMaxId) {
                    localMaxId = currentId;
                }
            }

            for (Map.Entry<Integer, Subtask> e : manager.subtasks.entrySet()) {
                final Subtask subtask = e.getValue();
                final Epic epic = manager.epics.get(subtask.getEpicId());
                if (epic != null) {
                    epic.addSubtask(subtask);
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка при загрузке файла", e);
        }

        manager.generatorId = localMaxId;

        return manager;
    }

    public static String toString(Task task) {
        String taskString = task.getId() + "," + task.getType() + "," + task.getTitle() + "," +
                task.getStatus() + "," + task.getDescription() + ",";

        if (task.getType().equals(TaskType.SUBTASK)) {
            taskString += ((Subtask) task).getEpicId() + ",";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

            taskString += task.getDuration() + "," + task.getStartTime().format(formatter);

        return taskString;
    }

    private static Task fromString(String value) {
        try {
            String[] fields = value.split(",", -1);

            if (fields.length < 6) {
                return null;
            }

            int id = Integer.parseInt(fields[0]);
            TaskType type = TaskType.valueOf(fields[1]);
            String name = fields[2];
            TaskStatus status = TaskStatus.valueOf(fields[3]);
            String description = fields[4];

            Duration duration = new Duration(0);
            LocalDateTime startTime = null;

            if (fields.length > 6) {
                try {
                    duration = new Duration(Integer.parseInt(fields[6]));
                } catch (NumberFormatException e) {
                    duration = new Duration(0);
                }

                if (fields.length > 7 && !fields[7].isEmpty()) {
                    try {
                        startTime = LocalDateTime.parse(fields[7]);
                    } catch (DateTimeParseException e) {
                        startTime = null;
                    }
                }
            }

            Task task;
            switch (type) {
                case EPIC:
                    task = new Epic(type, name, status, description, duration, startTime);
                    break;
                case SUBTASK:
                    task = new Subtask(type, name, status, description, duration, startTime);
                    if (fields.length > 5 && !fields[5].isEmpty()) {
                        int epicId = Integer.parseInt(fields[5]);
                        ((Subtask) task).setEpicId(epicId);
                    }
                    break;
                default:
                    task = new Task(type, name, status, description, duration, startTime);
            }

            task.setId(id);
            return task;
        } catch (Exception e) {
            System.err.println("Ошибка при парсинге строки: " + value);
            e.printStackTrace();
            return null;
        }
    }

    private static Duration parseTaskDuration(String durationString) {
        if (durationString != null && !durationString.isEmpty()) {
            String[] durationParts = durationString.split(" ");
            if (durationParts.length == 2 && durationParts[1].equalsIgnoreCase("минут")) {
                int minutes = Integer.parseInt(durationParts[0].trim());
                return new Duration(minutes);
            } else {
                throw new IllegalArgumentException("Неправильный формат продолжительности: " + durationString);
            }
        }
        return null;
    }

    public boolean isTasksOverlapping(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null) {
            return false;
        }

        LocalDateTime endTime1 = task1.getEndTime();
        LocalDateTime endTime2 = task2.getEndTime();

        if (endTime1 == null) {
            return task1.getStartTime().isBefore(task2.getStartTime());
        }

        if (endTime2 == null) {
            return task2.getStartTime().isBefore(task1.getStartTime());
        }

        return task1.getStartTime().isBefore(endTime2) && task2.getStartTime().isBefore(endTime1);
    }

    @Override
    public int addTask(Task task) {
        if (task.getStartTime() == null) {
            throw new IllegalArgumentException("Время начала задачи должно быть задано.");
        }

        for (Task existingTask : getPrioritizedTasks()) {
            if (isTasksOverlapping(existingTask, task)) {
                throw new IllegalArgumentException("Задача пересекается с существующей задачей: " + existingTask.getTitle());
            }
        }

        int id = super.addTask(task);

        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }

        save();
        return id;
    }

    @Override
    public Integer addSubtask(Subtask subtask) {
        if (subtask.getStartTime() == null) {
            throw new IllegalArgumentException("Время начала задачи должно быть задано.");
        }

        for (Task existingTask : getPrioritizedTasks()) {
            if (isTasksOverlapping(existingTask, subtask)) {
                throw new IllegalArgumentException("Задача пересекается с существующей задачей: " + existingTask.getTitle());
            }
        }

        int id = super.addSubtask(subtask);

        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        save();
        return id;
    }

    @Override
    public Integer addEpic(Epic epic) {
        for (Task existingTask : getPrioritizedTasks()) {
            if (isTasksOverlapping(existingTask, epic)) {
                throw new IllegalArgumentException("Эпик пересекается с существующей задачей: " + existingTask.getTitle());
            }
        }

        int id = super.addEpic(epic);

        if (epic.getStartTime() != null) {
            prioritizedTasks.add(epic);
        }

        save();
        return id;
    }

    @Override
    public void clearAll() {
        super.clearAll();
        prioritizedTasks.clear();
        save();
    }

    @Override
    public void clearTasks() {
        List<Task> tasksForClear = super.getTasks();
        for (Task task: tasksForClear) {
            prioritizedTasks.remove(task);
        }

        super.clearTasks();
        save();
    }

    @Override
    public void clearSubtasks() {
        List<Subtask> subtasksForClear = super.getSubtasks();
        for (Task subtask: subtasksForClear) {
            prioritizedTasks.remove(subtask);
        }

        super.clearSubtasks();
        save();
    }

    @Override
    public void clearEpics() {
        List<Epic> epicsForClear = super.getEpics();
        for (Task epic: epicsForClear) {
            prioritizedTasks.remove(epic);
        }

        super.clearEpics();
        save();
    }

    @Override
    public void deleteTask(int id) {
        Task task = super.tasks.get(id);
        if (task != null) {
            prioritizedTasks.remove(task);
        }
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        Task subtask = super.subtasks.get(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
        }
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        Task epic = super.epics.get(id);
        if (epic != null) {
            prioritizedTasks.remove(epic);
        }
        super.deleteEpic(id);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask.getStartTime() != null) {
            prioritizedTasks.remove(subtask);
            prioritizedTasks.add(subtask);
        }

        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic.getStartTime() != null) {
            prioritizedTasks.remove(epic);
            prioritizedTasks.add(epic);
        }

        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateEpicStatus(Integer epicId) {
        super.updateEpicStatus(epicId);
        save();
    }

}