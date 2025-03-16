package Manager;

import Tasks.Epic;
import Tasks.Status;
import Tasks.SubTask;
import Tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private int incrementId = 1;
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();


    //получение всех списков (2.а)
    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    //Удаление всех задач (2.b)
    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllSubTasks() {
        subTasks.clear();
        for (Epic epic : epics.values()) {
            if (epic != null) {
                epic.getSubTaskIds().clear();
                updateEpicStatus(epic.getId());
            }
        }
    }

    public void deleteAllEpics() {
        epics.clear();
        subTasks.clear();
    }

    //Получение по идентификатору (2.с)
    @Override
    public void getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
    }

    @Override
    public SubTask getSubTaskById(int id) {
        SubTask subTask = subTasks.get(id);
        if (subTask != null) {
            historyManager.add(subTask);
        }
        return subTask;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    //Создание (2.d)
    @Override
    public Task createTask(String name, String description, Status status) {
        Task task = new Task(generateId(), name, description, status);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic createEpic(String name, String description, Status status) {
        Epic epic = new Epic(generateId(), name, description, status.toString());
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public SubTask createSubTask(String name, String description, Status status, int epicId) {
        SubTask subTask = new SubTask(generateId(), name, description, status, epicId);
        subTasks.put(subTask.getId(), subTask);

        Epic epic = epics.get(epicId);
        if (epic != null) {
            epic.addSubtaskId(subTask.getId());
            updateEpicStatus(epicId);
        }
        return subTask;
    }

    //Обновление (2.e)
    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateSubtask(SubTask subTask) {
        subTasks.put(subTask.getId(), subTask);
        updateEpicStatus(subTask.getEpicId());
    }

    @Override
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic.getId());
    }

    //Удаление (2.f)
    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    @Override
    public void deleteSubTaskById(int id) {
        SubTask subTask = subTasks.remove(id);
        if (subTask != null) {
            Epic epic = epics.get(subTask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
            }

        }

    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subTaskId : epic.getSubTaskIds()) {
                subTasks.remove(subTaskId);
            }
        }
    }

    //Подзадачи эпика
    public ArrayList<SubTask> getSubtasksByEpicId(int epicId) {
        ArrayList<SubTask> result = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (int subtaskId : epic.getSubTaskIds()) {
                SubTask subtask = subTasks.get(subtaskId);
                if (subtask != null) {
                    result.add(subtask);
                }
            }
        }
        return result;
    }

    //Получение истории
    @Override
    public ArrayList<Task> getHistory() {
        return historyManager.getHistory();
    }

    //Обновление статусов
    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            ArrayList<Integer> subTaskIds = epic.getSubTaskIds();
            if (subTaskIds.isEmpty()) {
                epic.setStatus(String.valueOf(Status.NEW));
                return;
            }
            boolean allSubTasksDone = true;
            boolean allSubTasksNew = true;

            for (int subTaskId : subTaskIds) {
                SubTask subTask = subTasks.get(subTaskId);
                if (subTask != null) {
                    if (subTask.getStatus() != Status.DONE) {
                        allSubTasksDone = false;
                    }
                    if (subTask.getStatus() != Status.NEW) {
                        allSubTasksNew = false;
                    }
                }
            }
            if (allSubTasksDone) {
                epic.setStatus(String.valueOf(Status.DONE));
            } else if (allSubTasksNew) {
                epic.setStatus(String.valueOf(Status.NEW));
            } else {
                epic.setStatus(String.valueOf(Status.IN_PROGRESS));
            }
        }
    }

    private int generateId() {
        return incrementId++;
    }
}
