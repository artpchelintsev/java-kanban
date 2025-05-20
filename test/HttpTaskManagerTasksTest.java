import org.junit.jupiter.api.Test;
import tasks.Status;
import tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerTasksTest extends HttpTaskManagerTestBase {

    @Test
    void testAddTask() throws IOException, InterruptedException {
        Task task = new Task(0, "Test task", "Test description",
                Status.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Неверный код статуса при создании задачи");

        List<Task> tasks = taskManager.getAllTasks();
        assertEquals(1, tasks.size(), "Неверное количество задач после создания");
        assertEquals("Test task", tasks.get(0).getName(), "Неверное имя задачи");
    }

    @Test
    void testGetTaskById() throws IOException, InterruptedException {
        Task task = taskManager.createTask("Test", "Desc",
                Status.NEW, Duration.ofMinutes(30), LocalDateTime.now());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/tasks/" + task.getId()))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код статуса при получении задачи");
        assertTrue(response.body().contains("Test"), "Тело ответа не содержит название задачи");
    }

    @Test
    void testGetTaskByIdNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Неверный код статуса для несуществующей задачи");
    }

    @Test
    void testDeleteTask() throws IOException, InterruptedException {
        Task task = taskManager.createTask("Test", "Desc",
                Status.NEW, Duration.ofMinutes(30), LocalDateTime.now());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/tasks/" + task.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код статуса при удалении");
        assertTrue(taskManager.getAllTasks().isEmpty(), "Задачи не были удалены");
    }

    @Test
    void testDeleteAllTasks() throws IOException, InterruptedException {
        taskManager.createTask("Test1", "Desc1", Status.NEW, null, null);
        taskManager.createTask("Test2", "Desc2", Status.NEW, null, null);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/tasks"))
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void testTimeConflict() throws IOException, InterruptedException {
        Task task1 = new Task(0, "Task1", "Desc", Status.NEW,
                Duration.ofHours(2), LocalDateTime.of(2023, 1, 1, 10, 0));
        String taskJson1 = gson.toJson(task1);

        HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/tasks"))
                        .POST(HttpRequest.BodyPublishers.ofString(taskJson1))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        Task task2 = new Task(0, "Task2", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2023, 1, 1, 11, 0));
        String taskJson2 = gson.toJson(task2);

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/tasks"))
                        .POST(HttpRequest.BodyPublishers.ofString(taskJson2))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "Неверный код статуса при конфликте времени");
        assertEquals(1, taskManager.getAllTasks().size(), "Конфликтная задача была создана");
    }
}
