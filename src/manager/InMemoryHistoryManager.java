package manager;

import model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final CustomLinkedList<Task> history = new CustomLinkedList<>();
    private final Map<Integer, Node<Task>> nodeMap = new HashMap<>();

    @Override
    public void add(Task task) {
        remove(task.getId());
        linkLast(task);
    }

    @Override
    public List<Task> getHistory() {
        return history.getTasks();
    }

    @Override
    public void remove(int id) {
        Node<Task> node = nodeMap.remove(id);
        if (node != null) {
            history.removeNode(node);
        }
    }

    private void linkLast(Task task) {
        Node<Task> newNode = history.linkLast(task);
        nodeMap.put(task.getId(), newNode);
    }

    private static class CustomLinkedList<T> {
        private Node<T> head;
        private Node<T> tail;

        void removeNode(Node<T> node) {
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

        Node<T> linkLast(T element) {
            Node<T> node = new Node<>(tail, element, null);
            if (tail != null) {
                tail.next = node;
            } else {
                head = node;
            }
            tail = node;
            return node;
        }

        List<T> getTasks() {
            List<T> list = new ArrayList<>();
            Node<T> current = head;
            while (current != null) {
                list.add(current.data);
                current = current.next;
            }
            return list;
        }
    }

    private static class Node<T> {
        T data;
        Node<T> prev;
        Node<T> next;

        Node(Node<T> prev, T data, Node<T> next) {
            this.prev = prev;
            this.data = data;
            this.next = next;
        }
    }
}
