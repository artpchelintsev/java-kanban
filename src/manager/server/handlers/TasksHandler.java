package manager.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;

import tasks.Task;

import java.io.IOException;
import java.util.List;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TasksHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");

            switch (method) {
                case "GET":
                    if (pathParts.length == 2) {
                        handleGetAllTasks(exchange);
                    } else if (pathParts.length == 3) {
                        try {
                            int id = Integer.parseInt(pathParts[2]);
                            handleGetTaskById(exchange, id);
                        } catch (NumberFormatException e) {
                            sendNotFound(exchange);
                        }
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                case "POST":
                    handleCreateOrUpdateTask(exchange);
                    break;
                case "DELETE":
                    if (pathParts.length == 3) {
                        try {
                            int id = Integer.parseInt(pathParts[2]);
                            handleDeleteTask(exchange, id);
                        } catch (NumberFormatException e) {
                            sendNotFound(exchange);
                        }
                    } else if (pathParts.length == 2) {
                        handleDeleteAllTasks(exchange);
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendInternalError(exchange);
        }
    }

    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskManager.getAllTasks();
        sendJson(exchange, tasks, 200);
    }

    private void handleGetTaskById(HttpExchange exchange, int id) throws IOException {
        Task task = taskManager.getTaskById(id);
        if (task != null) {
            sendJson(exchange, task, 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleCreateOrUpdateTask(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        Task task = gson.fromJson(requestBody, Task.class);
        try {
            if (task.getId() == 0) {
                taskManager.createTask(task.getName(), task.getDescription(),
                        task.getStatus(), task.getDuration(), task.getStartTime());
                sendText(exchange, "Task created", 201);
            } else {
                taskManager.updateTask(task);
                sendText(exchange, "Task updated", 201);
            }
        } catch (Exception e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDeleteTask(HttpExchange exchange, int id) throws IOException {
        taskManager.deleteTaskById(id);
        sendText(exchange, "Task deleted", 200);
    }

    private void handleDeleteAllTasks(HttpExchange exchange) throws IOException {
        taskManager.deleteAllTasks();
        sendText(exchange, "All tasks deleted", 200);
    }

    private void sendJson(HttpExchange exchange, Object object, int statusCode) throws IOException {
        String response = gson.toJson(object);
        sendText(exchange, response, statusCode);
    }
}
