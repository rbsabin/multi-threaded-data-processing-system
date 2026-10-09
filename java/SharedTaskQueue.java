import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SharedTaskQueue {

    private final BlockingQueue<Task> queue;

    public SharedTaskQueue() {
        this.queue = new LinkedBlockingQueue<>();
    }

    public SharedTaskQueue(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    public void addTask(Task task) throws InterruptedException {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        queue.put(task);
    }

    public Task getTask() throws InterruptedException {
        return queue.take();
    }

    public int size() {
        return queue.size();
    }
}
