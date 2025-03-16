package Tests;

import Manager.Managers;
import Manager.TaskManager;
import Tasks.Epic;
import Tasks.Status;
import Tasks.SubTask;
import Tasks.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

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
    public void testHistoryLimit() {
        TaskManager manager = Managers.getDefault();
        for (int i = 1; i <= 12; i++) {
            Task task = manager.createTask("Task" + i, "Description" + i, Status.NEW);
            manager.getTaskById(task.getId());
        }
        ArrayList<Task> history = manager.getHistory();
        assertEquals(10, history.size(), "История должна содержать не более 10 задач");
        assertFalse(history.stream().anyMatch(task -> task.getName().equals("Task1")),
                "Первая задача должна быть удалена из истории");
        assertFalse(history.stream().anyMatch(task -> task.getName().equals("Task2")),
                "Вторая задача должна быть удалена из истории");
    }

}
