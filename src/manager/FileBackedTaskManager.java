package manager;

import model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private static final String HEADER = "id,type,name,status,description,epic";

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(HEADER);
            writer.newLine();
            for (Task task : getAllTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to save tasks to file", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            List<String> lines = Files.readAllLines(Path.of(file.getPath()));
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                Task task = fromString(line);
                switch (getTaskType(task)) {
                    case TASK -> manager.tasks.put(task.getId(), task);
                    case EPIC -> manager.epics.put(task.getId(), (Epic) task);
                    case SUBTASK -> {
                        Subtask subtask = (Subtask) task;
                        manager.subtasks.put(subtask.getId(), subtask);
                        Epic epic = manager.epics.get(subtask.getEpicId());
                        if (epic != null) epic.addSubtaskId(subtask.getId());
                    }
                }
                manager.nextId = Math.max(manager.nextId, task.getId() + 1);
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to load from file", e);
        }
        return manager;
    }

    private static TaskType getTaskType(Task task) {
        if (task instanceof Epic) return TaskType.EPIC;
        if (task instanceof Subtask) return TaskType.SUBTASK;
        return TaskType.TASK;
    }

    private static String toString(Task task) {
        String type = getTaskType(task).name();
        String epicId = (task instanceof Subtask) ? String.valueOf(((Subtask) task).getEpicId()) : "";
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(), type, task.getName(), task.getStatus(), task.getDescription(), epicId);
    }

    private static Task fromString(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];

        return switch (type) {
            case TASK -> new Task(id, name, description, status);
            case EPIC -> new Epic(id, name, description);
            case SUBTASK -> new Subtask(id, name, description, status, Integer.parseInt(fields[5]));
        };
    }

    @Override
    public Task addTask(Task task) {
        Task t = super.addTask(task);
        save();
        return t;
    }

    @Override
    public Epic addEpic(Epic epic) {
        Epic e = super.addEpic(epic);
        save();
        return e;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        Subtask s = super.addSubtask(subtask);
        save();
        return s;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    public static void main(String[] args) {
        File file = new File("tasks.csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        manager.addTask(new Task(0, "Task1", "Do something", TaskStatus.NEW));
        Epic epic = manager.addEpic(new Epic(0, "Epic1", "Epic desc"));
        manager.addSubtask(new Subtask(0, "Sub1", "Sub desc", TaskStatus.NEW, epic.getId()));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        System.out.println("Loaded tasks: " + loaded.getAllTasks());
        System.out.println("Loaded epics: " + loaded.getAllEpics());
        System.out.println("Loaded subtasks: " + loaded.getAllSubtasks());
    }
}
