package test;

import com.google.gson.Gson;
import http.HttpTaskServer;
import model.Epic;
import model.Subtask;
import model.TaskStatus;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerSubtasksTest {
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
    void shouldAddAndGetSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic(1, "Epic", "Epic description");

        client.send(HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic)))
                .build(), HttpResponse.BodyHandlers.ofString());

        Subtask subtask = new Subtask(2, "Subtask", "Subtask description", TaskStatus.NEW,
                1, Duration.ofMinutes(15), LocalDateTime.now());

        HttpRequest subtaskRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/subtask/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask)))
                .build();
        HttpResponse<String> response = client.send(subtaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
    }
}


