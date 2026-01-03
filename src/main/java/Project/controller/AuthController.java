package Project.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Project.dto.ApiResponse;
import Project.dto.AuthRequest;
import Project.dto.AuthResponse;
import Project.model.User;
import Project.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody AuthRequest authRequest) {
        try {
            logger.info("Login attempt for user: {}", authRequest.getUsername());

            String token = authService.authenticate(authRequest.getUsername(), authRequest.getPassword());
            User user = authService.getUserByUsername(authRequest.getUsername());

            AuthResponse response = new AuthResponse(token, user.getUsername(), user.getEmail(), "Login successful");

            logger.info("Login successful for user: {}", authRequest.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));

        } catch (RuntimeException e) {
            logger.error("Login failed for user: {}", authRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody User user) {
        try {
            logger.info("Registration attempt for user: {}", user.getUsername());

            authService.register(user);

            logger.info("Registration successful for user: {}", user.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered successfully", user.getUsername()));

        } catch (RuntimeException e) {
            logger.error("Registration failed for user: {}", user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateToken() {
        return ResponseEntity.ok(ApiResponse.success("Token is valid", "Token validated"));
    }
}
