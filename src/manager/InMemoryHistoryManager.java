package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node<Task>> nodeMap = new HashMap<>();
    private Node<Task> head;
    private Node<Task> tail;

    @Override
    public void add(Task task) {
        if (task == null) return;

        remove(task.getId());

        Task taskCopy = copy(task);
        Node<Task> newNode = linkLast(taskCopy);
        nodeMap.put(task.getId(), newNode);
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node<Task> current = head;
        while (current != null) {
            history.add(current.data);
            current = current.next;
        }
        return history;
    }

    @Override
    public void remove(int id) {
        Node<Task> node = nodeMap.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    private Node<Task> linkLast(Task task) {
        Node<Task> newNode = new Node<>(task, tail, null);
        if (tail != null) {
            tail.next = newNode;
        } else {
            head = newNode;
        }
        tail = newNode;
        return newNode;
    }

    private void removeNode(Node<Task> node) {
        Node<Task> prev = node.prev;
        Node<Task> next = node.next;

        if (prev != null) {
            prev.next = next;
        } else {
            head = next;
        }

        if (next != null) {
            next.prev = prev;
        } else {
            tail = prev;
        }
    }

    private Task copy(Task task) {
        if (task instanceof Epic epic) {
            Epic copy = new Epic(epic.getId(), epic.getName(), epic.getDescription());
            copy.setStatus(epic.getStatus());
            epic.getSubtaskIds().forEach(copy::addSubtaskId);
            return copy;
        }
        if (task instanceof Subtask subtask) {
            Subtask copy = new Subtask(subtask.getId(), subtask.getName(), subtask.getDescription(),
                    subtask.getStatus(), subtask.getEpicId());
            return copy;
        }

        Task copy = new Task(task.getId(), task.getName(), task.getDescription(), task.getStatus());
        return copy;
    }

    private static class Node<T> {
        public T data;
        public Node<T> prev;
        public Node<T> next;

        public Node(T data, Node<T> prev, Node<T> next) {
            this.data = data;
            this.prev = prev;
            this.next = next;
        }
    }
}