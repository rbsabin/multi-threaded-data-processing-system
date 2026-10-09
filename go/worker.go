package main

import (
	"fmt"
	"log"
	"sync"
	"time"
)

func worker(
	workerID int,
	queue *SharedTaskQueue,
	results chan<- ProcessingResult,
	wg *sync.WaitGroup,
	logger *log.Logger,
) {
	defer wg.Done()

	workerName := fmt.Sprintf("Worker-%d", workerID)
	logger.Printf("%s started.", workerName)
	defer logger.Printf("%s completed.", workerName)

	for {
		task, ok := queue.getTask()
		if !ok {
			logger.Printf("%s received queue shutdown signal.", workerName)
			return
		}

		logger.Printf("%s processing %s.", workerName, task)

		delay := time.Duration(100+((task.ID+workerID)%3)*100) * time.Millisecond
		time.Sleep(delay)

		results <- ProcessingResult{
			Task:     task,
			WorkerID: workerID,
		}

		logger.Printf("%s completed %s.", workerName, task)
	}
}
