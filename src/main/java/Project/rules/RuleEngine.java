package Project.rules;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import Project.model.Task;

@Component
public class RuleEngine {

    private static final Logger logger = LoggerFactory.getLogger(RuleEngine.class);

    public boolean shouldExecute(Task task) {
        if (task == null) {
            logger.warn("Null task provided to rule engine");
            return false;
        }

        logger.debug("Evaluating execution rules for task: {}", task.getId());

        if (task.isCompleted()) {
            logger.debug("Task {} is already completed", task.getId());
            return false;
        }

        if (task.getDueDate() == null) {
            logger.debug("Task {} has no due date", task.getId());
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = task.getDueDate().atStartOfDay();

        if (dueDate.isBefore(now)) {
            logger.debug("Task {} is overdue", task.getId());
            return false;
        }

        long minutesUntilDue = ChronoUnit.MINUTES.between(now, dueDate);
        boolean isDueSoon = minutesUntilDue <= 60 && minutesUntilDue >= 0;

        if (isDueSoon) {
            logger.info("Task {} meets execution criteria (due in {} minutes)", task.getId(), minutesUntilDue);
        } else {
            logger.debug("Task {} not due soon enough (due in {} minutes)", task.getId(), minutesUntilDue);
        }

        return isDueSoon;
    }

    public boolean isOverdue(Task task) {
        if (task == null || task.getDueDate() == null) {
            return false;
        }

        boolean overdue = task.getDueDate().isBefore(LocalDateTime.now().toLocalDate()) && !task.isCompleted();

        if (overdue) {
            logger.debug("Task {} is overdue", task.getId());
        }

        return overdue;
    }

    public boolean isHighPriority(Task task) {
        if (task == null || task.getPriority() == null) {
            return false;
        }

        return "HIGH".equalsIgnoreCase(task.getPriority());
    }

    public int calculateUrgencyScore(Task task) {
        if (task == null || task.getDueDate() == null || task.isCompleted()) {
            return 0;
        }

        int score = 0;

        switch (task.getPriority().toUpperCase()) {
            case "HIGH":
                score += 50;
                break;
            case "MEDIUM":
                score += 30;
                break;
            case "LOW":
                score += 10;
                break;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = task.getDueDate().atStartOfDay();
        long hoursUntilDue = ChronoUnit.HOURS.between(now, dueDate);

        if (hoursUntilDue < 0) {
            score += 100;
        } else if (hoursUntilDue <= 1) {
            score += 80;
        } else if (hoursUntilDue <= 6) {
            score += 60;
        } else if (hoursUntilDue <= 24) {
            score += 40;
        } else if (hoursUntilDue <= 72) {
            score += 20;
        }

        logger.debug("Task {} urgency score: {}", task.getId(), score);
        return score;
    }

    public boolean needsImmediateAttention(Task task) {
        if (task == null || task.isCompleted()) {
            return false;
        }

        boolean immediate = isOverdue(task) || (isHighPriority(task) && shouldExecute(task));

        if (immediate) {
            logger.info("Task {} needs immediate attention", task.getId());
        }

        return immediate;
    }
}
