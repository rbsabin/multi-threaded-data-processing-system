import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SharedTaskQueue {

    private final BlockingQueue<Task> queue;

    public SharedTaskQueue() {
        this.queue = new LinkedBlockingQueue<>();
    }

    public void addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }

        queue.add(task);
    }

    public Task getTask() throws InterruptedException {
        return queue.poll(500, TimeUnit.MILLISECONDS);
    }

    public int size() {
        return queue.size();
    }
}
