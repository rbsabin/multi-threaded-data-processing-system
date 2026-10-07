import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class Worker implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Worker.class.getName());

    private final int workerId;
    private final SharedTaskQueue taskQueue;
    private final List<String> results;

    public Worker(int workerId,
                  SharedTaskQueue taskQueue,
                  List<String> results) {
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

                if (task == null) {
                    continue;
                }

                if (task.isPoisonPill()) {
                    LOGGER.info(workerName + " received shutdown signal.");
                    break;
                }

                processTask(task, workerName);
            }

        } catch (InterruptedException e) {

            LOGGER.warning(workerName + " was interrupted.");

            Thread.currentThread().interrupt();

        } catch (RuntimeException e) {

            LOGGER.severe(
                    workerName + " encountered an error: "
                            + e.getMessage()
            );

        } finally {

            LOGGER.info(workerName + " completed.");
        }
    }

    private void processTask(Task task, String workerName)
            throws InterruptedException {

        LOGGER.info(
                workerName + " processing "
                        + task
        );

        // Simulate computational work.
        int processingTime =
                ThreadLocalRandom.current().nextInt(100, 301);

        Thread.sleep(processingTime);

        String result =
                task + " processed by " + workerName;

        // synchronizedList provides thread-safe access.
        results.add(result);

        LOGGER.info(
                workerName + " completed "
                        + task
        );
    }
}
