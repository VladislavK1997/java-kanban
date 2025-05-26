
import main.java.manager.InMemoryTaskManager;
import main.java.manager.TaskManager;
import main.java.model.Epic;
import main.java.model.Subtask;
import main.java.model.Task;
import main.java.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void addTaskShouldAddTask() {
        Task task = new Task(1, "Task", "Description", TaskStatus.NEW);
        taskManager.addTask(task);

        Task savedTask = taskManager.getTask(1);
        assertNotNull(savedTask);
        assertEquals(task, savedTask);
    }

    @Test
    void addEpicShouldAddEpic() {
        Epic epic = new Epic(1, "Epic", "Description");
        taskManager.addEpic(epic);

        Epic savedEpic = taskManager.getEpic(1);
        assertNotNull(savedEpic);
        assertEquals(epic, savedEpic);
    }

    @Test
    void addSubtaskShouldAddSubtask() {
        Epic epic = new Epic(1, "Epic", "Description");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask(2, "Subtask", "Description", TaskStatus.NEW, 1);
        taskManager.addSubtask(subtask);

        Subtask savedSubtask = taskManager.getSubtask(2);
        assertNotNull(savedSubtask);
        assertEquals(subtask, savedSubtask);
    }

    @Test
    void deleteEpicShouldDeleteAllSubtasks() {
        Epic epic = new Epic(1, "Epic", "Description");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask(2, "Subtask 1", "Description", TaskStatus.NEW, 1);
        Subtask subtask2 = new Subtask(3, "Subtask 2", "Description", TaskStatus.NEW, 1);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.deleteEpic(1);

        assertNull(taskManager.getEpic(1));
        assertNull(taskManager.getSubtask(2));
        assertNull(taskManager.getSubtask(3));
    }

    @Test
    void getHistoryShouldReturnViewHistory() {
        Task task = new Task(1, "Task", "Description", TaskStatus.NEW);
        taskManager.addTask(task);

        taskManager.getTask(1);

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.getFirst());
    }

    @Test
    void historyShouldNotContainDuplicates() {
        Task task = new Task(1, "Task", "Description", TaskStatus.NEW);
        taskManager.addTask(task);

        taskManager.getTask(1);
        taskManager.getTask(1);
        taskManager.getTask(1);

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size());
    }

    @Test
    void deleteTaskShouldRemoveItFromHistory() {
        Task task = new Task(1, "Task", "Description", TaskStatus.NEW);
        taskManager.addTask(task);

        taskManager.getTask(1);
        taskManager.deleteTask(1);

        List<Task> history = taskManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void updateTaskShouldNotAffectHistory() {
        Task task = new Task(1, "Task", "Description", TaskStatus.NEW);
        taskManager.addTask(task);

        taskManager.getTask(1); // Добавляем в историю
        task.setName("Updated name");
        taskManager.updateTask(task);

        Task fromHistory = taskManager.getHistory().get(0);
        assertEquals("Task", fromHistory.getName());
        assertEquals("Updated name", taskManager.getTask(1).getName());
    }

    @Test
    void deletingSubtaskShouldRemoveItFromEpic() {
        Epic epic = new Epic(1, "Epic", "Description");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask(2, "Subtask", "Description", TaskStatus.NEW, 1);
        taskManager.addSubtask(subtask);

        taskManager.deleteSubtask(2);

        assertFalse(taskManager.getEpic(1).getSubtaskIds().contains(2));
    }
}