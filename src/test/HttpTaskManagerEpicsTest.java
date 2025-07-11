package test;

import com.google.gson.Gson;
import http.HttpTaskServer;
import model.Epic;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpTaskManagerEpicsTest {
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
    void clearEpics() throws IOException, InterruptedException {
        HttpRequest deleteAll = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic/"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(deleteAll, HttpResponse.BodyHandlers.ofString());
        assertTrue(response.statusCode() == 200 || response.statusCode() == 204);
    }

    @Test
    void shouldAddAndGetEpic() throws IOException, InterruptedException {
        Epic epic = new Epic(1, "Epic 1", "Epic description");

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic)))
                .build();
        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic/?id=1"))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());

        Epic retrieved = gson.fromJson(getResponse.body(), Epic.class);
        assertNotNull(retrieved);
        assertEquals(epic.getName(), retrieved.getName());
        assertEquals(epic.getDescription(), retrieved.getDescription());
    }
}