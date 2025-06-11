package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import java.util.List;

public interface TaskManager {

    List<Task> getAllTasks();
    void deleteAllTasks();
    Task getTask(int id);
    Task addTask(Task task);
    void updateTask(Task task);
    void deleteTask(int id);


    List<Epic> getAllEpics();
    void deleteAllEpics();
    Epic getEpic(int id);
    Epic addEpic(Epic epic);
    void updateEpic(Epic epic);
    void deleteEpic(int id);
    List<Subtask> getEpicSubtasks(int epicId);


    List<Subtask> getAllSubtasks();
    void deleteAllSubtasks();
    Subtask getSubtask(int id);
    Subtask addSubtask(Subtask subtask);
    void updateSubtask(Subtask subtask);
    void deleteSubtask(int id);


    List<Task> getHistory();
}