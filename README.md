# Smart Task Manager

Command Line Interface (CLI) application that acts as a Smart Task Manager. 
Its distinctive feature is the **Priority Engine**, which mathematically calculates task priority rather than relying on manual input.

## Features
- **Priority Engine**: Automatically calculates a priority score based on:
  - `Importance` (1-5 baseline multiplier)
  - `Urgency` (Points based on how close the deadline is)
  - `Penalty` (Extra points added if the task is overdue)
- **JSON Storage**: Uses Jackson to save/load tasks to `data/tasks.json`.
- **Intelligent Views**:
  - `Show All`: Automatically sorted with highest priority first.
  - `Urgent`: Shows tasks very close to their deadlines.
  - `Done`: Shows completed tasks (automatically down-ranked in priority view).
  - `Late`: Shows tasks that missed their deadlines.

## Architecture Structure (MVC)
```
smart-task-manager/
├── src/
│   ├── main/java/com/taskmanager/
│   │   ├── model/         # Task entity and enums
│   │   ├── repository/    # JsonTaskRepository (data access layer)
│   │   ├── service/       # TaskService (business logic, priority engine)
│   │   └── controller/    # TaskController (CLI interaction)
├── data/                  # Output directory for tasks.json
├── pom.xml                # Maven configuration using Java 17+
```

## How Priority works
`Priority = (Importance * 2) + Urgency + Penalty`
- **Importance**: User input from 1 to 5.
- **Urgency**: Added dynamically when deadline approaches (e.g., +10 if due today, +8 tomorrow).
- **Penalty**: If overdue, flat +10 penalty +2 points for every extra day late to force attention.
- **Done tasks**: Given a massive negative priority to stay at the bottom of the list.

## Usage
Compile and pack the project:
```bash
mvn clean package
```

Run tests to ensure priority engine stability:
```bash
mvn test
```

Execute the CLI:
```bash
mvn exec:java -Dexec.mainClass="com.taskmanager.Main"
```
