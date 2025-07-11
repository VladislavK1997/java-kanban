package manager;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

public interface TaskManager {
    Task addTask(Task task) throws TaskIntersectionException;

    Epic addEpic(Epic epic);

    Subtask addSubtask(Subtask subtask) throws TaskIntersectionException;

    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    void updateTask(Task task) throws TaskIntersectionException;

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask) throws TaskIntersectionException;

    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubtask(int id);


    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();

    List<Subtask> getEpicSubtasks(int epicId);
}