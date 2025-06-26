package test;

import manager.InMemoryTaskManager;
import manager.TaskIntersectionException;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setup() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void shouldAddAndGetTask() throws TaskIntersectionException {
        Task task = new Task(1, "Task1", "Desc1", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 6, 25, 10, 0));
        manager.addTask(task);

        Task retrieved = manager.getTask(1);
        assertNotNull(retrieved);
        assertEquals(task.getId(), retrieved.getId());
        assertEquals(task.getStartTime(), retrieved.getStartTime());
        assertEquals(task.getDuration(), retrieved.getDuration());
    }

    @Test
    void shouldAddAndGetEpicWithSubtasks() throws TaskIntersectionException {
        Epic epic = new Epic(10, "Epic1", "DescEpic");
        manager.addEpic(epic);

        Subtask sub1 = new Subtask(11, "Sub1", "DescSub1", TaskStatus.NEW, 10,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 6, 26, 9, 0));
        Subtask sub2 = new Subtask(12, "Sub2", "DescSub2", TaskStatus.IN_PROGRESS, 10,
                Duration.ofMinutes(45), LocalDateTime.of(2025, 6, 26, 10, 0));

        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        Epic retrievedEpic = manager.getEpic(10);
        assertNotNull(retrievedEpic);

        assertTrue(retrievedEpic.getSubtaskIds().contains(11));
        assertTrue(retrievedEpic.getSubtaskIds().contains(12));

        assertEquals(Duration.ofMinutes(75), retrievedEpic.getDuration());
        assertEquals(LocalDateTime.of(2025, 6, 26, 9, 0), retrievedEpic.getStartTime());
        assertEquals(LocalDateTime.of(2025, 6, 26, 10, 45), retrievedEpic.getEndTime());
    }

    @Test
    void shouldUpdateTask() throws TaskIntersectionException {
        Task task = new Task(1, "Task1", "Desc1", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 6, 25, 10, 0));
        manager.addTask(task);

        Task updated = new Task(1, "Task1 Updated", "Desc1 Updated", TaskStatus.DONE,
                Duration.ofMinutes(90), LocalDateTime.of(2025, 6, 25, 11, 0));
        manager.updateTask(updated);

        Task retrieved = manager.getTask(1);
        assertEquals("Task1 Updated", retrieved.getName());
        assertEquals(TaskStatus.DONE, retrieved.getStatus());
        assertEquals(Duration.ofMinutes(90), retrieved.getDuration());
        assertEquals(LocalDateTime.of(2025, 6, 25, 11, 0), retrieved.getStartTime());
    }


    @Test
    void shouldThrowOnIntersectingTasks() throws TaskIntersectionException {
        Task task1 = new Task(1, "Task1", "Desc1", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 6, 25, 10, 0));
        manager.addTask(task1);

        Task task2 = new Task(2, "Task2", "Desc2", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 6, 25, 10, 30));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            manager.addTask(task2);
        });
        assertTrue(exception.getMessage().contains("пересекается"));
    }

    @Test
    void shouldReturnHistoryInOrder() throws TaskIntersectionException {
        Task task1 = new Task(1, "Task1", "Desc1", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.of(2025, 6, 25, 9, 0));
        Task task2 = new Task(2, "Task2", "Desc2", TaskStatus.NEW,
                Duration.ofMinutes(20), LocalDateTime.of(2025, 6, 25, 10, 0));

        manager.addTask(task1);
        manager.addTask(task2);

        manager.getTask(1);
        manager.getTask(2);
        manager.getTask(1);

        List<Task> history = manager.getHistory();

        assertEquals(2, history.size());
        assertEquals(2, history.get(0).getId());
        assertEquals(1, history.get(1).getId());
    }

    @Test
    void shouldReturnPrioritizedTasksSortedByStartTime() throws TaskIntersectionException {
        Task task1 = new Task(1, "Task1", "Desc1", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.of(2025, 6, 25, 11, 0));
        Task task2 = new Task(2, "Task2", "Desc2", TaskStatus.NEW,
                Duration.ofMinutes(20), LocalDateTime.of(2025, 6, 25, 10, 0));
        Task task3 = new Task(3, "Task3", "Desc3", TaskStatus.NEW,
                Duration.ofMinutes(15), LocalDateTime.of(2025, 6, 25, 12, 0));

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(3, prioritized.size());
        assertEquals(2, prioritized.get(0).getId()); // начало в 10:00
        assertEquals(1, prioritized.get(1).getId()); // начало в 11:00
        assertEquals(3, prioritized.get(2).getId()); // начало в 12:00
    }

    @Test
    void deletingEpicRemovesSubtasks() throws TaskIntersectionException {
        Epic epic = new Epic(100, "Epic100", "DescEpic");
        manager.addEpic(epic);

        Subtask subtask = new Subtask(101, "Sub101", "DescSub", TaskStatus.NEW, 100,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 6, 26, 9, 0));
        manager.addSubtask(subtask);

        assertNotNull(manager.getSubtask(101));

        manager.deleteEpic(100);

        assertNull(manager.getEpic(100));
        assertNull(manager.getSubtask(101));
    }
}