package test;

import com.google.gson.Gson;
import http.HttpTaskServer;
import model.Epic;
import model.Subtask;
import model.TaskStatus;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpTaskManagerSubtasksTest {
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
        client.send(HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/subtask/"))
                .DELETE()
                .build(), HttpResponse.BodyHandlers.ofString());

        client.send(HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic/"))
                .DELETE()
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void shouldAddAndGetSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic(1, "Epic", "Epic description");

        HttpRequest postEpic = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic)))
                .build();
        HttpResponse<String> epicResponse = client.send(postEpic, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, epicResponse.statusCode());

        Subtask subtask = new Subtask(2, "Subtask", "Subtask description", TaskStatus.NEW,
                1, Duration.ofMinutes(15), LocalDateTime.now());

        HttpRequest postSubtask = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/subtask/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask)))
                .build();
        HttpResponse<String> subtaskResponse = client.send(postSubtask, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, subtaskResponse.statusCode());

        HttpRequest getSubtaskRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/subtask/?id=2"))
                .GET()
                .build();
        HttpResponse<String> getSubtaskResponse = client.send(getSubtaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getSubtaskResponse.statusCode());

        Subtask retrieved = gson.fromJson(getSubtaskResponse.body(), Subtask.class);
        assertNotNull(retrieved);
        assertEquals(subtask.getName(), retrieved.getName());
        assertEquals(subtask.getEpicId(), retrieved.getEpicId());
    }
}