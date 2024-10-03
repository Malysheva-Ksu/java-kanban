package ru.yandex.javacource.malysheva.schedule;

import ru.yandex.javacource.malysheva.schedule.manager.TaskManager;
import ru.yandex.javacource.malysheva.schedule.manager.TaskStatus;
import ru.yandex.javacource.malysheva.schedule.tasks.Epic;
import ru.yandex.javacource.malysheva.schedule.tasks.Subtask;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;


public class Main {
    /* Внесла правки, но никак не получается проверка. Программа постоянно предлагает
    сделать методы статичными. Совсем не нахожу как справиться без "static".
     */

    public static void main(String[] args) {

        Task task1 = new Task("Task1", "description task1", TaskStatus.IN_PROGRESS);
        System.out.println("Задание добавлено. Идентификатор номер: " + TaskManager.addNewTask(task1));
        Task task2 = new Task("Task2", "description task2", TaskStatus.IN_PROGRESS);
        Epic epic1 = new Epic("epic1", "description epic1", TaskStatus.IN_PROGRESS);
        Epic epic2 = new Epic("epic2", "description epic2", TaskStatus.NEW);
        Subtask subtask1 = new Subtask("subtask1", "description subtask1", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("subtask2", "description subtask2", TaskStatus.NEW, epic1.getId());
        Subtask subtask3 = new Subtask("subtask3", "description subtask3", TaskStatus.NEW, epic2.getId());
        System.out.println(TaskManager.getTasks());









    }
}
