package Project.dao;

import Project.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UserDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static final RowMapper<User> USER_ROW_MAPPER = new RowMapper<User>() {
		@Override
		public User mapRow(ResultSet rs, int rowNum) throws SQLException {
			User user = new User();
			user.setId(rs.getLong("id"));
			user.setUsername(rs.getString("username"));
			user.setPassword(rs.getString("password"));
			user.setEmail(rs.getString("email"));
			user.setRole(rs.getString("role"));
			user.setEnabled(rs.getBoolean("enabled"));
			return user;
		}
	};

	public void save(User user) {
		String sql = "INSERT INTO users (username, password, email, role, enabled) VALUES (?, ?, ?, ?, ?)";
		jdbcTemplate.update(sql, user.getUsername(), user.getPassword(), user.getEmail(),
				user.getRole() != null ? user.getRole() : "USER", user.isEnabled() != null ? user.isEnabled() : true);
	}

	public User findById(Long id) {
		try {
			String sql = "SELECT * FROM users WHERE id = ?";
			return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public User findByUsername(String username) {
		try {
			String sql = "SELECT * FROM users WHERE username = ?";
			return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, username);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public User findByEmail(String email) {
		try {
			String sql = "SELECT * FROM users WHERE email = ?";
			return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, email);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public List<User> findAll() {
		String sql = "SELECT * FROM users";
		return jdbcTemplate.query(sql, USER_ROW_MAPPER);
	}

	public void update(User user) {
		String sql = "UPDATE users SET username = ?, password = ?, email = ?, role = ?, enabled = ? WHERE id = ?";
		jdbcTemplate.update(sql, user.getUsername(), user.getPassword(), user.getEmail(), user.getRole(),
				user.isEnabled(), user.getId());
	}

	public void delete(Long id) {
		String sql = "DELETE FROM users WHERE id = ?";
		jdbcTemplate.update(sql, id);
	}

	public boolean existsByUsername(String username) {
		String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
		return count != null && count > 0;
	}

	public boolean existsByEmail(String email) {
		String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
		return count != null && count > 0;
	}
}
