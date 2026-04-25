package com.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.UUID;

public class Task {
    private String id;
    private String title;
    private String description;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime deadline;
    
    private int importance; // 1 to 5
    private TaskStatus status;
    private int version = 1;

    public Task() {
        // default constructor for Jackson
    }

    public Task(String title, String description, LocalDateTime deadline, int importance) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.importance = Math.max(1, Math.min(5, importance));
        this.status = TaskStatus.PENDING;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public int getImportance() { return importance; }
    public void setImportance(int importance) { this.importance = Math.max(1, Math.min(5, importance)); }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    @JsonIgnore
    public int getPriority() {
        if (status == TaskStatus.DONE) {
            return -1000; // Done tasks have the lowest priority
        }

        int score = importance * 2;
        int urgency = 0;
        int penalty = 0;

        if (deadline != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(deadline)) {
                // Task is late
                Duration delay = Duration.between(deadline, now);
                long daysLate = delay.toDays();
                penalty = 10 + (int) daysLate * 2; // Flat penalty of 10 + 2 points per day late
            } else {
                // Task is pending
                Duration remaining = Duration.between(now, deadline);
                long daysLeft = remaining.toDays();
                // Urgency increases as deadline approaches
                if (daysLeft <= 0) urgency = 10;      // Due today
                else if (daysLeft <= 1) urgency = 8;  // Due tomorrow
                else if (daysLeft <= 3) urgency = 5;  // Due in <= 3 days
                else if (daysLeft <= 7) urgency = 2;  // Due in a week
                else urgency = 0;                     // Plenty of time
            }
        }

        return score + urgency + penalty;
    }

    @JsonIgnore
    public boolean isLate() {
        return status != TaskStatus.DONE && deadline != null && LocalDateTime.now().isAfter(deadline);
    }
}
