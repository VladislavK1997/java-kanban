package test;

import http.HttpTaskServer;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.*;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerHistoryTest {
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
    void shouldReturnHistoryAfterAccessingTask() throws IOException, InterruptedException {
        Task task = new Task(1, "Task", "Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        client.send(HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/tasks/task/"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        client.send(HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/tasks/task/?id=1"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        HttpResponse<String> historyResponse = client.send(HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/tasks/history"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertTrue(historyResponse.body().contains("Task"));
    }
}

