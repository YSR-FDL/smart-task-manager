package com.taskmanager.service;

import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.model.Task;
import com.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {

    private TaskService service;
    private MockTaskRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MockTaskRepository();
        service = new TaskService(repository);
    }

    @Test
    void testAddTask() {
        service.addTask("Test Task", "Description", null, 3);
        assertEquals(1, repository.findAll().size());
        assertEquals("Test Task", repository.findAll().get(0).getTitle());
    }

    @Test
    void testGetTaskThrowsExceptionIfNotFound() {
        assertThrows(TaskNotFoundException.class, () -> service.getTask("invalid-id"));
    }

    @Test
    void testDeleteThrowsExceptionIfNotFound() {
        assertThrows(TaskNotFoundException.class, () -> service.deleteTask("invalid-id"));
    }

    @Test
    void testMarkAsDone() {
        service.addTask("Done Task", "Desc", null, 3);
        String id = repository.findAll().get(0).getId();
        service.markAsDone(id);
        
        assertEquals(com.taskmanager.model.TaskStatus.DONE, service.getTask(id).getStatus());
    }

    @Test
    void testGetTasksSortedByPriority() {
        service.addTask("Low Priority", "Desc", LocalDateTime.now().plusDays(10), 1);
        service.addTask("High Priority", "Desc", null, 5);
        service.addTask("Medium Priority", "Desc", null, 3);

        List<Task> sorted = service.getTasksSortedByPriority();
        assertEquals("High Priority", sorted.get(0).getTitle());
        assertEquals("Medium Priority", sorted.get(1).getTitle());
        assertEquals("Low Priority", sorted.get(2).getTitle());
    }

    // A simple mock repository to avoid writing to JSON files during testing
    static class MockTaskRepository implements TaskRepository {
        private final List<Task> tasks = new ArrayList<>();

        @Override
        public List<Task> findAll() { return new ArrayList<>(tasks); }

        @Override
        public Optional<Task> findById(String id) {
            return tasks.stream().filter(t -> t.getId().equals(id)).findFirst();
        }

        @Override
        public void save(Task task) { tasks.add(task); }

        @Override
        public void delete(String id) { tasks.removeIf(t -> t.getId().equals(id)); }

        @Override
        public void update(Task task) {
            delete(task.getId());
            save(task);
        }
    }
}
