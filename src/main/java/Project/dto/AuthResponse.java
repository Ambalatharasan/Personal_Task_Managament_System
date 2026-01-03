package Project.dto;

public class AuthResponse {
	private String token;
	private String username;
	private String email;
	private String message;
	private Long expiresIn;

	public AuthResponse() {
	}

	public AuthResponse(String token, String username, String email, String message) {
		this.token = token;
		this.username = username;
		this.email = email;
		this.message = message;
		this.expiresIn = 86400000L;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(Long expiresIn) {
		this.expiresIn = expiresIn;
	}
}
