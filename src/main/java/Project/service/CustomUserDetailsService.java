package Project.service;

import Project.dao.UserDao;
import Project.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

	private final UserDao userDao;

	public CustomUserDetailsService(UserDao userDao) {
		this.userDao = userDao;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		logger.debug("Loading user details for: {}", username);

		User user = userDao.findByUsername(username);

		if (user == null) {
			logger.warn("User not found: {}", username);
			throw new UsernameNotFoundException("User not found: " + username);
		}

		if (!user.isEnabled()) {
			logger.warn("User account is disabled: {}", username);
			throw new UsernameNotFoundException("User account is disabled: " + username);
		}

		logger.debug("User loaded successfully: {}", username);

		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
				user.isEnabled(), true, true, true,
				Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
	}
}
