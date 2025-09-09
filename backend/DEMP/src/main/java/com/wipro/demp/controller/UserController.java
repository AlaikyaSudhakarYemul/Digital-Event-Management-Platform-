package com.wipro.demp.controller;

import java.util.HashMap;
import java.util.Map;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.wipro.demp.config.JwtUtil;
import com.wipro.demp.entity.Users;
import com.wipro.demp.service.UserService;

 
@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
 
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
 
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
 
    public UserController(UserService userService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        logger.info("Initializing UserController with UserService, JwtUtil, and PasswordEncoder");
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }
 
    @PostMapping("/auth/register")
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
 
        // Return user info and token
        Map<String, Object> response = new HashMap<>();
        response.put("user", savedUser);
        response.put("token", token);
 
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
 
    @PostMapping("/auth/login")
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
 
    @PutMapping("/user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody Users user) {
        if (user == null || user.getUserName() == null || user.getPassword() == null) {
            logger.warn("Update attempt with missing username or password for user ID: {}", id);
            return ResponseEntity.badRequest().body("Username and password are required");
        }
        Users updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }
 
    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        logger.info("Deleting user with ID: {}", id);
        userService.deleteUser(id);
        logger.info("User with ID: {} deleted successfully", id);
        return ResponseEntity.ok("User deleted successfully!");
 
    }
 
}