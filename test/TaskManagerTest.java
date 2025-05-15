import manager.TaskManager;
import manager.exceptions.ManagerSaveException;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;
    private final LocalDateTime testStartTime = LocalDateTime.of(2023, 1, 1, 10, 0);
    private final Duration testDuration = Duration.ofHours(1);


    @Test
    void testTaskTimeConflict() {
        manager.createTask("Task1", "Desc", Status.NEW,
                Duration.ofHours(2), LocalDateTime.of(2023, 1, 1, 10, 0));

        assertThrows(ManagerSaveException.class, () -> {
            manager.createTask("Task2", "Desc", Status.NEW,
                    Duration.ofHours(1), LocalDateTime.of(2023, 1, 1, 11, 0));
        });
    }

    @Test
    void testPrioritizedTasks() {
        Task task1 = manager.createTask("Task1", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2023, 1, 1, 12, 0));
        Task task2 = manager.createTask("Task2", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2023, 1, 1, 10, 0));

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(2, prioritized.size());
        assertEquals(task2, prioritized.get(0));
        assertEquals(task1, prioritized.get(1));
    }

    @Test
    void testTasksWithoutTimeNotInPrioritizedList() {
        Task taskWithTime = manager.createTask("Task1", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2023, 1, 1, 12, 0));
        Task taskWithoutTime = manager.createTask("Task2", "Desc", Status.NEW, null, null);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(1, prioritized.size());
        assertTrue(prioritized.contains(taskWithTime));
        assertFalse(prioritized.contains(taskWithoutTime));
    }

    @Test
    void testHistoryManager() {
        Task task = manager.createTask("Task", "Description", Status.NEW, testDuration, testStartTime);
        manager.getTaskById(task.getId());
        assertEquals(1, manager.getHistory().size(),
                "История должна содержать одну задачу");
    }

    @Test
    void testRemoveSubtaskFromEpic() {
        Epic epic = manager.createEpic("Epic1", "Description1", Status.NEW, null, null);
        SubTask subTask = manager.createSubTask("Subtask1", "Description1", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.now());

        manager.deleteSubTaskById(subTask.getId());

        assertFalse(epic.getSubTaskIds().contains(subTask.getId()), "Подзадача должна быть удалена из эпика");
        assertEquals(Status.NEW, epic.getStatus(), "Статус эпика должен быть NEW");
    }

    @Test
    void testHistoryHasNoDuplicates() {
        Task task = manager.createTask("Task", "Description", Status.NEW, testDuration, testStartTime);

        manager.getTaskById(task.getId());
        manager.getTaskById(task.getId());
        manager.getTaskById(task.getId());
        manager.getTaskById(task.getId());

        List<Task> history = manager.getHistory();

        assertEquals(1, history.size(), "История должна содержать лишь одну задачу.");
        assertEquals(task, history.get(0), "Задача должна быть уникальной.");
    }

    @Test
    void testTaskRemovalFromHistory() {
        Task task = manager.createTask("Task", "Description", Status.NEW, testDuration, testStartTime);

        manager.getTaskById(task.getId());
        manager.deleteTaskById(task.getId());

        assertTrue(manager.getHistory().isEmpty(), "История должна быть пустой после удаления задачи");
    }

    @Test
    void testEpicWithSubtasksRemoval() {
        Epic epic = manager.createEpic("Epic", "Description", Status.NEW, null, null);
        SubTask subTask = manager.createSubTask("Subtask", "Description", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.now());

        manager.getEpicById(epic.getId());
        manager.getSubTaskById(subTask.getId());
        manager.deleteEpicById(epic.getId());

        assertTrue(manager.getHistory().isEmpty(), "История должна быть пустой после удаления эпика");
        assertFalse(manager.getAllSubTasks().contains(subTask), "Подзадача должна быть удалена");
    }

    @Test
    void testHistoryOrder() {
        Task task1 = manager.createTask("Task1", "Description1", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2023, 1, 1, 10, 0));
        Task task2 = manager.createTask("Task2", "Description2", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2023, 1, 1, 11, 0));
        Task task3 = manager.createTask("Task3", "Description3", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2023, 1, 1, 12, 0));

        manager.getTaskById(task2.getId());
        manager.getTaskById(task1.getId());
        manager.getTaskById(task3.getId());

        List<Task> history = manager.getHistory();

        assertEquals(3, history.size(), "История должна содержать 3 задачи");
        assertEquals(task2, history.get(0), "Первая задача в истории должна быть task2");
        assertEquals(task1, history.get(1), "Вторая задача в истории должна быть task1");
        assertEquals(task3, history.get(2), "Третья задача в истории должна быть task3");
    }

    @Test
    void testEpicStatusCalculation() {

        Epic epicNew = manager.createEpic("EpicNew", "Desc", Status.NEW, null, null);
        SubTask subNew = manager.createSubTask("SubNew", "Desc", Status.NEW, epicNew.getId(),
                testDuration, testStartTime);
        assertEquals(Status.NEW, epicNew.getStatus());


        Epic epicDone = manager.createEpic("EpicDone", "Desc", Status.NEW, null, null);
        SubTask subDone = manager.createSubTask("SubDone", "Desc", Status.DONE, epicDone.getId(),
                testDuration, testStartTime);
        assertEquals(Status.DONE, epicDone.getStatus());


        Epic epicMixed = manager.createEpic("EpicMixed", "Desc", Status.NEW, null, null);
        SubTask subMixed1 = manager.createSubTask("SubMixed1", "Desc", Status.NEW, epicMixed.getId(),
                testDuration, testStartTime);
        SubTask subMixed2 = manager.createSubTask("SubMixed2", "Desc", Status.DONE, epicMixed.getId(),
                testDuration, testStartTime.plusHours(1));
        assertEquals(Status.IN_PROGRESS, epicMixed.getStatus());


        Epic epicInProgress = manager.createEpic("EpicInProgress", "Desc", Status.NEW, null, null);
        SubTask subInProgress = manager.createSubTask("SubInProgress", "Desc", Status.IN_PROGRESS,
                epicInProgress.getId(), testDuration, testStartTime);
        assertEquals(Status.IN_PROGRESS, epicInProgress.getStatus());
    }
}
