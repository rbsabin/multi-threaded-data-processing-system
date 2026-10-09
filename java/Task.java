public class Task {

    public static final Task POISON_PILL = new Task(-1, "SHUTDOWN");

    private final int taskId;
    private final String data;

    public Task(int taskId, String data) {
        this.taskId = taskId;
        this.data = data;
    }

    public static Task createPoisonPill() {
        return POISON_PILL;
    }

    public int getTaskId() {
        return taskId;
    }

    public int getId() {
        return taskId;
    }

    public String getData() {
        return data;
    }

    public boolean isPoisonPill() {
        return this == POISON_PILL || taskId == -1;
    }

    @Override
    public String toString() {
        return "Task-" + taskId + " [" + data + "]";
    }
}
