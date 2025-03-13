package ru.yandex.javacource.malysheva.schedule.tasks;

import ru.yandex.javacource.malysheva.schedule.manager.TaskType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private ArrayList<Integer> subtaskIds = new ArrayList<>();
    private List<Subtask> subtasks = new ArrayList<>();
    private LocalDateTime endTime;


    public Epic(TaskType type, String title, TaskStatus status, String description, Duration duration, LocalDateTime startTime) {
        super(type, title, status, description, duration, startTime);
        this.subtasks = new ArrayList<>();
        this.subtaskIds = new ArrayList<>();
    }

    public void addSubtaskId(Integer subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void calculateDuration() {
        if (subtasks == null) {
            subtasks = new ArrayList<>();
        }

        if (subtaskIds == null) {
            setDuration(new Duration(0));
            setStartTime(LocalDateTime.now());
            endTime = getStartTime().plusMinutes(getDuration().getMinutes());
            return;
        }

        int totalDuration = 0;
        LocalDateTime earliestStartTime = LocalDateTime.MAX;
        LocalDateTime latestEndTime = LocalDateTime.MIN;

        for (Subtask subtask: subtasks) {
            totalDuration += subtask.getDuration().getMinutes();
            if (subtask.getStartTime() != null) {
                if (subtask.getStartTime().isBefore(earliestStartTime)) {
                    earliestStartTime = subtask.getStartTime();
                }
            }
            if (subtask.getEndTime().isAfter(latestEndTime)) {
                latestEndTime = subtask.getEndTime();
            }
        }

        setDuration(new Duration(totalDuration));
        setStartTime(earliestStartTime);
        this.endTime = latestEndTime;
    }

    public Duration getDuration() {
        calculateDuration();
        return getDuration();
    }

    public LocalDateTime getStartTime() {
        return getStartTime();
    }

    public LocalDateTime getEndTime() {

        return endTime;
    }

    public void cleanSubtaskIds() {
        subtaskIds.clear();
        subtasks.clear();
    }

    public void removeSubtask(Task subtask) {
        subtaskIds.remove(subtask.getId());
        subtasks.remove(subtask);
    }

    public void addSubtask(Subtask subtask) {
        subtaskIds.add(subtask.getId());
        subtasks.add(subtask);
    }

    public void setSubtaskIds(ArrayList<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public List<Subtask> getSubtasks() {
        if (!subtasks.isEmpty()) {
            return subtasks;
        }
        return new ArrayList<>();
    }

    public ArrayList<Integer> getSubtaskIds() {
        if (!(subtaskIds == null)) {
            return subtaskIds;
        }
        return new ArrayList<>();
    }

}