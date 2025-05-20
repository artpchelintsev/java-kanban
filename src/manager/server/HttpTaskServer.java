package manager.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import manager.TaskManager;
import manager.Managers;
import manager.server.adapters.DurationAdapter;
import manager.server.adapters.LocalDateTimeAdapter;
import manager.server.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private final int port;
    private final HttpServer httpServer;
    private final TaskManager taskManager;
    private final Gson gson;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this(taskManager, 8080);
    }

    public HttpTaskServer(TaskManager taskManager, int port) throws IOException {
        this.taskManager = taskManager;
        this.port = port;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();


        this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        this.httpServer.createContext("/tasks", (HttpHandler) new TasksHandler(taskManager, gson));
        this.httpServer.createContext("/subtasks", (HttpHandler) new SubtasksHandler(taskManager, gson));
        this.httpServer.createContext("/epics", (HttpHandler) new EpicsHandler(taskManager, gson));
        this.httpServer.createContext("/history", (HttpHandler) new HistoryHandler(taskManager, gson));
        this.httpServer.createContext("/prioritized", (HttpHandler) new PrioritizedHandler(taskManager, gson));
    }

    public Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    public void start() {
        httpServer.start();
        System.out.println("HTTP-сервер запущен на " + port + " порту.");
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("HTTP-сервер остановлен.");
    }

    public int getPort() {
        return port;
    }

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Managers.getDefault();
        HttpTaskServer httpTaskServer = new HttpTaskServer(taskManager, 0);
        httpTaskServer.start();
    }

}