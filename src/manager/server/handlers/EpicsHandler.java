package manager.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import tasks.Epic;

import java.io.IOException;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public EpicsHandler(TaskManager taskManager, Gson gson) {
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
                        handleGetAllEpics(exchange);
                    } else if (pathParts.length == 3) {
                        int id = Integer.parseInt(pathParts[2]);
                        handleGetEpicById(exchange, id);
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                case "POST":
                    handleCreateOrUpdateEpic(exchange);
                    break;
                case "DELETE":
                    if (pathParts.length == 3) {
                        int id = Integer.parseInt(pathParts[2]);
                        handleDeleteEpic(exchange, id);
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

    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        List<Epic> epics = taskManager.getAllEpics();
        String response = gson.toJson(epics);
        sendText(exchange, response, 200);
    }

    private void handleGetEpicById(HttpExchange exchange, int id) throws IOException {
        Epic epic = taskManager.getEpicById(id);
        if (epic != null) {
            String response = gson.toJson(epic);
            sendText(exchange, response, 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleCreateOrUpdateEpic(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        Epic epic = gson.fromJson(requestBody, Epic.class);
        if (epic.getId() == 0) {
            taskManager.createEpic(epic.getName(), epic.getDescription(),
                    epic.getStatus(), epic.getDuration(), epic.getStartTime());
            sendText(exchange, "Epic created", 201);
        } else {
            taskManager.updateEpic(epic);
            sendText(exchange, "Epic updated", 201);
        }
    }

    private void handleDeleteEpic(HttpExchange exchange, int id) throws IOException {
        taskManager.deleteEpicById(id);
        sendText(exchange, "Epic deleted", 200);
    }

}
