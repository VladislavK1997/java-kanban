package test;

import com.google.gson.Gson;
import http.HttpTaskServer;
import model.TaskStatus;
import model.Task;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static http.HttpTaskServer.getGson;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HttpTaskManagerTasksTest {
    private static HttpTaskServer server;
    private static HttpClient client;
    private static final Gson gson = getGson();

    @BeforeAll
    static void setup() throws IOException {
        server = new HttpTaskServer();
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void teardown() {
        server.stop();
    }

    @Test
    @Order(1)
    void shouldAddTask() throws IOException, InterruptedException {
        Task task = new Task(0, "Test Task", "Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        HttpResponse<String> response = postTask(task);
        assertEquals(201, response.statusCode());

        Task created = gson.fromJson(response.body(), Task.class);
        assertEquals(task.getName(), created.getName());
    }

    @Test
    @Order(2)
    void shouldGetTaskById() throws IOException, InterruptedException {
        Task task = new Task(0, "Another Task", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(15), LocalDateTime.now());

        Task created = gson.fromJson(postTask(task).body(), Task.class);
        HttpResponse<String> response = getTaskById(created.getId());

        assertEquals(200, response.statusCode());
        Task fetched = gson.fromJson(response.body(), Task.class);
        assertEquals(created, fetched);
    }

    @Test
    @Order(3)
    void shouldDeleteAllTasks() throws IOException, InterruptedException {
        HttpResponse<String> response = deleteAllTasks();
        assertEquals(200, response.statusCode());
    }

    private HttpResponse<String> postTask(Task task) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getTaskById(int id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/?id=" + id))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> deleteAllTasks() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}