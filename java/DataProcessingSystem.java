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

                        // Create and submit worker threads.
                        for (int workerId = 1; workerId <= NUMBER_OF_WORKERS; workerId++) {

                                executor.submit(
                                                new Worker(
                                                                workerId,
                                                                taskQueue,
                                                                results));
                        }

                        // Dynamically create tasks.
                        for (int taskId = 1; taskId <= NUMBER_OF_TASKS; taskId++) {

                                Task task = new Task(
                                                taskId,
                                                "Data-" + taskId);

                                taskQueue.addTask(task);

                                LOGGER.info(
                                                "Added " + task + " to shared queue.");
                        }

                        /*
                         * Add one shutdown signal for each worker.
                         * Workers will stop after all previously queued
                         * tasks have been processed.
                         */
                        for (int i = 0; i < NUMBER_OF_WORKERS; i++) {

                                taskQueue.addTask(
                                                Task.createPoisonPill());
                        }

                        LOGGER.info(
                                        "All tasks and shutdown signals have "
                                                        + "been added to the queue.");

                        /*
                         * No more tasks will be submitted, so the executor
                         * can begin its normal shutdown process.
                         */
                        executor.shutdown();

                        boolean completed = executor.awaitTermination(
                                        30,
                                        TimeUnit.SECONDS);

                        if (!completed) {

                                LOGGER.warning(
                                                "Workers did not finish within "
                                                                + "the expected time.");

                                executor.shutdownNow();
                        }

                } catch (InterruptedException e) {

                        LOGGER.warning(
                                        "Main thread was interrupted while "
                                                        + "waiting for workers.");

                        executor.shutdownNow();

                        Thread.currentThread().interrupt();

                } finally {

                        if (!executor.isTerminated()) {
                                executor.shutdownNow();
                        }
                }

                // Write all results after workers have completed.
                writeResults(results);

                // Validate the expected number of results.
                validateResults(results);

                LOGGER.info(
                                "Data Processing System completed successfully.");
        }

        private static void writeResults(List<String> results) {

                Path outputPath = Paths.get(RESULTS_FILE);

                try (BufferedWriter writer = Files.newBufferedWriter(
                                outputPath,
                                StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING,
                                StandardOpenOption.WRITE)) {

                        writer.write("Data Processing Results");
                        writer.newLine();
                        writer.write("=======================");
                        writer.newLine();
                        writer.newLine();

                        synchronized (results) {

                                for (String result : results) {
                                        writer.write(result);
                                        writer.newLine();
                                }
                        }

                        LOGGER.info(
                                        "Results successfully saved to "
                                                        + outputPath.toAbsolutePath());

                } catch (IOException e) {

                        LOGGER.severe(
                                        "Unable to write results file: "
                                                        + e.getMessage());
                }
        }

        private static void validateResults(
                        List<String> results) {

                if (results.size() != NUMBER_OF_TASKS) {

                        LOGGER.warning(
                                        "Validation warning: expected "
                                                        + NUMBER_OF_TASKS
                                                        + " results but received "
                                                        + results.size());

                } else {

                        LOGGER.info(
                                        "Validation successful: all "
                                                        + NUMBER_OF_TASKS
                                                        + " tasks produced results.");
                }
        }
}
