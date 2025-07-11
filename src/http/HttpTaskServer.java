package http;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import manager.FileBackedTaskManager;
import manager.TaskManager;
import manager.TaskIntersectionException;
import model.Epic;
import model.Subtask;
import model.Task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;
    private final Gson gson;

    public HttpTaskServer() throws IOException {
        this(new FileBackedTaskManager(new File("tasks.csv")));
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.gson = getGson();
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/tasks/task", this::handleTasks);
        server.createContext("/tasks/subtask", this::handleSubtasks);
        server.createContext("/tasks/epic", this::handleEpics);
        server.createContext("/tasks/history", this::handleHistory);
        server.createContext("/tasks/prioritized", this::handlePrioritized);

    }

    public void start() {
        server.start();
        System.out.println("HTTP server started on port " + PORT);
    }

    private void handleTasks(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String query = exchange.getRequestURI().getQuery();

        switch (method) {
            case "GET" -> {
                if (query == null) {
                    List<Task> tasks = taskManager.getAllTasks();
                    sendResponse(exchange, 200, gson.toJson(tasks));
                } else {
                    Integer id = parseIdFromQuery(query);
                    if (id == null) {
                        sendResponse(exchange, 400, "Invalid id");
                        return;
                    }
                    Task task = taskManager.getTask(id);
                    if (task == null) {
                        sendResponse(exchange, 404, "Task not found");
                    } else {
                        sendResponse(exchange, 200, gson.toJson(task));
                    }
                }
            }
            case "POST" -> {
                String body = readRequestBody(exchange);
                Task task = gson.fromJson(body, Task.class);
                if (task == null) {
                    sendResponse(exchange, 400, "Invalid task data");
                    return;
                }
                try {
                    if (task.getId() == 0) {
                        Task created = taskManager.addTask(task);
                        sendResponse(exchange, 201, gson.toJson(created));
                    } else {
                        if (taskManager.getTask(task.getId()) == null) {
                            sendResponse(exchange, 404, "Task not found");
                            return;
                        }
                        taskManager.updateTask(task);
                        sendResponse(exchange, 200, gson.toJson(task));
                    }
                } catch (TaskIntersectionException e) {
                    sendResponse(exchange, 409, "Task time intersection error: " + e.getMessage());
                }
            }
            case "DELETE" -> {
                if (query == null) {
                    taskManager.deleteAllTasks();
                    sendResponse(exchange, 200, "All tasks deleted");
                } else {
                    Integer id = parseIdFromQuery(query);
                    if (id == null) {
                        sendResponse(exchange, 400, "Invalid id");
                        return;
                    }
                    taskManager.deleteTask(id);
                    sendResponse(exchange, 200, "Task deleted");
                }
            }
            default -> sendResponse(exchange, 405, "Method not allowed");
        }
    }

    private void handleSubtasks(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String query = exchange.getRequestURI().getQuery();

        switch (method) {
            case "GET" -> {
                if (query == null) {
                    List<Subtask> subtasks = taskManager.getAllSubtasks();
                    sendResponse(exchange, 200, gson.toJson(subtasks));
                } else {
                    Integer id = parseIdFromQuery(query);
                    if (id == null) {
                        sendResponse(exchange, 400, "Invalid id");
                        return;
                    }
                    Subtask subtask = taskManager.getSubtask(id);
                    if (subtask == null) {
                        sendResponse(exchange, 404, "Subtask not found");
                    } else {
                        sendResponse(exchange, 200, gson.toJson(subtask));
                    }
                }
            }
            case "POST" -> {
                String body = readRequestBody(exchange);
                Subtask subtask = gson.fromJson(body, Subtask.class);
                if (subtask == null) {
                    sendResponse(exchange, 400, "Invalid subtask data");
                    return;
                }
                try {
                    if (subtask.getId() == 0) {
                        Subtask created = taskManager.addSubtask(subtask);
                        sendResponse(exchange, 201, gson.toJson(created));
                    } else {
                        if (taskManager.getSubtask(subtask.getId()) == null) {
                            sendResponse(exchange, 404, "Subtask not found");
                            return;
                        }
                        taskManager.updateSubtask(subtask);
                        sendResponse(exchange, 200, gson.toJson(subtask));
                    }
                } catch (TaskIntersectionException e) {
                    sendResponse(exchange, 409, "Subtask time intersection error: " + e.getMessage());
                }
            }
            case "DELETE" -> {
                if (query == null) {
                    taskManager.deleteAllSubtasks();
                    sendResponse(exchange, 200, "All subtasks deleted");
                } else {
                    Integer id = parseIdFromQuery(query);
                    if (id == null) {
                        sendResponse(exchange, 400, "Invalid id");
                        return;
                    }
                    taskManager.deleteSubtask(id);
                    sendResponse(exchange, 200, "Subtask deleted");
                }
            }
            default -> sendResponse(exchange, 405, "Method not allowed");
        }
    }

    private void handleEpics(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String query = exchange.getRequestURI().getQuery();

        switch (method) {
            case "GET" -> {
                if (query == null) {
                    List<Epic> epics = taskManager.getAllEpics();
                    sendResponse(exchange, 200, gson.toJson(epics));
                } else {
                    Integer id = parseIdFromQuery(query);
                    if (id == null) {
                        sendResponse(exchange, 400, "Invalid id");
                        return;
                    }
                    Epic epic = taskManager.getEpic(id);
                    if (epic == null) {
                        sendResponse(exchange, 404, "Epic not found");
                    } else {
                        sendResponse(exchange, 200, gson.toJson(epic));
                    }
                }
            }
            case "POST" -> {
                String body = readRequestBody(exchange);
                Epic epic = gson.fromJson(body, Epic.class);
                if (epic == null) {
                    sendResponse(exchange, 400, "Invalid epic data");
                    return;
                }
                try {
                    if (epic.getId() == 0) {
                        Epic created = taskManager.addEpic(epic);
                        sendResponse(exchange, 201, gson.toJson(created));
                    } else {
                        if (taskManager.getEpic(epic.getId()) == null) {
                            sendResponse(exchange, 404, "Epic not found");
                            return;
                        }
                        taskManager.updateEpic(epic);
                        sendResponse(exchange, 200, gson.toJson(epic));
                    }
                } catch (TaskIntersectionException e) {
                    sendResponse(exchange, 409, "Epic time intersection error: " + e.getMessage());
                }
            }
            case "DELETE" -> {
                if (query == null) {
                    taskManager.deleteAllEpics();
                    sendResponse(exchange, 200, "All epics deleted");
                } else {
                    Integer id = parseIdFromQuery(query);
                    if (id == null) {
                        sendResponse(exchange, 400, "Invalid id");
                        return;
                    }
                    taskManager.deleteEpic(id);
                    sendResponse(exchange, 200, "Epic deleted");
                }
            }
            default -> sendResponse(exchange, 405, "Method not allowed");
        }
    }

    private void handleHistory(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            sendResponse(exchange, 405, "Method not allowed");
            return;
        }
        sendResponse(exchange, 200, gson.toJson(taskManager.getHistory()));
    }

    private void handlePrioritized(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            sendResponse(exchange, 405, "Method not allowed");
            return;
        }
        sendResponse(exchange, 200, gson.toJson(taskManager.getPrioritizedTasks()));
    }

    private static Integer parseIdFromQuery(String query) {
        if (query == null) return null;
        String[] parts = query.split("=");
        if (parts.length != 2) return null;
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }
    public void stop() {
        server.stop(0);
        System.out.println("HTTP server stopped");
    }
}




