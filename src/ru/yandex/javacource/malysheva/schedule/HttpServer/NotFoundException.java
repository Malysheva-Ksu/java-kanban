package ru.yandex.javacource.malysheva.schedule.HttpServer;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}