import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = new InMemoryTaskManager();

        System.out.println("Создаем задачи...");
        Task task1 = new Task(1, "Помыть посуду", "Помыть посуду вечером", TaskStatus.NEW);
        Task task2 = new Task(2, "Сделать уроки", "Выполнить задания по математике", TaskStatus.IN_PROGRESS);

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        System.out.println("\nСоздаем эпики с подзадачами...");
        Epic epic1 = new Epic(3, "Переезд", "Организовать переезд в другой город");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(4, "Собрать коробки", "Купить и собрать коробки для переезда",
                TaskStatus.NEW, 3);
        Subtask subtask2 = new Subtask(5, "Упаковать вещи", "Упаковать одежду и посуду",
                TaskStatus.IN_PROGRESS, 3);
        Subtask subtask3 = new Subtask(6, "Нанять грузчиков", "Найти грузчиков на день переезда",
                TaskStatus.DONE, 3);

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);

        Epic epic2 = new Epic(7, "Пустой эпик", "Эпик без подзадач");
        taskManager.addEpic(epic2);

        System.out.println("\nЗапрашиваем задачи несколько раз в разном порядке...");
        taskManager.getTask(1);
        printHistory(taskManager);

        taskManager.getEpic(3);
        printHistory(taskManager);

        taskManager.getSubtask(4);
        printHistory(taskManager);

        taskManager.getTask(1);
        printHistory(taskManager);

        taskManager.getSubtask(5);
        printHistory(taskManager);

        taskManager.getEpic(3);
        printHistory(taskManager);

        System.out.println("\nУдаляем задачу, которая есть в истории...");
        taskManager.deleteTask(1);
        printHistory(taskManager);

        System.out.println("\nУдаляем эпик с тремя подзадачами...");
        taskManager.deleteEpic(3);
        printHistory(taskManager);
    }

    private static void printHistory(TaskManager manager) {
        System.out.println("История просмотров:");
        List<Task> history = manager.getHistory();
        if (history.isEmpty()) {
            System.out.println("История пуста");
        } else {
            for (Task task : history) {
                System.out.println(task);
            }
        }
        System.out.println();
    }
}