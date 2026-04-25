package com.taskmanager.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskmanager.model.Task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonTaskRepository implements TaskRepository {
    private final File dataFile;
    private final ObjectMapper mapper;
    private List<Task> tasks;

    public JsonTaskRepository(String filePath) {
        this.dataFile = new File(filePath);
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        
        // Ensure data directory exists
        this.dataFile.getParentFile().mkdirs();
        
        this.tasks = loadTasks();
    }

    private List<Task> loadTasks() {
        if (!dataFile.exists()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(dataFile, new TypeReference<List<Task>>() {});
        } catch (IOException e) {
            System.err.println("Error reading tasks from file: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveTasks() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, tasks);
        } catch (IOException e) {
            System.err.println("Error saving tasks to file: " + e.getMessage());
        }
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(tasks);
    }

    @Override
    public Optional<Task> findById(String id) {
        return tasks.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    @Override
    public void save(Task task) {
        tasks.add(task);
        saveTasks();
    }

    @Override
    public void delete(String id) {
        tasks.removeIf(t -> t.getId().equals(id));
        saveTasks();
    }

    @Override
    public void update(Task task) {
        for (int i = 0; i < tasks.size(); i++) {
            Task existingTask = tasks.get(i);
            if (existingTask.getId().equals(task.getId())) {
                if (existingTask.getVersion() != task.getVersion()) {
                    throw new com.taskmanager.exception.OptimisticLockException("Version mismatch for task " + task.getId());
                }
                task.setVersion(task.getVersion() + 1);
                tasks.set(i, task);
                saveTasks();
                return;
            }
        }
        throw new com.taskmanager.exception.TaskNotFoundException("Task not found: " + task.getId());
    }
}
