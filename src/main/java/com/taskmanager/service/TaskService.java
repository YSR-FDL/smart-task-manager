package com.taskmanager.service;

import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TaskService {
    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public void addTask(String title, String description, LocalDateTime deadline, int importance) {
        Task task = new Task(title, description, deadline, importance);
        repository.save(task);
    }

    public void deleteTask(String id) {
        repository.delete(id);
    }

    public void updateTask(Task task) {
        repository.update(task);
    }

    public Task getTask(String id) {
        Optional<Task> task = repository.findById(id);
        return task.orElse(null);
    }

    public void markAsDone(String id) {
        repository.findById(id).ifPresent(task -> {
            task.setStatus(TaskStatus.DONE);
            repository.update(task);
        });
    }

    public List<Task> getTasksSortedByPriority() {
        return repository.findAll().stream()
                .sorted(Comparator.comparingInt(Task::getPriority).reversed())
                .collect(Collectors.toList());
    }

    public List<Task> getUrgentTasks() {
        return getTasksSortedByPriority().stream()
                .filter(t -> t.getStatus() != TaskStatus.DONE && t.getPriority() > 10) // Threshold for urgency
                .collect(Collectors.toList());
    }

    public List<Task> getDoneTasks() {
        return repository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .collect(Collectors.toList());
    }

    public List<Task> getLateTasks() {
        return repository.findAll().stream()
                .filter(Task::isLate)
                .collect(Collectors.toList());
    }
}
