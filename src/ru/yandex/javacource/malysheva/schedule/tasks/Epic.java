package ru.yandex.javacource.malysheva.schedule.tasks;

import ru.yandex.javacource.malysheva.schedule.manager.TaskType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private ArrayList<Integer> subtaskIds = new ArrayList<>();
    private List<Subtask> subtasks;
    private LocalDateTime endTime;
    private Duration duration;
    private LocalDateTime startTime;

    public Epic(TaskType type, String title, TaskStatus status, String description, Duration duration, LocalDateTime startTime) {
        super(type, title, status, description, duration, startTime);
        this.duration = duration;
        this.startTime = startTime;
        this.subtasks = new ArrayList<>();
    }

    public void addSubtaskId(Integer subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void calculateDuration() {
        if (subtaskIds.isEmpty()) {
            this.duration = new Duration(0);
            this.startTime = null;
            this.endTime = null;
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

        this.duration = new Duration(totalDuration);
        this.startTime = earliestStartTime;
        this.endTime = latestEndTime;
    }

    public Duration getDuration() {
        calculateDuration();
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
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
        return null;
    }

    public ArrayList<Integer> getSubtaskIds() {
        if (!subtaskIds.isEmpty()) {
            return subtaskIds;
        }
        return null;
    }

}
