package manager;

import model.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final NavigableSet<Task> prioritized = new TreeSet<>(
            Comparator.<Task, LocalDateTime>comparing(t -> Optional.ofNullable(t.getStartTime()).orElse(LocalDateTime.MAX))
                    .thenComparingInt(Task::getId)
    );
    protected int nextId = 1;
    private final HistoryManager history = new InMemoryHistoryManager();

    private boolean intersects(Task task) {
        return prioritized.stream()
                .anyMatch(t -> t.getId() != task.getId() &&
                        t.getStartTime() != null && t.getDuration() != null &&
                        task.getStartTime() != null && task.getDuration() != null &&
                        !(t.getEndTime().isEqual(task.getStartTime()) || t.getEndTime().isBefore(task.getStartTime()) ||
                                t.getStartTime().isEqual(task.getEndTime()) || t.getStartTime().isAfter(task.getEndTime()))
                );
    }

    @Override
    public Task addTask(Task task) {
        if (task.getId() == 0) task.setId(nextId++);
        if (intersects(task)) throw new IllegalArgumentException("пересекается");
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) prioritized.add(task);
        return task;
    }

    @Override
    public Epic addEpic(Epic epic) {
        if (epic.getId() == 0) epic.setId(nextId++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) throw new IllegalArgumentException();
        if (subtask.getId() == 0) subtask.setId(nextId++);
        if (intersects(subtask)) throw new IllegalArgumentException("пересекается");
        subtasks.put(subtask.getId(), subtask);
        epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
        updateEpicTime(subtask.getEpicId());
        if (subtask.getStartTime() != null) prioritized.add(subtask);
        return subtask;
    }

    private void updateEpicTime(int epicId) {
        Epic e = epics.get(epicId);
        e.updateTimeAndDuration(getEpicSubtasks(epicId));
    }

    @Override
    public Task getTask(int id) {
        Task t = tasks.get(id);
        if (t != null) history.add(t);
        return t;
    }

    @Override
    public Epic getEpic(int id) {
        Epic e = epics.get(id);
        if (e != null) history.add(e);
        return e;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask s = subtasks.get(id);
        if (s != null) history.add(s);
        return s;
    }

    @Override
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) throw new NoSuchElementException();
        if (intersects(task)) throw new IllegalArgumentException("пересекается");
        Task old = tasks.get(task.getId());
        if (old.getStartTime() != null) prioritized.remove(old);
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) prioritized.add(task);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) throw new NoSuchElementException();
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) throw new NoSuchElementException();
        if (intersects(subtask)) throw new IllegalArgumentException("пересекается");
        Subtask old = subtasks.get(subtask.getId());
        if (old.getStartTime() != null) prioritized.remove(old);
        subtasks.put(subtask.getId(), subtask);
        updateEpicTime(subtask.getEpicId());
        if (subtask.getStartTime() != null) prioritized.add(subtask);
    }

    @Override
    public void deleteTask(int id) {
        Task t = tasks.remove(id);
        if (t != null && t.getStartTime() != null) prioritized.remove(t);
        history.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) return;
        for (int subId : new ArrayList<>(epic.getSubtaskIds())) deleteSubtask(subId);
        history.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask s = subtasks.remove(id);
        if (s == null) return;
        if (s.getStartTime() != null) prioritized.remove(s);
        Epic e = epics.get(s.getEpicId());
        if (e != null) {
            e.removeSubtaskId(id);
            updateEpicTime(e.getId());
        }
        history.remove(id);
    }

    @Override
    public List<Task> getAllTasks() { return new ArrayList<>(tasks.values()); }
    @Override
    public List<Epic> getAllEpics() { return new ArrayList<>(epics.values()); }
    @Override
    public List<Subtask> getAllSubtasks() { return new ArrayList<>(subtasks.values()); }

    @Override
    public void deleteAllTasks() {
        tasks.values().forEach(t -> history.remove(t.getId()));
        prioritized.removeAll(tasks.values());
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.values().forEach(e -> history.remove(e.getId()));
        epics.clear();
        subtasks.clear();
        prioritized.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.values().forEach(s -> history.remove(s.getId()));
        prioritized.removeAll(subtasks.values());
        epics.values().forEach(e -> {
            e.getSubtaskIds().clear();
            updateEpicTime(e.getId());
        });
        subtasks.clear();
    }

    @Override
    public List<Task> getHistory() { return history.getHistory(); }

    @Override
    public List<Task> getPrioritizedTasks() { return new ArrayList<>(prioritized); }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        return subtasks.values().stream()
                .filter(s -> s.getEpicId() == epicId)
                .collect(Collectors.toList());
    }
}

