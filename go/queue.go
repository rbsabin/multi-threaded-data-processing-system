package main

type SharedTaskQueue struct {
	tasks chan Task
}

func NewSharedTaskQueue(capacity int) *SharedTaskQueue {
	return &SharedTaskQueue{
		tasks: make(chan Task, capacity),
	}
}

func (q *SharedTaskQueue) addTask(task Task) {
	q.tasks <- task
}

func (q *SharedTaskQueue) getTask() (Task, bool) {
	task, ok := <-q.tasks
	return task, ok
}

func (q *SharedTaskQueue) close() {
	close(q.tasks)
}
