package com.taskmanager.repository;

import com.taskmanager.exception.OptimisticLockException;
import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JdbcTaskRepositoryTest {

    private static final String TEST_DB_URL = "jdbc:sqlite:file:testdb?mode=memory&cache=shared";
    private JdbcTaskRepository repository;
    private Connection keepAliveConnection;

    @BeforeEach
    void setUp() throws SQLException {
        keepAliveConnection = DriverManager.getConnection(TEST_DB_URL);
        repository = new JdbcTaskRepository(TEST_DB_URL, null, null);
    }
    @AfterEach

    void tearDown() throws SQLException {
        if (keepAliveConnection != null && !keepAliveConnection.isClosed()) {
            keepAliveConnection.close();
        }
    }
    @Test
    void testSaveAndFindById() {
        Task task = new Task("Test Title", "Test Desc", LocalDateTime.now(), 3);
        repository.save(task);

        Optional<Task> found = repository.findById(task.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Title", found.get().getTitle());
        assertEquals(1, found.get().getVersion());
    }

    @Test
    void testFindAll() {
        repository.save(new Task("Task 1", "Desc 1", null, 1));
        repository.save(new Task("Task 2", "Desc 2", null, 2));

        List<Task> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testDelete() {
        Task task = new Task("To delete", "Desc", null, 1);
        repository.save(task);
        assertTrue(repository.findById(task.getId()).isPresent());

        repository.delete(task.getId());
        assertFalse(repository.findById(task.getId()).isPresent());
    }

    @Test
    void testUpdateSuccess() {
        Task task = new Task("Original Title", "Desc", null, 3);
        repository.save(task);

        task.setTitle("Updated Title");
        repository.update(task);

        Task updated = repository.findById(task.getId()).get();
        assertEquals("Updated Title", updated.getTitle());
        assertEquals(2, updated.getVersion());
        assertEquals(2, task.getVersion());
    }

    @Test
    void testUpdateOptimisticLocking() {
        Task task = new Task("Original", "Desc", null, 3);
        repository.save(task);

        Task taskInstance1 = repository.findById(task.getId()).get();
        Task taskInstance2 = repository.findById(task.getId()).get();

        taskInstance1.setTitle("Update 1");
        repository.update(taskInstance1);

        taskInstance2.setTitle("Update 2");
        assertThrows(OptimisticLockException.class, () -> repository.update(taskInstance2));
    }

    @Test
    void testUpdateNotFound() {
        Task task = new Task("Never Saved", "Desc", null, 3);
        // task.getId() exists but not in DB
        assertThrows(TaskNotFoundException.class, () -> repository.update(task));
    }
}
