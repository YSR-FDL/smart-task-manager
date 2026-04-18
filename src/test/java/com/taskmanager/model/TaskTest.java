package com.taskmanager.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void testPriorityCalculationDefault() {
        Task task = new Task("Test", "Desc", null, 3);
        // importance 3 * 2 = 6. No deadline, no penalty, no urgency.
        assertEquals(6, task.getPriority());
    }

    @Test
    void testPriorityWithUrgency() {
        // Due tomorrow (use 48 hours to reliably get 1 day left due to truncation)
        Task task = new Task("Test", "Desc", LocalDateTime.now().plusHours(48), 3);
        // importance 3 * 2 = 6. Urgency for tomorrow = 8.
        assertEquals(14, task.getPriority());
    }

    @Test
    void testPriorityWithPenaltyLate() {
        // Late by 2 days
        Task task = new Task("Test", "Desc", LocalDateTime.now().minusDays(2), 3);
        // importance 3 * 2 = 6. Late penalty = 10 + (2 * 2) = 14
        assertEquals(20, task.getPriority());
    }

    @Test
    void testPriorityWhenDone() {
        Task task = new Task("Test", "Desc", LocalDateTime.now().minusDays(2), 5);
        task.setStatus(TaskStatus.DONE);
        // Completed tasks should have a massive negative priority to sink to the bottom.
        assertEquals(-1000, task.getPriority());
    }

    @Test
    void testImportanceBounds() {
        Task lowTask = new Task("Low", "Desc", null, -5);
        assertEquals(1, lowTask.getImportance());
        
        Task highTask = new Task("High", "Desc", null, 10);
        assertEquals(5, highTask.getImportance());
    }
}
