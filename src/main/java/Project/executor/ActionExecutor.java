package Project.executor;

import Project.dao.UserDao;
import Project.model.Task;
import Project.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;

@Component
public class ActionExecutor {

	private static final Logger logger = LoggerFactory.getLogger(ActionExecutor.class);
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private UserDao userDao;

	public void execute(Task task) {
		if (task == null) {
			logger.warn("Cannot execute actions for null task");
			return;
		}

		logger.info("Executing actions for task: {}", task.getId());

		try {
			User user = userDao.findById(task.getUserId());

			if (user == null) {
				logger.error("User not found for task: {}", task.getId());
				return;
			}

			if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
				logger.warn("User {} has no email configured. Cannot send reminder for task: {}", user.getUsername(),
						task.getId());
				return;
			}

			sendEmailReminder(task, user);

			logger.info("Actions executed successfully for task: {}", task.getId());

		} catch (Exception e) {
			logger.error("Failed to execute actions for task: {}", task.getId(), e);
		}
	}

	private void sendEmailReminder(Task task, User user) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(user.getEmail());
			message.setSubject("Task Reminder: " + task.getTitle());

			StringBuilder emailBody = new StringBuilder();
			emailBody.append("Hello ").append(user.getUsername()).append(",\n\n");
			emailBody.append("This is a reminder for your upcoming task:\n\n");
			emailBody.append("Title: ").append(task.getTitle()).append("\n");

			if (task.getDescription() != null && !task.getDescription().isEmpty()) {
				emailBody.append("Description: ").append(task.getDescription()).append("\n");
			}

			if (task.getDueDate() != null) {
				emailBody.append("Due Date: ").append(task.getDueDate().format(DATE_FORMATTER)).append("\n");
			}

			emailBody.append("Priority: ").append(task.getPriority()).append("\n");
			emailBody.append("Status: ").append(task.getStatus()).append("\n\n");
			emailBody.append("Don't forget to complete this task!\n\n");
			emailBody.append("Best regards,\n");
			emailBody.append("Task Automation System");

			message.setText(emailBody.toString());

			mailSender.send(message);
			logger.info("Email reminder sent successfully to {} for task: {}", user.getEmail(), task.getId());

		} catch (Exception e) {
			logger.error("Failed to send email reminder for task: {}", task.getId(), e);
			throw new RuntimeException("Failed to send email reminder", e);
		}
	}

	public void executeAction(Task task, String actionType) {
		logger.info("Executing {} action for task: {}", actionType, task.getId());

		switch (actionType.toUpperCase()) {
		case "EMAIL":
			execute(task);
			break;
		case "NOTIFICATION":
			logger.info("Notification action not yet implemented");
			break;
		case "SMS":
			logger.info("SMS action not yet implemented");
			break;
		default:
			logger.warn("Unknown action type: {}", actionType);
		}
	}
}
