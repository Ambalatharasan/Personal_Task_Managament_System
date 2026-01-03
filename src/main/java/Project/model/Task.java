package Project.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Task {

	private Long id;
	private String title;
	private String description;

	@JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
	private LocalDate dueDate;

	private boolean completed;
	private String priority;
	private String status;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long userId;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime completedAt;

	public Task() {
		this.priority = "MEDIUM";
		this.status = "PENDING";
		this.completed = false;
	}

	public Task(Long id, String title, String description, LocalDate dueDate, boolean completed, Long userId) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.dueDate = dueDate;
		this.completed = completed;
		this.userId = userId;
		this.priority = "MEDIUM";
		this.status = completed ? "COMPLETED" : "PENDING";
	}

	public Task(Long id, String title, String description, LocalDate dueDate, boolean completed, String priority,
			String status, Long userId, LocalDateTime completedAt) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.dueDate = dueDate;
		this.completed = completed;
		this.priority = priority;
		this.status = status;
		this.userId = userId;
		this.completedAt = completedAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
		if (completed && this.status.equals("PENDING")) {
			this.status = "COMPLETED";
		}
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	@Override
	public String toString() {
		return "Task{" + "id=" + id + ", title='" + title + '\'' + ", description='" + description + '\'' + ", dueDate="
				+ dueDate + ", completed=" + completed + ", priority='" + priority + '\'' + ", status='" + status + '\''
				+ ", userId=" + userId + ", completedAt=" + completedAt + '}';
	}
}
