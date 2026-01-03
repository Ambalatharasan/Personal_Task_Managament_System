package Project.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
	private Long id;
	private String username;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	private String email;
	private String role;
	private Boolean enabled;

	public User() {
		this.role = "USER";
		this.enabled = true;
	}

	public User(String username, String password) {
		this.username = username;
		this.password = password;
		this.role = "USER";
		this.enabled = true;
	}

	public User(Long id, String username, String password, String email, String role, Boolean enabled) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.email = email;
		this.role = role;
		this.enabled = enabled;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return "User{" + "id=" + id + ", username='" + username + '\'' + ", email='" + email + '\'' + ", role='" + role
				+ '\'' + ", enabled=" + enabled + '}';
	}
}
