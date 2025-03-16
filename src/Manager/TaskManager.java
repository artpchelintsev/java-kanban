package Manager;

import Tasks.Epic;
import Tasks.Status;
import Tasks.SubTask;
import Tasks.Task;

import java.util.ArrayList;

public interface TaskManager {
    //получение всех списков (2.а)
    ArrayList<Task> getAllTasks();

    ArrayList<SubTask> getAllSubTasks();

    ArrayList<Epic> getAllEpics();

    //получение по идентификатору (2.с)
    void getTaskById(int id);

    SubTask getSubTaskById(int id);

    Epic getEpicById(int id);

    //creation (2.d)
    Task createTask(String name, String description, Status status);

    Epic createEpic(String name, String description, Status status);

    SubTask createSubTask(String name, String description, Status status, int epicId);

    //Обновление (2.e)
    void updateTask(Task task);

    void updateSubtask(SubTask subTask);

    void updateEpic(Epic epic);

    //Удаление по айди (2.f)
    void deleteTaskById(int id);

    void deleteSubTaskById(int id);

    void deleteEpicById(int id);

    //Получение истории топ 10.
    ArrayList<Task> getHistory();
}
