package ru.yandex.javacource.malysheva.schedule;

import ru.yandex.javacource.malysheva.schedule.manager.TaskManager;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;
import ru.yandex.javacource.malysheva.schedule.tasks.Epic;
import ru.yandex.javacource.malysheva.schedule.tasks.Subtask;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;


public class Main {
    /* Внесла правки, но никак не получается проверка. Программа постоянно предлагает
    сделать методы статичными. Совсем не нахожу как справиться без "static".
     */

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        Task task1 = new Task("Task1", "description task1", TaskStatus.IN_PROGRESS);
        int task1Id = taskManager.addTask(task1);
        System.out.println("Задание добавлено. Идентификатор номер: " + task1Id);

        Task task2 = new Task("Task2", "description task2", TaskStatus.IN_PROGRESS);
        int task2Id = taskManager.addTask(task2);
        System.out.println("Задание добавлено. Идентификатор номер: " + task2Id);

        Epic epic1 = new Epic("epic1", "description epic1", TaskStatus.IN_PROGRESS);
        int epic1Id = taskManager.addEpic(epic1);
        System.out.println(epic1Id);

        Epic epic2 = new Epic("epic2", "description epic2", TaskStatus.NEW);
        int epic2Id = taskManager.addEpic(epic2);
        System.out.println(epic2Id);

        Subtask subtask1 = new Subtask("subtask1", "description subtask1", TaskStatus.NEW, epic1Id);
        int sub1Id = taskManager.addSubtask(subtask1);
        System.out.println(sub1Id);

        Subtask subtask2 = new Subtask("subtask2", "description subtask2", TaskStatus.NEW, epic1Id);
        int sub2Id = taskManager.addSubtask(subtask2);

        Subtask subtask3 = new Subtask("subtask3", "description subtask3", TaskStatus.NEW, epic2Id);
        int sub3Id = taskManager.addSubtask(subtask3);

        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getSubtasks());
        System.out.println(taskManager.getEpics());

        task1.setStatus(TaskStatus.IN_PROGRESS);
        System.out.println(task1);

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);
        System.out.println(subtask1);
        System.out.println(epic1);

        subtask3.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask3);
        System.out.println(epic2);

        taskManager.deleteEpic(3);

        System.out.println(taskManager.getEpics());

    }
}
