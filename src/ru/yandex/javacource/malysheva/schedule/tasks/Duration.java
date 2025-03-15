package ru.yandex.javacource.malysheva.schedule.tasks;

public class Duration {
    private int minutes;
    public static final Duration ZERO = new Duration(0);

    public Duration(int minutes) {
        this.minutes = minutes;
    }

    public Duration plus(Duration other) {
        if (other == null) {
            return this;
        }
        return new Duration(this.minutes + other.minutes);
    }

    public int getMinutes() {
        return minutes;
    }

    @Override
    public String toString() {
        return minutes + " минут";
    }
}