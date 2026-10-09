package main

import (
	"bufio"
	"fmt"
	"log"
	"os"
	"sync"
)

const (
	numberOfWorkers = 3
	numberOfTasks   = 12
	resultsFile     = "results.txt"
)

func main() {
	logger := log.New(os.Stdout, "", log.Ldate|log.Ltime)

	if err := run(logger); err != nil {
		logger.Printf("ERROR: %v", err)
		os.Exit(1)
	}
}

func run(logger *log.Logger) error {
	logger.Println("Starting Data Processing System.")

	queue := NewSharedTaskQueue(numberOfTasks)
	results := make(chan ProcessingResult)

	var wg sync.WaitGroup
	wg.Add(numberOfWorkers)

	for workerID := 1; workerID <= numberOfWorkers; workerID++ {
		go worker(workerID, queue, results, &wg, logger)
	}

	processedResults := make([]ProcessingResult, 0, numberOfTasks)
	collectorDone := make(chan struct{})

	go func() {
		defer close(collectorDone)
		for result := range results {
			processedResults = append(processedResults, result)
		}
	}()

	for taskID := 1; taskID <= numberOfTasks; taskID++ {
		task := Task{
			ID:   taskID,
			Data: fmt.Sprintf("Data-%d", taskID),
		}
		queue.addTask(task)
		logger.Printf("Added %s to shared queue.", task)
	}

	queue.close()
	logger.Println("All tasks have been added; task queue closed.")

	wg.Wait()
	close(results)
	<-collectorDone

	if err := validateResults(processedResults, numberOfTasks, logger); err != nil {
		return err
	}

	if err := writeResults(processedResults, resultsFile); err != nil {
		return err
	}

	logger.Printf("Results successfully saved to %s.", resultsFile)
	logger.Println("Data Processing System completed successfully.")

	return nil
}

func validateResults(results []ProcessingResult, expected int, logger *log.Logger) error {
	if len(results) != expected {
		return fmt.Errorf("validation failed: expected %d results, received %d", expected, len(results))
	}

	seen := make(map[int]bool)
	for _, result := range results {
		taskID := result.Task.ID
		if seen[taskID] {
			return fmt.Errorf("validation failed: duplicate result for Task-%d", taskID)
		}
		seen[taskID] = true
	}

	logger.Printf("Validation successful: all %d tasks produced exactly one result.", expected)
	return nil
}

func writeResults(results []ProcessingResult, fileName string) error {
	file, err := os.Create(fileName)
	if err != nil {
		return fmt.Errorf("unable to create results file: %v", err)
	}
	defer file.Close()

	writer := bufio.NewWriter(file)
	if _, err = fmt.Fprintln(writer, "Data Processing Results"); err != nil {
		return err
	}
	if _, err = fmt.Fprintln(writer, "=======================\n"); err != nil {
		return err
	}

	for _, result := range results {
		if _, err = fmt.Fprintln(writer, result); err != nil {
			return err
		}
	}

	return writer.Flush()
}
