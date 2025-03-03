package ru.yandex.javacource.malysheva.schedule.manager;

import ru.yandex.javacource.malysheva.schedule.tasks.*;

import java.io.*;
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
            writer.write(HEADER);
            writer.newLine();

            for (Task task : getTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }

            for (Task epic : getEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }

            for (Task subtask : getSubtasks()) {
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

                TaskType taskType;
                try {
                    taskType = TaskType.valueOf(parts[1].toUpperCase());
                } catch (IllegalArgumentException e) {
                    continue;
                }

                Task task;
                try {
                    task = taskFromString(line);
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

            taskString += task.getDuration() + "," + task.getStartTime();

        return taskString;
    }

    public static Task taskFromString(String value) {
        final String[] keyValuePairs = value.split(",");

        if (keyValuePairs.length < 6) {
            throw new IllegalArgumentException("Неправильный формат строки: " + value);
        }

        int id = Integer.parseInt(keyValuePairs[0].trim());
        TaskType type = TaskType.valueOf(keyValuePairs[1].trim().toUpperCase());
        String name = keyValuePairs[2].trim();
        TaskStatus status = TaskStatus.valueOf(keyValuePairs[3].trim().toUpperCase());
        String description = keyValuePairs[4].trim();
        Duration duration = null;
        LocalDateTime startTime = null;

        if (type == TaskType.TASK || type == TaskType.EPIC) {
            if (keyValuePairs.length < 7) {
                throw new IllegalArgumentException("Необходимые параметры отсутствуют для задачи или эпика: " + value);
            }

            duration = parseTaskDuration(keyValuePairs[5].trim());

            startTime = LocalDateTime.parse(keyValuePairs[6].trim());
        } else if (type == TaskType.SUBTASK) {
            if (keyValuePairs.length < 8) {
                throw new IllegalArgumentException("Подзадача требует указания ID эпика и параметров: " + value);
            }

            int epicId = Integer.parseInt(keyValuePairs[5].trim());
            duration = parseTaskDuration(keyValuePairs[6].trim());
            startTime = LocalDateTime.parse(keyValuePairs[7].trim());

            Subtask subtask = new Subtask(type, name, status, description, duration, startTime);
            subtask.setEpicId(epicId);
            subtask.setId(id);
            return subtask;
        }

        Epic epic = new Epic(type, name, status, description, duration, startTime);
        epic.setId(id);
        return epic;
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
