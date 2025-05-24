import com.google.gson.Gson;
import manager.Managers;
import manager.TaskManager;
import manager.server.HttpTaskServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

public class HttpTaskManagerTestBase {
    protected HttpTaskServer httpTaskServer;
    protected TaskManager taskManager;
    protected Gson gson;
    protected String baseUrl;

    @BeforeEach
    public void setUp() throws IOException {
        taskManager = Managers.getDefault();
        httpTaskServer = new HttpTaskServer(taskManager, 8080);
        gson = httpTaskServer.getGson();
        httpTaskServer.start();
        baseUrl = "http://localhost:" + httpTaskServer.getPort();

        taskManager.deleteAllTasks();
        taskManager.deleteAllSubTasks();
        taskManager.deleteAllEpics();
    }

    @AfterEach
    public void tearDown() {
        if (httpTaskServer != null) {
            httpTaskServer.stop();
        }
    }
}