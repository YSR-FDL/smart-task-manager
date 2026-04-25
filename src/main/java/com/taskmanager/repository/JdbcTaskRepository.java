package com.taskmanager.repository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.exception.OptimisticLockException;


public class JdbcTaskRepository implements TaskRepository{

    private String DB_URL = "jdbc:sqlite:tasks.db";
    private  String username;
    private  String password;

    public JdbcTaskRepository(String DB_URL, String username, String password){
        this.DB_URL = DB_URL;
        this.username = username;
        this.password = password;
        initialiseDB();
    }
    private void initialiseDB(){
        String createTable = "CREATE TABLE IF NOT EXISTS tasks (id VARCHAR(36) PRIMARY KEY , title VARCHAR(30) NOT NULL, description VARCHAR(255), deadline DATETIME, importance INTEGER, status VARCHAR(10), version INTEGER DEFAULT 1);";

        try (Connection conn = DriverManager.getConnection(DB_URL, username, password);
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTable);

        } catch (SQLException e) {

            throw  new RuntimeException("Could not initialise the database", e);

        }
    }

    @Override
    public void save(Task task){
        String sql = "INSERT INTO tasks (id, title, description, deadline, importance, status) VALUES (?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, username, password);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, task.getId());
            pstmt.setString(2, task.getTitle());
            pstmt.setString(3, task.getDescription());
            if (task.getDeadline() != null) {
                pstmt.setTimestamp(4, Timestamp.valueOf(task.getDeadline()));
            } else {
                pstmt.setNull(4, java.sql.Types.TIMESTAMP); 
            }
            pstmt.setInt(5, task.getImportance());
            pstmt.setString(6, task.getStatus().name());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Could not save task", e);
        }
    }

    @Override
    public Optional<Task> findById(String id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, username, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTask(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve task with id: " + id, e);
        }
        return Optional.empty();
    }

    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getString("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));

        Timestamp deadlineTs = rs.getTimestamp("deadline");
        if (deadlineTs != null) {
            task.setDeadline(deadlineTs.toLocalDateTime());
        }

        int importance = rs.getInt("importance");
        if (!rs.wasNull()) {
            task.setImportance(importance);
        }

        String status = rs.getString("status");
        if (status != null) {
            task.setStatus(TaskStatus.valueOf(status));
        }
        
        task.setVersion(rs.getInt("version"));
        return task;
    }

    @Override
    public List<Task> findAll(){

        String sql = "SELECT * FROM tasks";
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }

        } catch (SQLException e) {

            throw new RuntimeException("Could not retrieve tasks", e);

        }

        return tasks;

    }

    @Override
    public void delete(String id){

        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, username, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Could not delete task with id: " + id, e);
        }
    }

    @Override
    public void update(Task task){

        String sql = "UPDATE tasks SET title = ?, description = ?, deadline = ?, importance = ?, status = ?, version = version + 1 WHERE id = ? AND version = ?";

        try(Connection conn = DriverManager.getConnection(DB_URL, username, password);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            if (task.getDeadline() != null) {
                pstmt.setTimestamp(3, Timestamp.valueOf(task.getDeadline()));
            } else {
                pstmt.setNull(3, java.sql.Types.TIMESTAMP);
            }
            pstmt.setInt(4, task.getImportance());
            pstmt.setString(5, task.getStatus().name());
            pstmt.setString(6, task.getId());
            pstmt.setInt(7, task.getVersion());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                if (findById(task.getId()).isEmpty()) {
                    throw new com.taskmanager.exception.TaskNotFoundException("Task not found with ID: " + task.getId());
                } else {
                    throw new OptimisticLockException("Could not update task with id: " + task.getId() + " as version " + task.getVersion() + " does not match the current version");
                }
            }
            
            task.setVersion(task.getVersion() + 1);
        } catch (SQLException e) {
            throw new RuntimeException("Could not update task", e);
        }
    }
}
