package Project.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Project.model.Task;
import Project.scheduler.TaskExecutionJob;

@Service
public class SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    @Autowired
    private Scheduler scheduler;

    public void scheduleTask(Task task) {
        if (task == null || task.getId() == null || task.getDueDate() == null) {
            logger.warn("Invalid task provided for scheduling: {}", task);
            return;
        }

        LocalDateTime dueDate = task.getDueDate().atStartOfDay();

        if (dueDate.isBefore(LocalDateTime.now())) {
            logger.warn("Task {} has a due date in the past: {}. Skipping scheduling.", task.getId(), dueDate);
            return;
        }

        try {
            JobKey jobKey = new JobKey("task-" + task.getId());

            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                logger.info("Rescheduling existing task: {}", task.getId());
            }

            JobDetail job = JobBuilder.newJob(TaskExecutionJob.class).withIdentity(jobKey)
                    .usingJobData("taskId", task.getId()).build();

            LocalDateTime reminderTime = dueDate.minusHours(1);
            if (reminderTime.isBefore(LocalDateTime.now())) {
                reminderTime = LocalDateTime.now().plusMinutes(1);
            }

            Trigger trigger = TriggerBuilder.newTrigger().forJob(job).withIdentity("trigger-" + task.getId())
                    .startAt(Date.from(reminderTime.atZone(ZoneId.systemDefault()).toInstant())).build();

            scheduler.scheduleJob(job, trigger);
            logger.info("Successfully scheduled task: {} for {}", task.getId(), reminderTime);

        } catch (SchedulerException e) {
            logger.error("Failed to schedule task with ID: {}", task.getId(), e);
        }
    }

    public void cancelTask(Long taskId) {
        if (taskId == null) {
            logger.warn("Invalid task ID provided for cancellation");
            return;
        }

        try {
            JobKey jobKey = new JobKey("task-" + taskId);

            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                logger.info("Successfully cancelled scheduled task: {}", taskId);
            } else {
                logger.debug("Task {} was not scheduled, nothing to cancel", taskId);
            }

        } catch (SchedulerException e) {
            logger.error("Failed to cancel task with ID: {}", taskId, e);
        }
    }

    public void rescheduleTask(Task task) {
        cancelTask(task.getId());
        scheduleTask(task);
    }

    public boolean isTaskScheduled(Long taskId) {
        try {
            JobKey jobKey = new JobKey("task-" + taskId);
            return scheduler.checkExists(jobKey);
        } catch (SchedulerException e) {
            logger.error("Error checking if task {} is scheduled", taskId, e);
            return false;
        }
    }

    public LocalDateTime getNextExecutionTime(Long taskId) {
        try {
            JobKey jobKey = new JobKey("task-" + taskId);
            TriggerKey triggerKey = new TriggerKey("trigger-" + taskId);

            if (scheduler.checkExists(jobKey)) {
                Trigger trigger = scheduler.getTrigger(triggerKey);
                if (trigger != null) {
                    Date nextFireTime = trigger.getNextFireTime();
                    if (nextFireTime != null) {
                        return LocalDateTime.ofInstant(nextFireTime.toInstant(), ZoneId.systemDefault());
                    }
                }
            }
        } catch (SchedulerException e) {
            logger.error("Error getting next execution time for task {}", taskId, e);
        }

        return null;
    }
}
