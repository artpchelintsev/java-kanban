package manager;

import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskManager {
    //получение всех списков (2.а)
    List<Task> getAllTasks();

    List<SubTask> getAllSubTasks();

    List<Epic> getAllEpics();

    //получение по идентификатору (2.с)
    Task getTaskById(int id);

    SubTask getSubTaskById(int id);

    Epic getEpicById(int id);

    //creation (2.d)
    Task createTask(String name, String description, Status status, Duration duration, LocalDateTime startTime);

    Epic createEpic(String name, String description, Status status, Duration duration, LocalDateTime startTime);

    SubTask createSubTask(String name, String description, Status status, int epicId,
                          Duration duration, LocalDateTime startTime);

    //Обновление (2.e)
    void updateTask(Task task);

    void updateSubtask(SubTask subTask);

    void updateEpic(Epic epic);

    //Удаление по айди (2.f)
    void deleteTaskById(int id);

    void deleteSubTaskById(int id);

    void deleteEpicById(int id);

    //Получение истории.
    List<Task> getHistory();

    //Получение отсортированного списка
    List<Task> getPrioritizedTasks();

    //Проверка пересечений
    boolean hasTimeConflict(Task task);

    void deleteAllTasks();

    void deleteAllSubTasks();

    void deleteAllEpics();
}
