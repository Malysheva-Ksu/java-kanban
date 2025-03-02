package ru.yandex.javacource.malysheva.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacource.malysheva.schedule.tasks.Duration;
import ru.yandex.javacource.malysheva.schedule.tasks.Task;
import ru.yandex.javacource.malysheva.schedule.tasks.TaskStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager manager;

    @BeforeEach
    public void setUp() {
        manager = new InMemoryHistoryManager();
    }

    @Test
    public void testEmptyHistory() {
        assertTrue(manager.getHistory().isEmpty(), "История должна быть пустой");
    }

    @Test
    public void testDuplicateTaskInHistory() {
        Task task = new Task(TaskType.TASK, "Задача 1", TaskStatus.NEW,
                "Описание задачи 1", new Duration(30), LocalDateTime.now());

        manager.addTask(task);
        manager.addTask(task);

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "История должна содержать только одну задачу");
        assertEquals(task, history.get(0), "История должна содержать добавленную задачу");
    }

    @Test
    public void testRemoveTaskFromHistory_Start() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Task task1 = new Task(TaskType.TASK, "Задача 1", TaskStatus.NEW,
                "Описание задачи 1", new Duration(30), LocalDateTime.now());
        Task task2 = new Task(TaskType.TASK, "Задача 2", TaskStatus.NEW,
                "Описание задачи 2", new Duration(30), LocalDateTime.now().plusMinutes(30));

        taskManager.addTask(task1);
        taskManager.addTask(task2);
        manager.addTask(task1);
        manager.addTask(task2);

        manager.remove(task1.getId());

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу после удаления");
        assertEquals(task2, history.get(0), "История должна содержать только задачу 2");
    }

    @Test
    public void testRemoveTaskFromHistory_End() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Task task1 = new Task(TaskType.TASK, "Задача 1", TaskStatus.NEW,
                "Описание задачи 1", new Duration(30), LocalDateTime.now());
        Task task2 = new Task(TaskType.TASK, "Задача 2", TaskStatus.NEW,
                "Описание задачи 2", new Duration(30), LocalDateTime.now().plusMinutes(30));

        taskManager.addTask(task1);
        taskManager.addTask(task2);
        manager.addTask(task1);
        manager.addTask(task2);

        manager.remove(task2.getId());

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу после удаления");
        assertEquals(task1, history.get(0), "История должна содержать только задачу 1");
    }

    @Test
    public void testRemoveTaskFromHistory_Middle() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Task task1 = new Task(TaskType.TASK, "Задача 1", TaskStatus.NEW,
                "Описание задачи 1", new Duration(30), LocalDateTime.now());
        Task task2 = new Task(TaskType.TASK, "Задача 2", TaskStatus.NEW,
                "Описание задачи 2", new Duration(30), LocalDateTime.now().plusMinutes(30));
        Task task3 = new Task(TaskType.TASK, "Задача 3", TaskStatus.NEW,
                "Описание задачи 3", new Duration(30), LocalDateTime.now().plusMinutes(60));

        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        manager.remove(task2.getId());

        List<Task> history = manager.getHistory();
        assertEquals(2, history.size(), "История должна содержать две задачи после удаления");
        assertEquals(task1, history.get(0), "Первая задача должна быть task 1");
        assertEquals(task3, history.get(1), "Вторая задача должна быть task 3");
    }

    @Test
    public void addAndGetTasksTest() {
        Task task1 = new Task(TaskType.TASK,"1", TaskStatus.NEW, "Задача 1", new Duration(10),
                LocalDateTime.now());
        Task task2 = new Task(TaskType.TASK, "2", TaskStatus.NEW, "Задача 2", new Duration(10),
                LocalDateTime.now().plusMinutes(10));
        Task task3 = new Task(TaskType.TASK, "3", TaskStatus.NEW, "Задача 3", new Duration(10),
                LocalDateTime.now().plusMinutes(20));

        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        List<Task> tasks = manager.getHistory();
        assertEquals(3, manager.getSize(), "tasks size");
        assertEquals("Задача 1", tasks.get(0).getDescription(), "task1");
        assertEquals("Задача 2", tasks.get(1).getDescription());
        assertEquals("Задача 3", tasks.get(2).getDescription());
    }

    @Test
    public void removeTaskTest() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Task task1 = new Task(TaskType.TASK,"1", TaskStatus.NEW,"Задача 1", new Duration(10),
                LocalDateTime.now());
        Task task2 = new Task(TaskType.TASK,"2", TaskStatus.NEW, "Задача 2", new Duration(10),
                LocalDateTime.now().plusMinutes(10));
        Task task3 = new Task(TaskType.TASK,"3", TaskStatus.NEW, "Задача 3", new Duration(10),
                LocalDateTime.now().plusMinutes(20));

        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        int removeId = task2.getId();

        taskManager.deleteTask(removeId);
        manager.remove(removeId);

        List<Task> tasks = manager.getTasks();
        assertEquals(2, manager.getSize());
        assertEquals("Задача 1", tasks.get(0).getDescription());
        assertEquals("Задача 3", tasks.get(1).getDescription());
    }

    @Test
    public void notRetainOldIdsAfterRemovalTest() {
        boolean containsOldId = false;

        Task task1 = new Task(TaskType.TASK, "1", TaskStatus.NEW, "Задача 1", new Duration(10),
                LocalDateTime.now());
        Task task2 = new Task(TaskType.TASK, "2", TaskStatus.NEW, "Задача 2", new Duration(10),
                LocalDateTime.now().plusMinutes(10));
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
        Task task1 = new Task(TaskType.TASK, "1", TaskStatus.NEW, "Задача 1", new Duration(10),
                LocalDateTime.now());
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

        epic.removeSubtask(2);

        assertFalse(epic.getSubtaskIds().contains(2));
    }


}