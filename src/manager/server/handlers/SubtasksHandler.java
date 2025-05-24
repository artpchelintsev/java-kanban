package manager.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import tasks.SubTask;

import java.io.IOException;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public SubtasksHandler(TaskManager taskManager, Gson gson) {
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
                        handleGetAllSubtasks(exchange);
                    } else if (pathParts.length == 3) {
                        int id = Integer.parseInt(pathParts[2]);
                        handleGetSubtaskById(exchange, id);
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                case "POST":
                    handleCreateOrUpdateSubtask(exchange);
                    break;
                case "DELETE":
                    if (pathParts.length == 3) {
                        int id = Integer.parseInt(pathParts[2]);
                        handleDeleteSubtask(exchange, id);
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGetAllSubtasks(HttpExchange exchange) throws IOException {
        List<SubTask> subtasks = taskManager.getAllSubTasks();
        String response = gson.toJson(subtasks);
        sendText(exchange, response, 200);
    }

    private void handleGetSubtaskById(HttpExchange exchange, int id) throws IOException {
        SubTask subtask = taskManager.getSubTaskById(id);
        if (subtask != null) {
            String response = gson.toJson(subtask);
            sendText(exchange, response, 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleCreateOrUpdateSubtask(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        SubTask subtask = gson.fromJson(requestBody, SubTask.class);
        try {
            if (subtask.getId() == 0) {
                taskManager.createSubTask(subtask.getName(), subtask.getDescription(),
                        subtask.getStatus(), subtask.getEpicId(), subtask.getDuration(), subtask.getStartTime());
                sendText(exchange, "Subtask created", 201);
            } else {
                taskManager.updateSubtask(subtask);
                sendText(exchange, "Subtask updated", 201);
            }
        } catch (Exception e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDeleteSubtask(HttpExchange exchange, int id) throws IOException {
        taskManager.deleteSubTaskById(id);
        sendText(exchange, "Subtask deleted", 200);
    }

}
