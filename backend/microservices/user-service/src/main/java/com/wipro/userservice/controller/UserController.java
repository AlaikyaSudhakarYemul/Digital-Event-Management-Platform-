package com.wipro.userservice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.wipro.userservice.config.JwtUtil;
import com.wipro.userservice.entity.Role;
import com.wipro.userservice.entity.Users;
import com.wipro.userservice.service.UserService;

@RestController
@RequestMapping("/api")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody(required = false) Users user) {
        if (user == null || user.getUserName() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Users savedUser = userService.registerUser(user);
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole());

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
            return ResponseEntity.badRequest().body("Email and password are required");
        }

        Users user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            user.setPassword(null);
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("token", token);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    @GetMapping("/user/profile")
    public ResponseEntity<?> getProfile(@RequestParam int userId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        Users user = userService.findById(userId);
        if (!authentication.getName().equalsIgnoreCase(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/user/{id}/contact")
    public ResponseEntity<?> updateContactNo(@PathVariable int id,
                                             @RequestBody Map<String, String> payload,
                                             Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String contactNo = payload.get("contactNo");
        if (contactNo == null || !contactNo.matches("\\d{10}")) {
            return ResponseEntity.badRequest().body("Contact number must be exactly 10 digits");
        }
        try {
            Users updated = userService.updateContactNo(id, contactNo, authentication.getName());
            updated.setPassword(null);
            return ResponseEntity.ok(updated);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @PutMapping("/user/{id}/password")
    public ResponseEntity<?> changePassword(@PathVariable int id,
                                            @RequestBody Map<String, String> payload,
                                            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String currentPassword = payload.get("currentPassword");
        String newPassword = payload.get("newPassword");

        if (currentPassword == null || newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body("Current password required and new password must be at least 6 characters");
        }
        try {
            userService.changePassword(id, currentPassword, newPassword, authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Password changed successfully. Please login again."));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody Users user) {
        if (user == null || user.getUserName() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username and password are required");
        }
        Users updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully!");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/all")
    public ResponseEntity<List<Map<String, Object>>> getAllUsersForAdmin() {
        List<Map<String, Object>> users = userService.getAllUsers().stream()
                .map(this::toSafeUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/organizers")
    public ResponseEntity<List<Map<String, Object>>> getAllOrganizersForAdmin() {
        List<Map<String, Object>> organizers = userService.getAllUsers().stream()
                .filter(u -> Role.ORGANIZER.equals(u.getRole()))
                .map(this::toSafeUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(organizers);
    }

    // Internal endpoint for other microservices
    @GetMapping("/internal/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable int id) {
        Users user = userService.findById(id);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    private Map<String, Object> toSafeUserResponse(Users user) {
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getUserId());
        response.put("userName", user.getUserName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("contactNo", user.getContactNo());
        return response;
    }
}
