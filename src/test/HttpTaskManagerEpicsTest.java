package test;

import com.google.gson.Gson;
import http.HttpTaskServer;
import model.Epic;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerEpicsTest {
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
    void shouldAddAndGetEpic() throws IOException, InterruptedException {
        Epic epic = new Epic(1, "Epic 1", "Epic description");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic/?id=1"))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());
        Epic retrieved = gson.fromJson(getResponse.body(), Epic.class);
        assertEquals(epic.getName(), retrieved.getName());
    }
}

