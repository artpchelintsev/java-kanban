import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerEpicsTest extends HttpTaskManagerTestBase {

    @Test
    void testCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic(0, "Test epic", "Test description",
                Status.NEW.toString(), null, null);
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Epic> epics = taskManager.getAllEpics();
        assertEquals(1, epics.size());
        assertEquals("Test epic", epics.get(0).getName());
    }

    @Test
    void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = taskManager.createEpic("Test", "Desc",
                Status.valueOf(Status.NEW.toString()), null, null);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/epics/" + epic.getId()))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test"));
    }

    @Test
    void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = taskManager.createEpic("Test", "Desc",
                Status.valueOf(Status.NEW.toString()), null, null);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/epics/" + epic.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getAllEpics().isEmpty());
    }

}
