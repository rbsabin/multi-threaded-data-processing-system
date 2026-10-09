import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataProcessingSystem {

    private static final Logger LOGGER = Logger.getLogger(DataProcessingSystem.class.getName());

    private static final int NUMBER_OF_WORKERS = 3;
    private static final int NUMBER_OF_TASKS = 12;
    private static final String RESULTS_FILE = "results.txt";

    public static void main(String[] args) {
        LOGGER.info("Starting Data Processing System.");

        SharedTaskQueue taskQueue = new SharedTaskQueue();
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_WORKERS);

        try {
            for (int workerId = 1; workerId <= NUMBER_OF_WORKERS; workerId++) {
                executor.submit(new Worker(workerId, taskQueue, results));
            }

            for (int taskId = 1; taskId <= NUMBER_OF_TASKS; taskId++) {
                Task task = new Task(taskId, "Data-" + taskId);
                taskQueue.addTask(task);
                LOGGER.info("Added " + task + " to shared queue.");
            }

            // Enqueue poison pills to signal workers to terminate
            for (int i = 0; i < NUMBER_OF_WORKERS; i++) {
                taskQueue.addTask(Task.createPoisonPill());
            }

            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                LOGGER.warning("Workers did not finish within timeout; forcing shutdown.");
                executor.shutdownNow();
            }

        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Execution interrupted", e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        }

        boolean writeSuccess = writeResults(results);
        boolean validateSuccess = validateResults(results);

        if (writeSuccess && validateSuccess) {
            LOGGER.info("Data Processing System completed successfully.");
        } else {
            LOGGER.warning("Data Processing System finished with errors or validation warnings.");
        }
    }

    private static boolean writeResults(List<String> results) {
        Path outputPath = Paths.get(RESULTS_FILE);

        try (BufferedWriter writer = Files.newBufferedWriter(
                outputPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {

            writer.write("Data Processing Results\n");
            writer.write("=======================\n\n");

            for (String result : results) {
                writer.write(result);
                writer.newLine();
            }

            LOGGER.info("Results successfully saved to " + outputPath.toAbsolutePath());
            return true;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to write results file to " + outputPath, e);
            return false;
        }
    }

    private static boolean validateResults(List<String> results) {
        if (results.size() != NUMBER_OF_TASKS) {
            LOGGER.warning("Validation mismatch: expected " + NUMBER_OF_TASKS + " results, got " + results.size());
            return false;
        }

        LOGGER.info("Validation successful: all " + NUMBER_OF_TASKS + " tasks produced results.");
        return true;
    }
}
