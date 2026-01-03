package Project.service;

import Project.dao.UserDao;
import Project.model.User;
import Project.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

	private final UserDao userDao;
	private final JwtUtil jwtUtil;
	private final PasswordEncoder passwordEncoder;

	public AuthService(UserDao userDao, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
		this.userDao = userDao;
		this.jwtUtil = jwtUtil;
		this.passwordEncoder = passwordEncoder;
	}

	public String authenticate(String username, String password) {
		logger.debug("Authenticating user: {}", username);

		User user = userDao.findByUsername(username);

		if (user == null) {
			logger.warn("Authentication failed: User not found - {}", username);
			throw new RuntimeException("Invalid username or password");
		}

		if (!user.isEnabled()) {
			logger.warn("Authentication failed: User disabled - {}", username);
			throw new RuntimeException("User account is disabled");
		}

		if (!passwordEncoder.matches(password, user.getPassword())) {
			logger.warn("Authentication failed: Invalid password for user - {}", username);
			throw new RuntimeException("Invalid username or password");
		}

		logger.info("User authenticated successfully: {}", username);
		return jwtUtil.generateToken(username);
	}

	public void register(User user) {
		logger.debug("Attempting to register user: {}", user.getUsername());

		validateUserInput(user);

		if (userDao.existsByUsername(user.getUsername())) {
			logger.warn("Registration failed: Username already exists - {}", user.getUsername());
			throw new RuntimeException("Username already exists");
		}

		if (user.getEmail() != null && !user.getEmail().isEmpty() && userDao.existsByEmail(user.getEmail())) {
			logger.warn("Registration failed: Email already exists - {}", user.getEmail());
			throw new RuntimeException("Email already exists");
		}

		String hashedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(hashedPassword);

		if (user.getRole() == null || user.getRole().isEmpty()) {
			user.setRole("USER");
		}

		if (user.isEnabled() == null) {
			user.setEnabled(true);
		}

		userDao.save(user);

		logger.info("User registered successfully: {}", user.getUsername());
	}

	public User getUserByUsername(String username) {
		logger.debug("Fetching user by username: {}", username);

		User user = userDao.findByUsername(username);

		if (user == null) {
			logger.warn("User not found: {}", username);
			throw new RuntimeException("User not found");
		}

		return user;
	}

	public void changePassword(String username, String oldPassword, String newPassword) {
		logger.debug("Changing password for user: {}", username);

		User user = userDao.findByUsername(username);

		if (user == null) {
			throw new RuntimeException("User not found");
		}

		if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
			logger.warn("Password change failed: Invalid old password for user - {}", username);
			throw new RuntimeException("Invalid old password");
		}

		if (newPassword == null || newPassword.length() < 6) {
			throw new RuntimeException("New password must be at least 6 characters");
		}

		user.setPassword(passwordEncoder.encode(newPassword));
		userDao.update(user);

		logger.info("Password changed successfully for user: {}", username);
	}

	private void validateUserInput(User user) {
		if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
			throw new RuntimeException("Username cannot be empty");
		}

		if (user.getUsername().length() < 3) {
			throw new RuntimeException("Username must be at least 3 characters");
		}

		if (user.getUsername().length() > 50) {
			throw new RuntimeException("Username must be less than 50 characters");
		}

		if (user.getPassword() == null || user.getPassword().isEmpty()) {
			throw new RuntimeException("Password cannot be empty");
		}

		if (user.getPassword().length() < 6) {
			throw new RuntimeException("Password must be at least 6 characters");
		}

		if (user.getEmail() != null && !user.getEmail().isEmpty()) {
			if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
				throw new RuntimeException("Invalid email format");
			}
		}
	}
}
