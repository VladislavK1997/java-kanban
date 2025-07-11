package test;

import com.google.gson.Gson;
import http.HttpTaskServer;
import model.Task;
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

class HttpTaskManagerTasksTest {
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
    void shouldAddAndGetTask() throws IOException, InterruptedException {
        Task task = new Task(1, "Test Task", "Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/?id=1"))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());
        Task retrieved = gson.fromJson(getResponse.body(), Task.class);
        assertEquals(task.getName(), retrieved.getName());
    }
}

