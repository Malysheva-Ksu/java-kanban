package ru.yandex.javacource.malysheva.schedule;

import ru.yandex.javacource.malysheva.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacource.malysheva.schedule.manager.Managers;
import ru.yandex.javacource.malysheva.schedule.manager.TaskType;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;
import ru.yandex.javacource.malysheva.schedule.tasks.Epic;
import ru.yandex.javacource.malysheva.schedule.tasks.Subtask;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;


public class Main {


    public static void main(String[] args) {
        InMemoryTaskManager taskManager = (InMemoryTaskManager) Managers.getDefault();

        Task task1 = new Task("Task1", "description task1", TaskStatus.IN_PROGRESS, TaskType.TASK);
        int task1Id = taskManager.addTask(task1);
        System.out.println("task1 добавлено. Идентификатор номер: " + task1Id);

        Task task2 = new Task("Task2", "description task2", TaskStatus.IN_PROGRESS, TaskType.TASK);
        int task2Id = taskManager.addTask(task2);
        System.out.println("task2 добавлено. Идентификатор номер: " + task2Id);

        Epic epic1 = new Epic("epic1", "description epic1", TaskStatus.IN_PROGRESS, TaskType.EPIC);
        int epic1Id = taskManager.addEpic(epic1);
        System.out.println("epic1 добавлено. Идентификатор номер: " + epic1Id);

        Epic epic2 = new Epic("epic2", "description epic2", TaskStatus.NEW, TaskType.EPIC);
        int epic2Id = taskManager.addEpic(epic2);
        System.out.println("epic2 добавлено. Идентификатор номер: " + epic2Id);

        Subtask subtask1 = new Subtask("subtask1", "description subtask1", TaskStatus.NEW, TaskType.SUBTASK);
        int sub1Id = taskManager.addSubtask(subtask1);
        System.out.println("subtask1 добавлено. Идентификатор номер: " + sub1Id);

        Subtask subtask2 = new Subtask("subtask2", "description subtask2", TaskStatus.NEW, TaskType.SUBTASK);
        int sub2Id = taskManager.addSubtask(subtask2);
        System.out.println("subtask2 добавлено. Идентификатор номер: " + sub2Id);

        Subtask subtask3 = new Subtask("subtask3", "description subtask3", TaskStatus.NEW, TaskType.SUBTASK);
        int sub3Id = taskManager.addSubtask(subtask3);
        System.out.println("subtask3 добавлено. Идентификатор номер: " + sub3Id);

        System.out.println("Вызываем просмотр task1");
        taskManager.getTask(1);
        System.out.println("История просмотров:");
        taskManager.getHistory();

        System.out.println("Вызываем просмотр epic1");
        taskManager.getEpic(3);
        System.out.println("История просмотров:");
        taskManager.getHistory();

        System.out.println("Вызываем просмотр subtask1");
        taskManager.getSubtask(5);
        System.out.println("История просмотров:");
        taskManager.getHistory();

        System.out.println("Делаем просмотры более 10шт.");
        taskManager.getEpic(4);
        taskManager.getEpic(4);
        taskManager.getEpic(4);
        taskManager.getEpic(4);
        taskManager.getEpic(4);
        taskManager.getEpic(4);
        taskManager.getEpic(4);
        taskManager.getEpic(4);
        taskManager.getHistory();

    }
}
