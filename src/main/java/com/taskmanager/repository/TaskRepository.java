package com.taskmanager.repository;

import com.taskmanager.model.Task;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    List<Task> findAll();
    Optional<Task> findById(String id);
    void save(Task task);
    void delete(String id);
    void update(Task task);
}
