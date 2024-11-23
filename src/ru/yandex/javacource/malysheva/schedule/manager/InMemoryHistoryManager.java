package ru.yandex.javacource.malysheva.schedule.manager;

import ru.yandex.javacource.malysheva.schedule.tasks.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private Node head;
    private Node tail;
    private HashMap<Integer, Node> nodeMap;
    private int tasksCount;

    public InMemoryHistoryManager() {
        this.nodeMap = new HashMap<>();
        this.tasksCount = 0;
    }

    public void linkLast(Task task) {
        if (task == null) {
            return;
        }

        Node tailNode = new Node(task);
        if (tail == null) {
            head = tail = tailNode;
        } else {
            tail.next = tailNode;
            tail.prev = tail;
            tail = tailNode;
        }

        nodeMap.put(task.getId(), tailNode);
        tasksCount++;
    }

    public List<Task> getTasks() {
        List<Task> taskList = new ArrayList<>();
        Node check = head;
        while (check != null) {
            taskList.add(check.task);
            check = check.next;
        }
        return taskList;
    }

    public void removeNode (Node node) {
        nodeMap.remove(node);
    }

    @Override
    public void addTask(Task task) {
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        Node nodeToRemove = nodeMap.get(id);
        if (nodeToRemove == null) {
            return;
        }

        if (nodeToRemove.prev != null) {
            nodeToRemove.prev.next = nodeToRemove.next;
        } else {
            head = nodeToRemove.next;
        }

        if (nodeToRemove.next != null) {
            nodeToRemove.next.prev = nodeToRemove.prev;
        } else {
            tail = nodeToRemove.prev;
        }

        removeNode (nodeToRemove);
        tasksCount--;
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }
}
