package Project.controller;

import Project.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex, WebRequest request) {
		logger.error("Runtime exception occurred: ", ex);

		ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
		logger.error("Authentication exception: ", ex);

		ApiResponse<Void> response = ApiResponse.error("Authentication failed: " + ex.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
		logger.error("Illegal argument: ", ex);

		ApiResponse<Void> response = ApiResponse.error("Invalid input: " + ex.getMessage());
		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<ApiResponse<Void>> handleNullPointerException(NullPointerException ex, WebRequest request) {
		logger.error("Null pointer exception: ", ex);

		ApiResponse<Void> response = ApiResponse.error("An unexpected error occurred");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex, WebRequest request) {
		logger.error("Unexpected exception: ", ex);

		ApiResponse<Void> response = ApiResponse.error("An internal server error occurred");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

}
