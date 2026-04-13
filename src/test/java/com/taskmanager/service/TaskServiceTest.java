package com.taskmanager.service;

import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {

    private TaskService taskService;
    private MockRepository mockRepository;

    @BeforeEach
    void setUp() {
        mockRepository = new MockRepository();
        taskService = new TaskService(mockRepository);
    }

    @Test
    void testPriorityCalculation() {
        taskService.addTask("Urgent & Important", "Desc", LocalDateTime.now().plusHours(2), 5); // Importance 5, Urgency 10
        taskService.addTask("Important only", "Desc", LocalDateTime.now().plusDays(10), 5); // Importance 5, Urgency 0
        taskService.addTask("Late task", "Desc", LocalDateTime.now().minusDays(2), 1); // Importance 1, Late penalty 14
        
        List<Task> sortedTasks = taskService.getTasksSortedByPriority();
        assertEquals(3, sortedTasks.size());
        
        // "Urgent & Important" priority = 5*2 + 10 = 20
        // "Important only" priority = 5*2 + 0 = 10
        // "Late task" priority = 1*2 + (10 + 2*2) = 2 + 14 = 16
        
        assertEquals("Urgent & Important", sortedTasks.get(0).getTitle());
        assertEquals("Late task", sortedTasks.get(1).getTitle());
        assertEquals("Important only", sortedTasks.get(2).getTitle());
    }

    @Test
    void testDoneTasksHaveLowPriority() {
        taskService.addTask("Task 1", "Desc", LocalDateTime.now().plusHours(2), 5);
        List<Task> tasks = mockRepository.findAll();
        taskService.markAsDone(tasks.get(0).getId());

        List<Task> sortedTasks = taskService.getTasksSortedByPriority();
        assertEquals(1, sortedTasks.size());
        assertTrue(sortedTasks.get(0).getPriority() < 0);
    }

    // Mock implementation for fast tests without JSON files
    static class MockRepository implements TaskRepository {
        private final List<Task> tasks = new ArrayList<>();

        @Override
        public List<Task> findAll() { return tasks; }

        @Override
        public Optional<Task> findById(String id) { return tasks.stream().filter(t -> t.getId().equals(id)).findFirst(); }

        @Override
        public void save(Task task) { tasks.add(task); }

        @Override
        public void delete(String id) { tasks.removeIf(t -> t.getId().equals(id)); }

        @Override
        public void update(Task task) {
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).getId().equals(task.getId())) {
                    tasks.set(i, task);
                    return;
                }
            }
        }
    }
}
