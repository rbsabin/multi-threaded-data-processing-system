public class Task {

    private final int taskId;
    private final String data;
    private final boolean poisonPill;

    public Task(int taskId, String data) {
        this.taskId = taskId;
        this.data = data;
        this.poisonPill = false;
    }

    private Task(boolean poisonPill) {
        this.taskId = -1;
        this.data = "";
        this.poisonPill = poisonPill;
    }

    public static Task createPoisonPill() {
        return new Task(true);
    }

    public int getTaskId() {
        return taskId;
    }

    public String getData() {
        return data;
    }

    public boolean isPoisonPill() {
        return poisonPill;
    }

    @Override
    public String toString() {
        return "Task-" + taskId + " [" + data + "]";
    }
}
