package com.taskmanager.controller;

import com.taskmanager.model.Task;
import com.taskmanager.service.TaskService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class TaskController {
    private final TaskService service;
    private final Scanner scanner;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TaskController(TaskService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean exit = false;
        while (!exit) {
            printMenu();
            String choice = scanner.nextLine();
            
            try {
                switch (choice) {
                    case "1" -> addTask();
                    case "2" -> deleteTask();
                    case "3" -> markAsDone();
                    case "4" -> showTasks(service.getTasksSortedByPriority(), "All Tasks (Sorted by Priority)");
                    case "5" -> showTasks(service.getUrgentTasks(), "Urgent Tasks");
                    case "6" -> showTasks(service.getDoneTasks(), "Done Tasks");
                    case "7" -> showTasks(service.getLateTasks(), "Late Tasks");
                    case "0" -> exit = true;
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
        System.out.println("Goodbye!");
    }

    private void printMenu() {
        System.out.println("\n=== Smart Task Manager ===");
        System.out.println("1. Add Task");
        System.out.println("2. Delete Task");
        System.out.println("3. Mark Task as Done");
        System.out.println("4. Show All Tasks (Smart Sort)");
        System.out.println("5. Show Urgent Tasks");
        System.out.println("6. Show Done Tasks");
        System.out.println("7. Show Late Tasks");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private void addTask() {
        System.out.print("Title: ");
        String title = scanner.nextLine();
        
        System.out.print("Description: ");
        String description = scanner.nextLine();
        
        System.out.print("Deadline (yyyy-MM-dd HH:mm) or empty for none: ");
        String deadlineStr = scanner.nextLine();
        LocalDateTime deadline = null;
        if (!deadlineStr.trim().isEmpty()) {
            try {
                deadline = LocalDateTime.parse(deadlineStr, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Task will have no deadline.");
            }
        }

        System.out.print("Importance (1-5): ");
        int importance = 3;
        try {
            importance = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input, defaulting to 3.");
        }

        service.addTask(title, description, deadline, importance);
        System.out.println("Task added successfully!");
    }

    private void deleteTask() {
        System.out.print("Enter Task ID to delete: ");
        String id = scanner.nextLine();
        service.deleteTask(id);
        System.out.println("Task deleted (if existed).");
    }

    private void markAsDone() {
        System.out.print("Enter Task ID to mark as done: ");
        String id = scanner.nextLine();
        service.markAsDone(id);
        System.out.println("Task marked as done (if existed).");
    }

    private void showTasks(List<Task> tasks, String listTitle) {
        System.out.println("\n--- " + listTitle + " ---");
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
        for (Task task : tasks) {
            System.out.printf("[%s] %s priority:%d importance:%d status:%s deadline:%s%n",
                    task.getId(),
                    task.getTitle(),
                    task.getPriority(),
                    task.getImportance(),
                    task.getStatus(),
                    task.getDeadline() != null ? task.getDeadline().format(formatter) : "None");
        }
        System.out.println("--------------------");
    }
}
