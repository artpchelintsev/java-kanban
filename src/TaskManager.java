import Tasks.Epic;
import Tasks.Status;
import Tasks.SubTask;
import Tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private int incrementId = 1;
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, SubTask> subTasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();


    //получение всех списков (2.а)
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    //удаление всех задач (2.b)
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

    //получение по идентификатору (2.с)
    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public SubTask getSubTaskById(int id) {
        return subTasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    //creation (2.d)
    public Task createTask(String name, String description, Status status) {
        Task task = new Task(generateId(), name, description, status);
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(String name, String description, Status status) {
        Epic epic = new Epic(generateId(), name, description, status.toString());
        epics.put(epic.getId(), epic);
        return epic;
    }

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
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateSubtask(SubTask subTask) {
        subTasks.put(subTask.getId(), subTask);
        updateEpicStatus(subTask.getEpicId());
    }

    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic.getId());
    }

    //Удаление (2.f)
    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

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

    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subTaskId : epic.getSubTaskIds()) {
                subTasks.remove(subTaskId);
            }
        }
    }

    //подзадачи эпика
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
