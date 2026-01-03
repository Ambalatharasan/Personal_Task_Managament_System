package Project.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Project.dao.TaskDao;
import Project.dao.UserDao;
import Project.model.Task;
import Project.model.User;

@Service
@Transactional
public class TaskService {

	private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

	@Autowired
	private TaskDao taskDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private SchedulerService schedulerService;

	@Transactional(readOnly = true)
	public List<Task> getTasksByUsername(String username) {
		logger.debug("Fetching tasks for user: {}", username);

		User user = userDao.findByUsername(username);
		if (user == null) {
			throw new RuntimeException("User not found: " + username);
		}

		return taskDao.findByUserId(user.getId());
	}

	@Transactional(readOnly = true)
	public List<Task> getAllTasks() {
		logger.debug("Fetching all tasks");
		return taskDao.findAll();
	}

	@Transactional(readOnly = true)
	public Task getTaskById(Long id, String username) {
		logger.debug("Fetching task {} for user: {}", id, username);

		User user = userDao.findByUsername(username);
		if (user == null) {
			throw new RuntimeException("User not found: " + username);
		}

		Task task = taskDao.findById(id);
		if (task == null) {
			throw new RuntimeException("Task not found: " + id);
		}

		if (!task.getUserId().equals(user.getId())) {
			throw new RuntimeException("Unauthorized: Task does not belong to user");
		}

		return task;
	}

	public Task createTask(Task task, String username) {
		logger.debug("Creating task for user: {}", username);

		User user = userDao.findByUsername(username);
		if (user == null) {
			throw new RuntimeException("User not found: " + username);
		}

		validateTask(task);

		task.setUserId(user.getId());

		if (task.getPriority() == null || task.getPriority().isEmpty()) {
			task.setPriority("MEDIUM");
		}

		if (task.getStatus() == null || task.getStatus().isEmpty()) {
			task.setStatus("PENDING");
		}

		Task savedTask = taskDao.save(task);

		if (savedTask.getDueDate() != null) {
			schedulerService.scheduleTask(savedTask);
		}

		logger.info("Task created successfully: {}", savedTask.getId());
		return savedTask;
	}

	public Task updateTask(Long id, Task task, String username) {
		logger.debug("Updating task {} for user: {}", id, username);

		User user = userDao.findByUsername(username);
		if (user == null) {
			throw new RuntimeException("User not found: " + username);
		}

		Task existingTask = taskDao.findById(id);
		if (existingTask == null) {
			throw new RuntimeException("Task not found: " + id);
		}

		if (!existingTask.getUserId().equals(user.getId())) {
			throw new RuntimeException("Unauthorized: Task does not belong to user");
		}

		validateTask(task);

		task.setId(id);
		task.setUserId(user.getId());

		if (task.isCompleted() && !existingTask.isCompleted()) {
			task.setCompletedAt(LocalDateTime.now());
			task.setStatus("COMPLETED");
		}

		taskDao.update(task);

		if (task.getDueDate() != null && !task.isCompleted()) {
			schedulerService.scheduleTask(task);
		} else if (task.isCompleted()) {
			schedulerService.cancelTask(id);
		}

		logger.info("Task updated successfully: {}", id);
		return task;
	}

	public void deleteTask(Long id, String username) {
		logger.debug("Deleting task {} for user: {}", id, username);

		User user = userDao.findByUsername(username);
		if (user == null) {
			throw new RuntimeException("User not found: " + username);
		}

		Task existingTask = taskDao.findById(id);
		if (existingTask == null) {
			throw new RuntimeException("Task not found: " + id);
		}

		if (!existingTask.getUserId().equals(user.getId())) {
			throw new RuntimeException("Unauthorized: Task does not belong to user");
		}

		schedulerService.cancelTask(id);

		taskDao.delete(id);

		logger.info("Task deleted successfully: {}", id);
	}

	public Task completeTask(Long id, String username) {
		logger.debug("Marking task {} as complete for user: {}", id, username);

		Task task = getTaskById(id, username);

		task.setCompleted(true);
		task.setCompletedAt(LocalDateTime.now());
		task.setStatus("COMPLETED");

		taskDao.update(task);

		schedulerService.cancelTask(id);

		logger.info("Task marked as complete: {}", id);
		return task;
	}

	@Transactional(readOnly = true)
	public List<Task> getCompletedTasks(String username) {
		logger.debug("Fetching completed tasks for user: {}", username);

		User user = userDao.findByUsername(username);
		if (user == null) {
			throw new RuntimeException("User not found: " + username);
		}

		return taskDao.findByUserIdAndCompleted(user.getId(), true);
	}

	@Transactional(readOnly = true)
	public List<Task> getPendingTasks(String username) {
		logger.debug("Fetching pending tasks for user: {}", username);

		User user = userDao.findByUsername(username);
		if (user == null) {
			throw new RuntimeException("User not found: " + username);
		}

		return taskDao.findByUserIdAndCompleted(user.getId(), false);
	}

	@Transactional(readOnly = true)
	public List<Task> getOverdueTasks(String username) {
		logger.debug("Fetching overdue tasks for user: {}", username);

		User user = userDao.findByUsername(username);
		if (user == null) {
			throw new RuntimeException("User not found: " + username);
		}

		List<Task> allOverdue = taskDao.findOverdueTasks();
		return allOverdue.stream().filter(task -> task.getUserId().equals(user.getId())).toList();
	}

	private void validateTask(Task task) {
		if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
			throw new RuntimeException("Task title cannot be empty");
		}

		if (task.getTitle().length() > 500) {
			throw new RuntimeException("Task title is too long (max 500 characters)");
		}

		if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now())) {
			throw new RuntimeException("Due date cannot be in the past");
		}

		if (task.getPriority() != null && !task.getPriority().matches("HIGH|MEDIUM|LOW")) {
			throw new RuntimeException("Invalid priority. Must be HIGH, MEDIUM, or LOW");
		}

		if (task.getStatus() != null && !task.getStatus().matches("PENDING|IN_PROGRESS|COMPLETED|CANCELLED")) {
			throw new RuntimeException("Invalid status");
		}
	}
}
