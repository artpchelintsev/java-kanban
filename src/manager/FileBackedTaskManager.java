package manager;

import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;
import manager.exceptions.ManagerSaveException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public Task createTask(String name, String description, Status status) {
        Task task = super.createTask(name, description, status);
        save();
        return task;
    }

    @Override
    public Epic createEpic(String name, String description, Status status) {
        Epic epic = super.createEpic(name, description, status);
        save();
        return epic;
    }

    @Override
    public SubTask createSubTask(String name, String description, Status status, int epicId) {
        SubTask subTask = super.createSubTask(name, description, status, epicId);
        save();
        return subTask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(SubTask subTask) {
        super.updateSubtask(subTask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteSubTaskById(int id) {
        super.deleteSubTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }


    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            if (lines.length <= 1) {
                return manager;
            }

            int maxId = 0;
            List<Task> tasks = new ArrayList<>();

            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (line.isEmpty()) {
                    continue;
                }

                Task task = fromString(line);
                if (task != null) {
                    tasks.add(task);
                    if (task.getId() > maxId) {
                        maxId = task.getId();
                    }
                }
            }
            manager.incrementId = maxId + 1;

            for (Task task : tasks) {
                if (task instanceof SubTask) {
                    SubTask subTask = (SubTask) task;
                    manager.subTasks.put(subTask.getId(), subTask);

                    Epic epic = manager.epics.get(subTask.getEpicId());
                    if (epic != null) {
                        epic.addSubtaskId(subTask.getId());
                    }
                } else if (task instanceof Epic) {
                    manager.epics.put(task.getId(), (Epic) task);
                } else {
                    manager.tasks.put(task.getId(), task);
                }
            }

            for (Epic epic : manager.epics.values()) {
                manager.updateEpicStatus(epic.getId());
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла.", e);
        }
        return manager;
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",");
        if (parts.length < 5) {
            return null;
        }

        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        switch (type) {
            case "TASK":
                return new Task(id, name, description, status);
            case "EPIC":
                Epic epic = new Epic(id, name, description, status.toString());
                return epic;
            case "SUBTASK":
                if (parts.length < 6) {
                    return null;
                }
                int epicId = Integer.parseInt(parts[5]);
                return new SubTask(id, name, description, status, epicId);
            default:
                return null;
        }
    }

    private void save() {
        try {
            StringBuilder data = new StringBuilder("id,type,name,status,description,epic\n");
            for (Task task : getAllTasks()) {
                data.append(taskToString(task)).append("\n");
            }

            for (Epic epic : getAllEpics()) {
                data.append(taskToString(epic)).append("\n");
            }

            for (SubTask subTask : getAllSubTasks()) {
                data.append(taskToString(subTask)).append("\n");
            }

            Files.writeString(file.toPath(), data.toString());
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл.", e);
        }
    }

    private String taskToString(Task task) {
        if (task instanceof SubTask) {
            SubTask subTask = (SubTask) task;
            return String.format("%d,SUBTASK,%s,%s,%s,%d",
                    subTask.getId(),
                    subTask.getName(),
                    subTask.getStatus(),
                    subTask.getDescription(),
                    subTask.getEpicId());
        } else if (task instanceof Epic) {
            return String.format("%d,EPIC,%s,%s,%s,",
                    task.getId(),
                    task.getName(),
                    task.getStatus(),
                    task.getDescription());
        } else {
            return String.format("%d,TASK,%s,%s,%s,",
                    task.getId(),
                    task.getName(),
                    task.getStatus(),
                    task.getDescription());
        }
    }
}
