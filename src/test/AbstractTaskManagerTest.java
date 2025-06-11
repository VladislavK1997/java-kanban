package test;

import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.*;
import model.TaskStatus;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractTaskManagerTest {

    protected TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = createTaskManager();
        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();
    }

    protected abstract TaskManager createTaskManager();

    @Test
    void addAndGetTask() {
        Task task = new Task(0, "Task1", "Desc", TaskStatus.NEW);
        Task saved = manager.addTask(task);
        assertNotNull(saved);
        Task retrieved = manager.getTask(saved.getId());
        assertEquals(saved.getId(), retrieved.getId());
        assertEquals("Task1", retrieved.getName());
    }

    @Test
    void addAndGetEpic() {
        Epic epic = new Epic(0, "Epic1", "EpicDesc");
        Epic saved = manager.addEpic(epic);
        assertNotNull(saved);
        Epic retrieved = manager.getEpic(saved.getId());
        assertEquals(saved.getId(), retrieved.getId());
        assertEquals("Epic1", retrieved.getName());
    }

    @Test
    void addAndGetSubtask() {
        Epic epic = manager.addEpic(new Epic(0, "Epic1", "Desc"));
        Subtask subtask = new Subtask(0, "Subtask1", "Desc", TaskStatus.NEW, epic.getId());
        Subtask saved = manager.addSubtask(subtask);
        assertNotNull(saved);
        Subtask retrieved = manager.getSubtask(saved.getId());
        assertEquals(saved.getId(), retrieved.getId());
        assertEquals("Subtask1", retrieved.getName());
        assertEquals(epic.getId(), retrieved.getEpicId());
    }

    @Test
    void updateTask() {
        Task task = manager.addTask(new Task(0, "Task1", "Desc", TaskStatus.NEW));
        task.setName("Updated");
        manager.updateTask(task);
        Task updated = manager.getTask(task.getId());
        assertEquals("Updated", updated.getName());
    }

    @Test
    void updateEpic() {
        Epic epic = manager.addEpic(new Epic(0, "Epic1", "Desc"));
        epic.setName("UpdatedEpic");
        manager.updateEpic(epic);
        Epic updated = manager.getEpic(epic.getId());
        assertEquals("UpdatedEpic", updated.getName());
    }

    @Test
    void updateSubtask() {
        Epic epic = manager.addEpic(new Epic(0, "Epic1", "Desc"));
        Subtask subtask = manager.addSubtask(new Subtask(0, "Subtask1", "Desc", TaskStatus.NEW, epic.getId()));
        subtask.setName("UpdatedSubtask");
        manager.updateSubtask(subtask);
        Subtask updated = manager.getSubtask(subtask.getId());
        assertEquals("UpdatedSubtask", updated.getName());
    }

    @Test
    void deleteTask() {
        Task task = manager.addTask(new Task(0, "Task1", "Desc", TaskStatus.NEW));
        manager.deleteTask(task.getId());
        assertNull(manager.getTask(task.getId()));
    }

    @Test
    void deleteEpicAndSubtasks() {
        Epic epic = manager.addEpic(new Epic(0, "Epic1", "Desc"));
        Subtask subtask = manager.addSubtask(new Subtask(0, "Subtask1", "Desc", TaskStatus.NEW, epic.getId()));
        manager.deleteEpic(epic.getId());
        assertNull(manager.getEpic(epic.getId()));
        assertNull(manager.getSubtask(subtask.getId()));
    }

    @Test
    void getAllTasks() {
        manager.addTask(new Task(0, "Task1", "Desc", TaskStatus.NEW));
        manager.addTask(new Task(0, "Task2", "Desc", TaskStatus.NEW));
        List<Task> tasks = manager.getAllTasks();
        assertEquals(2, tasks.size());
    }

    @Test
    void getAllEpics() {
        manager.addEpic(new Epic(0, "Epic1", "Desc"));
        manager.addEpic(new Epic(0, "Epic2", "Desc"));
        List<Epic> epics = manager.getAllEpics();
        assertEquals(2, epics.size());
    }

    @Test
    void getAllSubtasks() {
        Epic epic = manager.addEpic(new Epic(0, "Epic1", "Desc"));
        manager.addSubtask(new Subtask(0, "Subtask1", "Desc", TaskStatus.NEW, epic.getId()));
        manager.addSubtask(new Subtask(0, "Subtask2", "Desc", TaskStatus.NEW, epic.getId()));
        List<Subtask> subtasks = manager.getAllSubtasks();
        assertEquals(2, subtasks.size());
    }

    @Test
    void getSubtasksOfEpic() {
        Epic epic = manager.addEpic(new Epic(0, "Epic1", "Desc"));
        Subtask s1 = manager.addSubtask(new Subtask(0, "Subtask1", "Desc", TaskStatus.NEW, epic.getId()));
        Subtask s2 = manager.addSubtask(new Subtask(0, "Subtask2", "Desc", TaskStatus.NEW, epic.getId()));
        List<Subtask> subtasks = manager.getEpicSubtasks(epic.getId());
        assertEquals(2, subtasks.size());
        assertTrue(subtasks.stream().anyMatch(s -> s.getId() == s1.getId()));
        assertTrue(subtasks.stream().anyMatch(s -> s.getId() == s2.getId()));
    }

    @Test
    void historyTracksAccessedTasks() {
        Task task = manager.addTask(new Task(0, "Task1", "Desc", TaskStatus.NEW));
        Epic epic = manager.addEpic(new Epic(0, "Epic1", "Desc"));
        Subtask subtask = manager.addSubtask(new Subtask(0, "Subtask1", "Desc", TaskStatus.NEW, epic.getId()));

        manager.getTask(task.getId());
        manager.getEpic(epic.getId());
        manager.getSubtask(subtask.getId());

        List<Task> history = manager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task.getId(), history.get(0).getId());
        assertEquals(epic.getId(), history.get(1).getId());
        assertEquals(subtask.getId(), history.get(2).getId());
    }

    @Test
    void clearAllTasks() {
        manager.addTask(new Task(0, "Task1", "Desc", TaskStatus.NEW));
        manager.deleteAllTasks();
        List<Task> tasks = manager.getAllTasks();
        assertTrue(tasks.isEmpty());
    }

    @Test
    void clearAllEpicsAndSubtasks() {
        Epic epic = manager.addEpic(new Epic(0, "Epic1", "Desc"));
        manager.addSubtask(new Subtask(0, "Subtask1", "Desc", TaskStatus.NEW, epic.getId()));

        manager.deleteAllEpics();
        manager.deleteAllSubtasks();

        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }
}
