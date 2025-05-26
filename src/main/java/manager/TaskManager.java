package main.java.manager;

import main.java.model.Epic;
import main.java.model.Subtask;
import main.java.model.Task;
import java.util.List;

public interface TaskManager {

    // Методы для Task
    List<Task> getAllTasks();

    void deleteAllTasks();

    Task getTask(int id);

    Task addTask(Task task);

    void updateTask(Task task);

    void deleteTask(int id);

    // Методы для Epic
    List<Epic> getAllEpics();

    void deleteAllEpics();

    Epic getEpic(int id);

    Epic addEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteEpic(int id);

    List<Subtask> getEpicSubtasks(int epicId);

    // Методы для Subtask
    List<Subtask> getAllSubtasks();

    void deleteAllSubtasks();

    Subtask getSubtask(int id);

    Subtask addSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtask(int id);

    // История просмотров
    List<Task> getHistory();
}