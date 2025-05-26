package main.java.manager;

import main.java.model.Epic;
import main.java.model.Subtask;
import main.java.model.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private static class Node {
        Task task;
        Node next;
        Node prev;

        Node(Task task, Node prev, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }

    private final Map<Integer, Node> historyMap = new HashMap<>();
    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (task == null) return;

        remove(task.getId());
        linkLast(deepCopy(task));
    }

    @Override
    public void remove(int id) {
        Node node = historyMap.get(id);
        if (node != null) {
            removeNode(node);
            historyMap.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node current = head;
        while (current != null) {
            history.add(current.task);
            current = current.next;
        }
        return history;
    }

    private void linkLast(Task task) {
        final Node newNode = new Node(task, tail, null);
        if (tail == null) {
            head = newNode;
        } else {
            tail.next = newNode;
        }
        tail = newNode;
        historyMap.put(task.getId(), newNode);
    }

    private void removeNode(Node node) {
        if (node == null) return;

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }

    private Task deepCopy(Task original) {
        if (original == null) return null;

        if (original instanceof Epic) {
            Epic epic = (Epic) original;
            Epic copy = new Epic(epic.getId(), epic.getName(), epic.getDescription());
            copy.setStatus(epic.getStatus());
            copy.getSubtaskIds().addAll(epic.getSubtaskIds());
            return copy;
        } else if (original instanceof Subtask) {
            Subtask subtask = (Subtask) original;
            return new Subtask(
                    subtask.getId(),
                    subtask.getName(),
                    subtask.getDescription(),
                    subtask.getStatus(),
                    subtask.getEpicId()
            );
        } else {
            return new Task(
                    original.getId(),
                    original.getName(),
                    original.getDescription(),
                    original.getStatus()
            );
        }
    }
}