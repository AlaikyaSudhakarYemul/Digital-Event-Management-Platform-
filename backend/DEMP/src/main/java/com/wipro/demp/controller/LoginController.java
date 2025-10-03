package com.wipro.demp.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wipro.demp.config.JwtUtil;
import com.wipro.demp.entity.Users;
import com.wipro.demp.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class.getName());

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public LoginController(UserService userService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        logger.info("Initializing LoginController with UserService, JwtUtil, and PasswordEncoder");
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody(required = false) Users user) {

        if (user == null || user.getUserName() == null || user.getPassword() == null) {
            logger.warn("Registration attempt with missing username or password");
            Map<String, String> errorResponse = Map.of("error", "Username and password are required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        logger.info("Registering user: {}", user.getUserName());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // return ResponseEntity.ok(userService.registerUser(user));
        Users savedUser = userService.registerUser(user);

        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("user", savedUser);
        response.put("token", token);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        if (email == null || password == null) {
            logger.warn("Login attempt with missing email or password");
            return ResponseEntity.badRequest().body("Email and password are required");
        }
        logger.info("User login attempt for email: {}", email);

        Users user = userService.findByEmail(email);

        if (passwordEncoder.matches(password, user.getPassword())) {
            logger.info("User {} logged in successfully", user.getUserName());
            user.setPassword(null); // Do not expose password
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("token", token);

            return ResponseEntity.ok(response);

        }
        logger.warn("Invalid login attempt for email: {}", email);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    @GetMapping("/user/profile")
    public Users getProfile(@RequestParam String email) {
        if (email == null || email.isEmpty()) {
            logger.warn("Profile request with missing or empty email");
            throw new IllegalArgumentException("Email is required to fetch profile");
        }
        return userService.findByEmail(email);
    }
    
}
