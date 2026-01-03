package Project.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Project.dto.ApiResponse;
import Project.model.Task;
import Project.service.TaskService;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskService taskService;

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return auth.getName();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Task>>> getAllTasks() {
        try {
            String username = getCurrentUsername();
            logger.info("Fetching all tasks for user: {}", username);

            List<Task> tasks = taskService.getTasksByUsername(username);

            return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", tasks));
        } catch (Exception e) {
            logger.error("Error fetching tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch tasks: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> getTaskById(@PathVariable Long id) {
        try {
            String username = getCurrentUsername();
            logger.info("Fetching task {} for user: {}", id, username);

            Task task = taskService.getTaskById(id, username);

            return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", task));
        } catch (RuntimeException e) {
            logger.error("Error fetching task {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Task>> createTask(@RequestBody Task task) {
        try {
            String username = getCurrentUsername();
            logger.info("Creating task for user: {}", username);

            Task createdTask = taskService.createTask(task, username);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Task created successfully", createdTask));
        } catch (Exception e) {
            logger.error("Error creating task", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create task: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> updateTask(@PathVariable Long id, @RequestBody Task task) {
        try {
            String username = getCurrentUsername();
            logger.info("Updating task {} for user: {}", id, username);

            Task updatedTask = taskService.updateTask(id, task, username);

            return ResponseEntity.ok(ApiResponse.success("Task updated successfully", updatedTask));
        } catch (RuntimeException e) {
            logger.error("Error updating task {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        try {
            String username = getCurrentUsername();
            logger.info("Deleting task {} for user: {}", id, username);

            taskService.deleteTask(id, username);

            return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
        } catch (RuntimeException e) {
            logger.error("Error deleting task {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Task>> completeTask(@PathVariable Long id) {
        try {
            String username = getCurrentUsername();
            logger.info("Marking task {} as complete for user: {}", id, username);

            Task task = taskService.completeTask(id, username);

            return ResponseEntity.ok(ApiResponse.success("Task marked as complete", task));
        } catch (RuntimeException e) {
            logger.error("Error completing task {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }
}
