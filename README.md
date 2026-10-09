# Multi-Threaded Data Processing System

## Overview
This project implements a concurrent data processing system designed to demonstrate multi-threading, producer-consumer patterns, synchronization, and graceful shutdown mechanics. It provides parallel implementations in **Java** and **Go** to compare different concurrency models:
- **Java**: Shared-memory concurrency using thread pools (`ExecutorService`), blocking queues, and the poison pill shutdown pattern.
- **Go**: Communicating Sequential Processes (CSP) using goroutines, channels, and `sync.WaitGroup`.

---

## Objectives
- **Concurrent Task Execution**: Distribute computational tasks dynamically across a fixed pool of worker threads/goroutines (3 workers processing 12 tasks).
- **Thread Safety**: Ensure shared resources (task queue, results list) are accessed concurrently without race conditions or deadlocks.
- **Graceful Termination**: Guarantee that all enqueued tasks complete before the system terminates, leaving no zombie or orphaned threads.
- **Resilience & Fault Isolation**: Handle thread interruptions and unexpected task-level exceptions without crashing the worker pool.
- **Result Verification**: Persist output to `results.txt` and validate that every task was processed exactly once.

---

## Architecture

### 1. Java Implementation (`java/`)
- `Task.java`: Represents a discrete unit of work (`Task-ID [Data-ID]`) and defines a `POISON_PILL` sentinel to signal thread termination.
- `SharedTaskQueue.java`: Encapsulates a thread-safe `LinkedBlockingQueue` with blocking `take()` and `put()` operations.
- `Worker.java`: A `Runnable` that continuously consumes tasks from the queue, simulates workload (100–300 ms), isolates task exceptions, and appends results to a thread-safe collection.
- `DataProcessingSystem.java`: The main orchestrator that initializes the thread pool (`Executors.newFixedThreadPool(3)`), enqueues tasks and poison pills, awaits clean termination, writes results to `results.txt`, and validates result counts.

### 2. Go Implementation (`go/`)
- `task.go`: Defines `Task` and `ProcessingResult` structs with string formatters.
- `queue.go`: Wraps a buffered channel for queueing tasks and cleanly closing when input is exhausted.
- `worker.go`: Goroutines consuming tasks until the channel is drained and closed, using `time.Sleep` to simulate processing delay.
- `main.go`: Spawns worker goroutines with a `sync.WaitGroup`, streams results to an asynchronous collector goroutine, validates uniqueness, and writes the output file.

---

## How to Compile and Run

### Java

#### From the `java/` directory
```bash
cd java
javac *.java
java DataProcessingSystem
```

Output is saved to `results.txt` (or `java/results.txt` if executed from within the directory).

---

### Go

Ensure Go 1.18+ is installed.


#### From the `go/` directory
```bash
cd go
go run .
```

Output is saved to `results.txt` (or `go/results.txt` if executed from within the directory).