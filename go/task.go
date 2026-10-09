package main

import "fmt"

type Task struct {
	ID   int
	Data string
}

func (t Task) String() string {
	return fmt.Sprintf("Task-%d [%s]", t.ID, t.Data)
}

type ProcessingResult struct {
	Task     Task
	WorkerID int
}

func (r ProcessingResult) String() string {
	return fmt.Sprintf("%s processed by Worker-%d", r.Task, r.WorkerID)
}
