package Tasks;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subTaskIds;

    public Epic(int id, String name, String description, String status) {
        super(id, name, description, Status.valueOf(status));
        this.subTaskIds = new ArrayList<>();
    }

    public ArrayList<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void addSubtaskId(int subTaskId) {
        subTaskIds.add(subTaskId);
    }

    public void removeSubtaskId(int subTaskId) {
        subTaskIds.remove(subTaskId);
    }

    @Override
    public String toString() {
        return "Tasks.Epic{" +
                "id=" + getId() +
                ", name =" + getName() +
                ", description='" + getDescription() + '\'' +
                ", status =" + getStatus() +
                ", subTaskIds=" + subTaskIds +
                '}';
    }
}
