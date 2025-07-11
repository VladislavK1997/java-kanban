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

class HttpTaskManagerPrioritizedTest {
    private static HttpTaskServer server;
    private static final Gson gson = HttpTaskServer.getGson();
    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void setup() throws IOException {
        server = new HttpTaskServer();
        server.start();
    }

    @AfterAll
    static void tearDown() {
        server.stop();
    }

    @Test
    void shouldReturnPrioritizedTasksInCorrectOrder() throws IOException, InterruptedException {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW,
                Duration.ofMinutes(20), LocalDateTime.of(2023, 1, 1, 10, 0));
        Task task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW,
                Duration.ofMinutes(20), LocalDateTime.of(2023, 1, 1, 9, 0));

        client.send(HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/tasks/task/"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1)))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        client.send(HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/tasks/task/"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task2)))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        HttpResponse<String> response = client.send(HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/tasks/prioritized"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertTrue(response.body().indexOf("Task 2") < response.body().indexOf("Task 1"));
    }
}

