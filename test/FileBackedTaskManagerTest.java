import manager.FileBackedTaskManager;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBackedTaskManagerTest {
    @Test
    public void testSaveAndLoadEmptyFile() throws IOException {
        File file = Files.createTempFile("test", ".csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubTasks().isEmpty());
    }

    @Test
    public void testSaveAndLoadTasks() throws IOException {
        File file = Files.createTempFile("test", ".csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = manager.createTask("Taska", "Opisanie taski", Status.NEW);
        Epic epic = manager.createEpic("EpicN", "Opisanie EpicN", Status.NEW);
        SubTask subTask = manager.createSubTask("SubTaska", "Opisanie subtaski",
                Status.NEW, epic.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubTasks().size());

        Task loadedTask = loadedManager.getAllTasks().get(0);
        assertEquals(task.getId(), loadedTask.getId());
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getStatus(), loadedTask.getStatus());
    }

    @Test
    public void testSaveAndLoadTasksAfterUpdate() throws IOException {
        File file = Files.createTempFile("test", ".csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = manager.createTask("Taska", "Opisanie taski", Status.NEW);
        task.setStatus(String.valueOf(Status.DONE));
        manager.updateTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        Task loadedTask = loadedManager.getAllTasks().get(0);
        assertEquals(Status.DONE, loadedTask.getStatus());
    }

}
