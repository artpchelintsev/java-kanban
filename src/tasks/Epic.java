package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private ArrayList<Integer> subTaskIds;
    private LocalDateTime endTime;

    public Epic(int id, String name, String description, String status,
                Duration duration, LocalDateTime startTime) {
        super(id, name, description, Status.valueOf(status), duration, startTime);
        this.subTaskIds = new ArrayList<>();
    }

    public List<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void addSubtaskId(int subTaskId) {
        subTaskIds.add(subTaskId);
    }

    public void removeSubtaskId(int subTaskId) {
        subTaskIds.remove(Integer.valueOf(subTaskId));
    }

    @Override
    public String toString() {
        return "Tasks.Epic{" +
                "id=" + getId() +
                ", name =" + getName() +
                ", description='" + getDescription() + '\'' +
                ", status =" + getStatus() +
                ", subTaskIds=" + subTaskIds +
                ", startTime=" + getStartTime() +
                ", endTime=" + endTime +
                ", duration=" + getDuration() +
                '}';
    }

    public void updateTimeFields(List<SubTask> subtasks) {
        if (subtasks.isEmpty()) {
            this.setDuration(null);
            this.setStartTime(null);
            this.endTime = null;
            return;
        }

        this.setStartTime(subtasks.stream()
                .map(SubTask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null));

        this.endTime = subtasks.stream()
                .map(SubTask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        this.setDuration(subtasks.stream()
                .map(SubTask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus));
    }


    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }
}
