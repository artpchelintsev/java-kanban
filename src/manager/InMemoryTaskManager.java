package manager;

import manager.exceptions.ManagerSaveException;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    int incrementId = 1;
    final HashMap<Integer, Task> tasks = new HashMap<>();
    final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(
                    Task::getStartTime,
                    Comparator.nullsLast(Comparator.naturalOrder())
            )
    );


    //получение всех списков (2.а)
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    //Удаление всех задач (2.b)
    public void deleteAllTasks() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }
        tasks.clear();
    }

    public void deleteAllSubTasks() {
        for (Integer id : subTasks.keySet()) {
            historyManager.remove(id);
        }
        subTasks.clear();
        for (Epic epic : epics.values()) {
            if (epic != null) {
                epic.getSubTaskIds().clear();
                updateEpicStatus(epic.getId());
            }
        }
    }

    public void deleteAllEpics() {
        for (Integer id : epics.keySet()) {
            historyManager.remove(id);
        }
        for (Integer id : subTasks.keySet()) {
            historyManager.remove(id);
        }
        epics.clear();
        subTasks.clear();
    }

    //Получение по идентификатору (2.с)
    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
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
    public Task createTask(String name, String description, Status status, Duration duration, LocalDateTime startTime) {
        Task task = new Task(generateId(), name, description, status, duration, startTime);
        if (hasTimeConflict(task)) {
            throw new ManagerSaveException("Задача пересекается по времени с существующей");
        }
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return task;
    }

    @Override
    public Epic createEpic(String name, String description, Status status, Duration duration, LocalDateTime startTime) {
        Epic epic = new Epic(generateId(), name, description, status.toString(), duration, startTime);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public SubTask createSubTask(String name, String description, Status status, int epicId,
                                 Duration duration, LocalDateTime startTime) {
        SubTask subTask = new SubTask(generateId(), name, description, status, epicId, duration, startTime);
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
        historyManager.remove(id);
    }

    @Override
    public void deleteSubTaskById(int id) {
        SubTask subTask = subTasks.remove(id);
        if (subTask != null) {
            Epic epic = epics.get(subTask.getEpicId());
            historyManager.remove(id);
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
            }

        }

    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        historyManager.remove(id);
        if (epic != null) {
            for (int subTaskId : epic.getSubTaskIds()) {
                subTasks.remove(subTaskId);
                historyManager.remove(subTaskId);
            }
        }
    }

    //Подзадачи эпика
    public List<SubTask> getSubtasksByEpicId(int epicId) {
        List<SubTask> result = new ArrayList<>();
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
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    //Обновление статусов
    void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            List<Integer> subTaskIds = epic.getSubTaskIds();
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

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public boolean hasTimeConflict(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getDuration() == null) {
            return false;
        }

        LocalDateTime newStart = newTask.getStartTime();
        LocalDateTime newEnd = newTask.getEndTime();

        return prioritizedTasks.stream()
                .filter(task -> task.getId() != newTask.getId())
                .filter(task -> task.getStartTime() != null)
                .anyMatch(existingTask -> {
                    LocalDateTime existingStart = existingTask.getStartTime();
                    LocalDateTime existingEnd = existingTask.getEndTime();
                    return !(newEnd.isBefore(existingStart) || newStart.isAfter(existingEnd));
                });
    }

    private int generateId() {
        return incrementId++;
    }

    private boolean isTimeOverlapping(Task task1, Task task2) {
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        return !(end1.isBefore(start2) || end2.isBefore(start1));
    }


}
