package Project.dao;

import Project.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class TaskDao {

    private static final Logger logger = LoggerFactory.getLogger(TaskDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<Task> TASK_ROW_MAPPER = new RowMapper<Task>() {
        @Override
        public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
            Task task = new Task();
            task.setId(rs.getLong("id"));
            task.setTitle(rs.getString("title"));
            task.setDescription(rs.getString("description"));

            Timestamp dueDate = rs.getTimestamp("due_date");
            if (dueDate != null) {
                task.setDueDate(dueDate.toLocalDateTime().toLocalDate());
            }

            task.setCompleted(rs.getBoolean("completed"));
            task.setPriority(rs.getString("priority"));
            task.setStatus(rs.getString("status"));
            task.setUserId(rs.getLong("user_id"));

            Timestamp completedAt = rs.getTimestamp("completed_at");
            if (completedAt != null) {
                task.setCompletedAt(completedAt.toLocalDateTime());
            }

            return task;
        }
    };

    public Task save(Task task) {
        String sql = "INSERT INTO tasks (title, description, due_date, completed, priority, status, user_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setTimestamp(3, task.getDueDate() != null ? Timestamp.valueOf(task.getDueDate().atStartOfDay()) : null);
            ps.setBoolean(4, task.isCompleted());
            ps.setString(5, task.getPriority() != null ? task.getPriority() : "MEDIUM");
            ps.setString(6, task.getStatus() != null ? task.getStatus() : "PENDING");
            ps.setLong(7, task.getUserId());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            task.setId(keyHolder.getKey().longValue());
        }

        return task;
    }

    public Task findById(Long id) {
        try {
            String sql = "SELECT * FROM tasks WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, TASK_ROW_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Task> findAll() {
        String sql = "SELECT * FROM tasks ORDER BY due_date ASC";
        return jdbcTemplate.query(sql, TASK_ROW_MAPPER);
    }

    public List<Task> findByUserId(Long userId) {
        logger.debug("Finding tasks for user ID: {}", userId);
        String sql = "SELECT * FROM tasks WHERE user_id = ? ORDER BY due_date ASC";
        List<Task> tasks = jdbcTemplate.query(sql, TASK_ROW_MAPPER, userId);
        logger.debug("Found {} tasks for user ID: {}", tasks.size(), userId);
        return tasks;
    }

    public List<Task> findByUserIdAndCompleted(Long userId, boolean completed) {
        String sql = "SELECT * FROM tasks WHERE user_id = ? AND completed = ? ORDER BY due_date ASC";
        return jdbcTemplate.query(sql, TASK_ROW_MAPPER, userId, completed);
    }

    public List<Task> findPendingTasksDueSoon() {
        String sql = "SELECT * FROM tasks WHERE completed = false " + "AND due_date IS NOT NULL "
                + "AND due_date > CURRENT_TIMESTAMP " + "AND due_date < TIMESTAMPADD(HOUR, 1, CURRENT_TIMESTAMP) "
                + "ORDER BY due_date ASC";
        return jdbcTemplate.query(sql, TASK_ROW_MAPPER);
    }

    public List<Task> findOverdueTasks() {
        String sql = "SELECT * FROM tasks WHERE completed = false " + "AND due_date IS NOT NULL "
                + "AND due_date < CURRENT_TIMESTAMP " + "ORDER BY due_date ASC";
        return jdbcTemplate.query(sql, TASK_ROW_MAPPER);
    }

    public void update(Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, due_date = ?, completed = ?, "
                + "priority = ?, status = ?, completed_at = ? WHERE id = ?";

        jdbcTemplate.update(sql, task.getTitle(), task.getDescription(),
                task.getDueDate() != null ? Timestamp.valueOf(task.getDueDate().atStartOfDay()) : null, task.isCompleted(),
                task.getPriority(), task.getStatus(),
                task.getCompletedAt() != null ? Timestamp.valueOf(task.getCompletedAt()) : null, task.getId());
    }

    public void delete(Long id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public int countByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    public int countCompletedByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE user_id = ? AND completed = true";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }
}
