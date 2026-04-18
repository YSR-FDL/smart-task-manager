package com.taskmanager.controller;

import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.service.TaskService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TaskController {
    private final TaskService service;
    private final Scanner scanner;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // Store the last displayed list to map indices
    private List<Task> lastDisplayedTasks = new ArrayList<>();

    // ANSI Color Codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";

    public TaskController(TaskService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean exit = false;
        while (!exit) {
            printMenu();
            String choice = scanner.nextLine().trim();
            
            try {
                switch (choice) {
                    case "1" -> addTask();
                    case "2" -> deleteTask();
                    case "3" -> markAsDone();
                    case "4" -> showTasks(service.getTasksSortedByPriority(), "All Tasks (Sorted by Priority)");
                    case "5" -> showTasks(service.getUrgentTasks(), "Urgent Tasks");
                    case "6" -> showTasks(service.getDoneTasks(), "Done Tasks");
                    case "7" -> showTasks(service.getLateTasks(), "Late Tasks");
                    case "8" -> editTask();
                    case "0" -> exit = true;
                    default -> System.out.println(RED + "Invalid option. Please try again." + RESET);
                }
            } catch (TaskNotFoundException e) {
                System.out.println(RED + "Error: " + e.getMessage() + RESET);
            } catch (Exception e) {
                System.out.println(RED + "An unexpected error occurred: " + e.getMessage() + RESET);
            }
        }
        System.out.println(GREEN + "Goodbye!" + RESET);
    }

    private void printMenu() {
        System.out.println("\n" + BLUE + "=== Smart Task Manager ===" + RESET);
        System.out.println("1. Add Task");
        System.out.println("2. Delete Task");
        System.out.println("3. Mark Task as Done");
        System.out.println("4. Show All Tasks (Smart Sort)");
        System.out.println("5. Show Urgent Tasks");
        System.out.println("6. Show Done Tasks");
        System.out.println("7. Show Late Tasks");
        System.out.println("8. Edit Task");
        System.out.println("0. Exit");
        System.out.print(CYAN + "Choose an option: " + RESET);
    }

    private void addTask() {
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        
        LocalDateTime deadline = promptForDeadline("Deadline (yyyy-MM-dd HH:mm) or empty for none: ");

        int importance = promptForInt("Importance (1-5): ", 3, 1, 5);

        service.addTask(title, description, deadline, importance);
        System.out.println(GREEN + "Task added successfully!" + RESET);
    }

    private void deleteTask() {
        Task task = promptForTaskSelection("Enter Task # to delete: ");
        if (task != null) {
            service.deleteTask(task.getId());
            System.out.println(GREEN + "Task deleted successfully." + RESET);
        }
    }

    private void markAsDone() {
        Task task = promptForTaskSelection("Enter Task # to mark as done: ");
        if (task != null) {
            service.markAsDone(task.getId());
            System.out.println(GREEN + "Task marked as done." + RESET);
        }
    }
    
    private void editTask() {
        Task task = promptForTaskSelection("Enter Task # to edit: ");
        if (task == null) return;

        System.out.print("New Title (leave empty to keep '" + task.getTitle() + "'): ");
        String title = scanner.nextLine().trim();
        if (!title.isEmpty()) task.setTitle(title);

        System.out.print("New Description (leave empty to keep current): ");
        String description = scanner.nextLine().trim();
        if (!description.isEmpty()) task.setDescription(description);

        String currentDeadline = task.getDeadline() != null ? task.getDeadline().format(formatter) : "None";
        System.out.print("New Deadline (yyyy-MM-dd HH:mm) or 'clear' to remove, enter to keep (" + currentDeadline + "): ");
        String deadlineStr = scanner.nextLine().trim();
        
        if (deadlineStr.equalsIgnoreCase("clear")) {
            task.setDeadline(null);
        } else if (!deadlineStr.isEmpty()) {
            try {
                task.setDeadline(LocalDateTime.parse(deadlineStr, formatter));
            } catch (DateTimeParseException e) {
                System.out.println(RED + "Invalid date format. Deadline unchanged." + RESET);
            }
        }
        
        System.out.print("New Importance (1-5, leave empty to keep " + task.getImportance() + "): ");
        String impStr = scanner.nextLine().trim();
        if (!impStr.isEmpty()) {
            try {
                int importance = Integer.parseInt(impStr);
                if (importance >= 1 && importance <= 5) {
                    task.setImportance(importance);
                } else {
                    System.out.println(RED + "Importance must be between 1 and 5. Unchanged." + RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(RED + "Invalid number format. Importance unchanged." + RESET);
            }
        }

        service.updateTask(task);
        System.out.println(GREEN + "Task updated successfully!" + RESET);
    }

    private Task promptForTaskSelection(String promptMessage) {
        if (lastDisplayedTasks.isEmpty()) {
            System.out.println(YELLOW + "Please use one of the 'Show' options first to see the task numbers." + RESET);
            return null;
        }

        System.out.print(promptMessage);
        String input = scanner.nextLine().trim();
        
        try {
            int index = Integer.parseInt(input) - 1;
            if (index >= 0 && index < lastDisplayedTasks.size()) {
                return lastDisplayedTasks.get(index);
            } else {
                System.out.println(RED + "Invalid task number. Please enter a number between 1 and " + lastDisplayedTasks.size() + "." + RESET);
            }
        } catch (NumberFormatException e) {
            System.out.println(RED + "Please enter a valid number." + RESET);
        }
        return null;
    }

    private LocalDateTime promptForDeadline(String prompt) {
        System.out.print(prompt);
        String deadlineStr = scanner.nextLine().trim();
        if (!deadlineStr.isEmpty()) {
            try {
                return LocalDateTime.parse(deadlineStr, formatter);
            } catch (DateTimeParseException e) {
                System.out.println(RED + "Invalid date format. Task will have no deadline." + RESET);
            }
        }
        return null;
    }
    
    private int promptForInt(String prompt, int defaultValue, int min, int max) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return defaultValue;
        
        try {
            int val = Integer.parseInt(input);
            if (val >= min && val <= max) return val;
            System.out.println(YELLOW + "Value out of range (" + min + "-" + max + "), defaulting to " + defaultValue + "." + RESET);
        } catch (NumberFormatException e) {
            System.out.println(YELLOW + "Invalid input, defaulting to " + defaultValue + "." + RESET);
        }
        return defaultValue;
    }

    private void showTasks(List<Task> tasks, String listTitle) {
        System.out.println("\n" + BLUE + "--- " + listTitle + " ---" + RESET);
        
        lastDisplayedTasks = new ArrayList<>(tasks); // Update mapping
        
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
        
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            String deadlineStr = task.getDeadline() != null ? task.getDeadline().format(formatter) : "None";
            
            // Color logic based on status and urgency
            String color = RESET;
            if (task.getStatus() == TaskStatus.DONE) {
                color = GREEN;
            } else if (task.isLate()) {
                color = RED;
            } else if (task.getPriority() > 10) {
                color = YELLOW;
            }
            
            System.out.printf(color + "%d. [%s] priority:%d importance:%d status:%s deadline:%s" + RESET + "%n",
                    (i + 1),
                    task.getTitle(),
                    task.getPriority(),
                    task.getImportance(),
                    task.getStatus(),
                    deadlineStr);
        }
        System.out.println(BLUE + "--------------------" + RESET);
    }
}
