package com.taskmanager;

import com.taskmanager.controller.TaskController;
import com.taskmanager.repository.JdbcTaskRepository;
import com.taskmanager.repository.JsonTaskRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.service.TaskService;

public class Main {
    public static void main(String[] args) {
        String dbUrl = "jdbc:sqlite:tasks.db";
        TaskRepository repository = new JdbcTaskRepository(dbUrl, null, null);
        TaskService service = new TaskService(repository);
        TaskController controller = new TaskController(service);
        
        System.out.println("Starting Smart Task Manager Engine...");
        controller.start();
    }
}
