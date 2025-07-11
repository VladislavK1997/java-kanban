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
class HttpTaskManagerHistoryTest {
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
    void clearAll() throws IOException, InterruptedException {
        HttpRequest deleteAllTasks = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .DELETE()
                .build();
        client.send(deleteAllTasks, HttpResponse.BodyHandlers.ofString());

        HttpRequest deleteAllEpics = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic/"))
                .DELETE()
                .build();
        client.send(deleteAllEpics, HttpResponse.BodyHandlers.ofString());

        HttpRequest deleteAllSubtasks = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/subtask/"))
                .DELETE()
                .build();
        client.send(deleteAllSubtasks, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void shouldReturnHistoryAfterAccessingTask() throws IOException, InterruptedException {
        Task task = new Task(1, "Task", "Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                .build();
        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/?id=1"))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());

        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/history"))
                .GET()
                .build();
        HttpResponse<String> historyResponse = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, historyResponse.statusCode());
        assertTrue(historyResponse.body().contains("Task"));
    }
}