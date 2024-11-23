package ru.yandex.javacource.malysheva.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;

import java.util.ArrayList;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager manager;

    @BeforeEach
    public void setUp() {
        manager = new InMemoryHistoryManager();
    }

    @Test
    public void addAndGetTasksTest() {
        Task task1 = new Task("1", "Задача 1", TaskStatus.NEW);
        Task task2 = new Task("2", "Задача 2", TaskStatus.NEW);
        Task task3 = new Task("3", "Задача 3", TaskStatus.NEW);

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        List<Task> tasks = manager.getHistory();
        assertEquals(3, tasks.size(), "tasks size");
        assertEquals("Задача 1", tasks.get(0).getDescription(), "task1");
        assertEquals("Задача 2", tasks.get(1).getDescription());
        assertEquals("Задача 3", tasks.get(2).getDescription());
    }

    @Test
    public void removeTaskTest() {
        Task task1 = new Task("1", "Задача 1", TaskStatus.NEW);
        Task task2 = new Task("2", "Задача 2", TaskStatus.NEW);
        Task task3 = new Task("3", "Задача 3", TaskStatus.NEW);

        task1.setId(122);
        task2.setId(123);
        task3.setId(124);

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        int removeId = task2.getId();

        manager.remove(removeId);

        List<Task> tasks = manager.getTasks();
        assertEquals(3, tasks.size());
        assertEquals("Задача 1", tasks.get(0).getDescription());
        assertEquals("Задача 3", tasks.get(1).getDescription());
    }

    @Test
    public void notRetainOldIdsAfterRemovalTest() {
        boolean containsOldId = false;

        Task task1 = new Task("1", "Задача 1", TaskStatus.NEW);
        Task task2 = new Task("2", "Задача 2", TaskStatus.NEW);
        manager.addTask(task1);
        manager.addTask(task2);

        int removeId = task1.getId();
        manager.remove(removeId);
        List<Task> tasks = manager.getHistory();

        for (Task task : tasks) {
            if (task.getId() == removeId) {
                containsOldId = true;
                break;
            }
        }

        assertFalse(containsOldId, "old id contain");

    }


    @Test
    public void afterUpdatingTaskTest() {
        Task task1 = new Task("1", "Задача 1", TaskStatus.NEW);
        manager.addTask(task1);

        task1.setDescription("Измененная задача 1");
        List<Task> tasks = manager.getHistory();

        assertEquals("Измененная задача 1", tasks.get(0).getDescription(), "неактуальное описание");
    }

    @Test
    public void epicTaskIntegrityTest() {
        class Epic {
            private int id;
            private String description;
            private List<Integer> subtaskIds;

            public Epic(int id, String description) {
                this.id = id;
                this.description = description;
                this.subtaskIds = new ArrayList<>();
            }

            public void addSubtask(int subtaskId) {
                subtaskIds.add(subtaskId);
            }

            public void removeSubtask(int subtaskId) {
                subtaskIds.remove(Integer.valueOf(subtaskId));
            }

            public List<Integer> getSubtaskIds() {
                return subtaskIds;
            }
        }

        Epic epic = new Epic(1, "Эпик Задача 1");
        epic.addSubtask(2);
        epic.addSubtask(3);

        manager.remove(2);

        assertFalse(epic.getSubtaskIds().contains(2));
    }


}