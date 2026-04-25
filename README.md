# Smart Task Manager

A Command Line Interface (CLI) application that acts as a Smart Task Manager.
Its distinctive feature is the **Priority Engine**, which mathematically
calculates task priority rather than relying on manual input.

## Features
- **Priority Engine**: Automatically calculates a priority score based on:
  - `Importance` (1–5 baseline multiplier)
  - `Urgency` (Points based on how close the deadline is)
  - `Penalty` (Extra points added if the task is overdue)
- **Database Persistence (JDBC + SQLite)**: Stores tasks in a local SQLite
  database — no setup required, the file is created automatically on first run.
- **Legacy JSON Storage**: A file-based `JsonTaskRepository` is also available
  as a lightweight, zero-dependency alternative (swap in `Main.java`).
- **Concurrency Safety**: Implements **Optimistic Locking** to prevent data
  loss when multiple clients attempt to update the same task simultaneously.
- **Intelligent Views**:
  - `Show All`: Automatically sorted with highest priority first.
  - `Urgent`: Shows tasks very close to their deadlines.
  - `Done`: Shows completed tasks (automatically down-ranked in priority view).
  - `Late`: Shows tasks that missed their deadlines.

## Architecture (MVC & Repository Pattern)
```text
smart-task-manager/
├── src/
│   └── main/java/com/taskmanager/
│       ├── model/       # Task entity, Enums, and Custom Exceptions
│       ├── repository/  # TaskRepository interface, JdbcTaskRepository,
│       │                  JsonTaskRepository
│       ├── service/     # TaskService (business logic & Priority Engine)
│       └── controller/  # TaskController (CLI interaction)
├── data/                # Auto-created directory for tasks.db / tasks.json
└── pom.xml              # Maven config (Java 17+, sqlite-jdbc, jackson)
```

## How Priority Works
`Priority = (Importance × 2) + Urgency + Penalty`

| Factor       | Logic                                                      |
|--------------|------------------------------------------------------------|
| Importance   | User input 1–5                                             |
| Urgency      | +10 if due today, +8 if due tomorrow, scales down after   |
| Penalty      | +10 flat if overdue, +2 per extra day late                |
| Done tasks   | Large negative score to stay at the bottom                |

## Switching Storage Backend
The app uses the **Repository Pattern** — you can swap storage with one line
in `Main.java`:

```java
// SQLite (default)
TaskRepository repository = new JdbcTaskRepository("jdbc:sqlite:data/tasks.db", null, null);

// JSON file (lightweight alternative)
TaskRepository repository = new JsonTaskRepository("data/tasks.json");
```

## Usage

Compile and package:
```bash
mvn clean package
```

Run tests:
```bash
mvn test
```

Run the CLI:
```bash
mvn exec:java -Dexec.mainClass="com.taskmanager.Main"
```