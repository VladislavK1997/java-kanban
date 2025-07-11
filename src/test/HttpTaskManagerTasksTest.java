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
class HttpTaskManagerTasksTest {
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
        HttpRequest deleteAllRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteAllRequest, HttpResponse.BodyHandlers.ofString());
        assertTrue(deleteResponse.statusCode() == 200 || deleteResponse.statusCode() == 204);
    }

    @Test
    void shouldAddAndGetTask() throws IOException, InterruptedException {
        Task task = new Task(1, "Test Task", "Description", TaskStatus.NEW,
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

        Task retrieved = gson.fromJson(getResponse.body(), Task.class);
        assertNotNull(retrieved);
        assertEquals(task.getName(), retrieved.getName());
        assertEquals(task.getDescription(), retrieved.getDescription());
        assertEquals(task.getStatus(), retrieved.getStatus());
        assertEquals(task.getDuration(), retrieved.getDuration());
        assertEquals(task.getStartTime(), retrieved.getStartTime());
    }
}