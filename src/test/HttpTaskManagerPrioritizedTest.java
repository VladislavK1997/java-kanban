package test;

import com.google.gson.Gson;
import http.HttpTaskServer;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpTaskManagerPrioritizedTest {
    private HttpTaskServer server;
    private final Gson gson = HttpTaskServer.getGson();
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    void startServer() throws IOException {
        server = new HttpTaskServer();
        server.start();
    }

    @AfterAll
    void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearTasks() throws IOException, InterruptedException {
        HttpRequest deleteAll = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(deleteAll, HttpResponse.BodyHandlers.ofString());
        assertTrue(response.statusCode() == 200 || response.statusCode() == 204);
    }

    @Test
    void shouldReturnPrioritizedTasksInCorrectOrder() throws IOException, InterruptedException {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW,
                Duration.ofMinutes(20), LocalDateTime.of(2023, 1, 1, 10, 0));
        Task task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW,
                Duration.ofMinutes(20), LocalDateTime.of(2023, 1, 1, 9, 0));

        HttpRequest post1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1)))
                .build();
        HttpResponse<String> resp1 = client.send(post1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, resp1.statusCode());

        HttpRequest post2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task2)))
                .build();
        HttpResponse<String> resp2 = client.send(post2, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, resp2.statusCode());

        HttpRequest getPrioritized = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/prioritized"))
                .GET()
                .build();
        HttpResponse<String> prioritizedResp = client.send(getPrioritized, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, prioritizedResp.statusCode());

        String body = prioritizedResp.body();
        assertTrue(body.indexOf("Task 2") < body.indexOf("Task 1"));
    }
}