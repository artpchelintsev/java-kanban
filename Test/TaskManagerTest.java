import Manager.Managers;
import Manager.TaskManager;
import Tasks.Epic;
import Tasks.Status;
import Tasks.SubTask;
import Tasks.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskManagerTest {
    @Test
    public void testTaskEquality() {
        Task task1 = new Task(1, "Task1", "Description1", Status.NEW);
        Task task2 = new Task(1, "Task2", "Description2", Status.DONE);
        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны.");
    }

    @Test
    public void testHistoryManager() {
        TaskManager manager = Managers.getDefault();
        Task task = manager.createTask("Task", "Description", Status.NEW);
        manager.getTaskById(task.getId());
        assertEquals(1, manager.getHistory().size(),
                "История должна содержать одну задачу");
    }

    @Test
    public void testRemoveSubtaskFromEpic() {
        TaskManager manager = Managers.getDefault();
        Epic epic = manager.createEpic("Epic1", "Description1", Status.NEW);
        SubTask subTask = manager.createSubTask("Subtask1", "Description1", Status.NEW, epic.getId());

        manager.deleteSubTaskById(subTask.getId());

        assertFalse(epic.getSubTaskIds().contains(subTask.getId()), "Подзадача должна быть удалена из эпика");
        assertEquals(Status.NEW, epic.getStatus(), "Статус эпика должен быть NEW");
    }

    @Test
    public void testHistoryHasNoDuplicates() {
        TaskManager manager = Managers.getDefault();
        Task task = manager.createTask("Task", "Description", Status.NEW);

        manager.getTaskById(task.getId());
        manager.getTaskById(task.getId());
        manager.getTaskById(task.getId());
        manager.getTaskById(task.getId());

        List<Task> history = manager.getHistory();

        assertEquals(1, history.size(), "История должна содержать лишь одну задачу.");
        assertEquals(task, history.getFirst(), "Задача должна быть уникальной.");
    }

    @Test
    public void testTaskRemovalFromHistory() {
        TaskManager manager = Managers.getDefault();
        Task task = manager.createTask("Task", "Description", Status.NEW);

        manager.getTaskById(task.getId());

        manager.deleteTaskById(task.getId());

        assertTrue(manager.getHistory().isEmpty(), "История должна быть пустой после удаления задачи");
    }

    @Test
    public void testEpicWithSubtasksRemoval() {
        TaskManager manager = Managers.getDefault();
        Epic epic = manager.createEpic("Epic", "Description", Status.NEW);
        SubTask subTask = manager.createSubTask("Subtask", "Description", Status.NEW, epic.getId());

        manager.getEpicById(epic.getId());
        manager.getSubTaskById(subTask.getId());

        manager.deleteEpicById(epic.getId());

        assertTrue(manager.getHistory().isEmpty(), "История должна быть пустой после удаления эпика");
        assertFalse(manager.getAllSubTasks().contains(subTask), "Подзадача должна быть удалена");
    }

    @Test
    public void testHistoryOrder() {
        TaskManager manager = Managers.getDefault();
        Task task1 = manager.createTask("Task1", "Description1", Status.NEW);
        Task task2 = manager.createTask("Task2", "Description2", Status.NEW);
        Task task3 = manager.createTask("Task3", "Description3", Status.NEW);

        manager.getTaskById(task2.getId());
        manager.getTaskById(task1.getId());
        manager.getTaskById(task3.getId());

        List<Task> history = manager.getHistory();

        assertEquals(3, history.size(), "История должна содержать 3 задачи");
        assertEquals(task2, history.get(0), "Первая задача в истории должна быть task2");
        assertEquals(task1, history.get(1), "Вторая задача в истории должна быть task1");
        assertEquals(task3, history.get(2), "Третья задача в истории должна быть task3");
    }

}
