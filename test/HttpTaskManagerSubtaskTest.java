import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerSubtasksTest extends HttpTaskManagerTestBase {

    @Test
    void testCreateSubTask() throws IOException, InterruptedException {
        Epic epic = taskManager.createEpic("Epic", "Desc",
                Status.valueOf(Status.NEW.toString()), null, null);

        SubTask subTask = new SubTask(0, "Subtask", "Desc", Status.NEW,
                epic.getId(), Duration.ofMinutes(30), LocalDateTime.now());
        String subTaskJson = gson.toJson(subTask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subTaskJson))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<SubTask> subTasks = taskManager.getAllSubTasks();
        assertEquals(1, subTasks.size());
        assertEquals("Subtask", subTasks.get(0).getName());
    }

    @Test
    void testGetSubTaskById() throws IOException, InterruptedException {
        Epic epic = taskManager.createEpic("Epic", "Desc",
                Status.valueOf(Status.NEW.toString()), null, null);
        SubTask subTask = taskManager.createSubTask("Subtask", "Desc",
                Status.NEW, epic.getId(), null, null);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/subtasks/" + subTask.getId()))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Subtask"));
    }

    @Test
    void testDeleteSubTask() throws IOException, InterruptedException {
        Epic epic = taskManager.createEpic("Epic", "Desc",
                Status.valueOf(Status.NEW.toString()), null, null);
        SubTask subTask = taskManager.createSubTask("Subtask", "Desc",
                Status.NEW, epic.getId(), null, null);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/subtasks/" + subTask.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getAllSubTasks().isEmpty());
    }
}
