import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Worker implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Worker.class.getName());

    private final int workerId;
    private final SharedTaskQueue taskQueue;
    private final List<String> results;

    public Worker(int workerId, SharedTaskQueue taskQueue, List<String> results) {
        this.workerId = workerId;
        this.taskQueue = taskQueue;
        this.results = results;
    }

    @Override
    public void run() {
        String workerName = "Worker-" + workerId;
        LOGGER.info(workerName + " started.");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                Task task = taskQueue.getTask();

                if (task.isPoisonPill()) {
                    LOGGER.info(workerName + " received shutdown signal.");
                    break;
                }

                try {
                    processTask(task, workerName);
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, workerName + " failed processing " + task, e);
                    results.add(task + " failed by " + workerName + ": " + e.getClass().getSimpleName());
                }
            }
        } catch (InterruptedException e) {
            LOGGER.warning(workerName + " interrupted.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, workerName + " unexpected worker error", e);
        } finally {
            LOGGER.info(workerName + " completed.");
        }
    }

    private void processTask(Task task, String workerName) throws InterruptedException {
        LOGGER.info(workerName + " processing " + task);

        int delay = ThreadLocalRandom.current().nextInt(100, 301);
        Thread.sleep(delay);

        results.add(task + " processed by " + workerName);
        LOGGER.info(workerName + " completed " + task);
    }
}
