package Project.scheduler;

import Project.dao.TaskDao;
import Project.executor.ActionExecutor;
import Project.model.Task;
import Project.rules.RuleEngine;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class TaskExecutionJob implements Job {

	private static final Logger logger = LoggerFactory.getLogger(TaskExecutionJob.class);

	@Autowired
	private TaskDao taskDao;

	@Autowired
	private ActionExecutor actionExecutor;

	@Autowired
	private RuleEngine ruleEngine;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			logger.info("Task execution job started");
			Long taskId = null;
			try {
				Object taskIdObj = context.getJobDetail().getJobDataMap().get("taskId");
				if (taskIdObj != null) {
					if (taskIdObj instanceof Long) {
						taskId = (Long) taskIdObj;
					} else if (taskIdObj instanceof Integer) {
						taskId = ((Integer) taskIdObj).longValue();
					} else if (taskIdObj instanceof String) {
						taskId = Long.parseLong((String) taskIdObj);
					}
				}
			} catch (Exception e) {
				logger.debug("No taskId in job data or invalid format: {}", e.getMessage());
			}

			if (taskId != null && taskId > 0) {
				executeSpecificTask(taskId);
			} else {
				executeAllPendingTasks();
			}

			logger.info("Task execution job completed");

		} catch (Exception e) {
			logger.error("Error during task execution job", e);
		}
	}

	private void executeSpecificTask(Long taskId) {
		logger.debug("Executing specific task: {}", taskId);

		try {
			Task task = taskDao.findById(taskId);

			if (task == null) {
				logger.warn("Task not found: {}", taskId);
				return;
			}

			if (task.isCompleted()) {
				logger.debug("Task {} is already completed, skipping", taskId);
				return;
			}

			if (ruleEngine.shouldExecute(task)) {
				logger.info("Executing actions for task: {}", taskId);
				actionExecutor.execute(task);
			} else {
				logger.debug("Task {} does not meet execution criteria", taskId);
			}
		} catch (Exception e) {
			logger.error("Error executing specific task {}", taskId, e);
		}
	}

	private void executeAllPendingTasks() {
		logger.debug("Checking all pending tasks");

		try {
			List<Task> pendingTasks = taskDao.findPendingTasksDueSoon();

			if (pendingTasks == null || pendingTasks.isEmpty()) {
				logger.debug("No pending tasks due soon");
				return;
			}

			logger.info("Found {} pending tasks to process", pendingTasks.size());

			for (Task task : pendingTasks) {
				try {
					if (ruleEngine.shouldExecute(task)) {
						logger.info("Executing actions for task: {}", task.getId());
						actionExecutor.execute(task);
					}
				} catch (Exception e) {
					logger.error("Error executing task {}", task.getId(), e);
				}
			}
		} catch (Exception e) {
			logger.error("Error fetching pending tasks", e);
		}
	}
}