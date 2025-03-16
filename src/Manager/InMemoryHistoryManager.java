package Manager;

import Tasks.Task;

import java.util.ArrayList;
import java.util.LinkedList;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY_SIZE = 10;
    private final LinkedList<Task> history = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (history.size() == MAX_HISTORY_SIZE) {
            history.removeFirst();
        }
        history.addLast(task);
    }

    @Override
    public ArrayList<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
