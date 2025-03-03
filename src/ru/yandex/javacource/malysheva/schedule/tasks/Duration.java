package ru.yandex.javacource.malysheva.schedule.tasks;

public class Duration {
    private int minutes;

    public Duration(int minutes) {
        this.minutes = minutes;
    }

    public int getMinutes() {
        return minutes;
    }

    @Override
    public String toString() {
        return minutes + " минут";
    }
}
