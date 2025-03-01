import Tasks.Epic;
import Tasks.Status;
import Tasks.SubTask;
import Tasks.Task;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        Task task1 = taskManager.createTask("Issue1",
                "Description for issue1", Status.NEW);
        Task task2 = taskManager.createTask("Issue2",
                "Description for issue2", Status.DONE);

        Epic epic1 = taskManager.createEpic("Epic1", "Epic1 with 2 subtasks", Status.NEW);
        SubTask subTask1 = taskManager.createSubTask("Subtask1", "Description subtask1",
                Status.NEW, epic1.getId());
        SubTask subTask2 = taskManager.createSubTask("Subtask2", "Description subtask2",
                Status.DONE, epic1.getId());

        Epic epic2 = taskManager.createEpic("Epic2", "Epic2 with 1 subtask", Status.NEW);
        SubTask subTask3 = taskManager.createSubTask("Subtask3", "Subtask for epic2",
                Status.DONE, epic2.getId());

        //taskManager.deleteAllTasks();
        //taskManager.deleteAllSubTasks();
        //taskManager.deleteAllEpics();
        //taskManager.deleteSubTaskById(3);
        //taskManager.deleteEpicById(3);

        System.out.println("Tasks");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("Epics");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic);
        }
        System.out.println("Subtasks");
        for (SubTask subTask : taskManager.getAllSubTasks()) {
            System.out.println(subTask);
        }

    }
}
