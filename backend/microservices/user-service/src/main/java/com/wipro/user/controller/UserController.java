package com.wipro.user.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wipro.user.config.JwtUtil;
import com.wipro.user.constants.UserServiceConstants;
import com.wipro.user.entity.Role;
import com.wipro.user.entity.Users;
import com.wipro.user.service.UserService;

@RestController
@RequestMapping(UserServiceConstants.API_URL)
@CrossOrigin(origins = UserServiceConstants.FRONTEND_URL)
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping(UserServiceConstants.REGISTER_URL)
    public ResponseEntity<?> register(@RequestBody(required = false) Users user) {
        if (user == null || user.getUserName() == null || user.getPassword() == null) {
            Map<String, String> errorResponse = Map.of("error", "Username and password are required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Users savedUser = userService.registerUser(user);
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("user", toSafeUser(savedUser));
        response.put("token", token);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping(UserServiceConstants.LOGIN_URL)
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }

        Users user = userService.findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        Map<String, Object> response = new HashMap<>();
        response.put("user", toSafeUser(user));
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @GetMapping(UserServiceConstants.USER_URL + "/profile")
    public ResponseEntity<?> getProfile(@RequestParam int userId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Users user = userService.findById(userId);
        if (!authentication.getName().equalsIgnoreCase(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }

        return ResponseEntity.ok(toSafeUser(user));
    }

    @PutMapping(UserServiceConstants.USER_URL + "/{id}/contact")
    public ResponseEntity<?> updateContactNo(@PathVariable int id,
        @RequestBody Map<String, String> payload,
        Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String contactNo = payload.get("contactNo");
        if (contactNo == null || !contactNo.matches("\\d{10}")) {
            return ResponseEntity.badRequest().body("Contact number must be exactly 10 digits");
        }

        try {
            Users updated = userService.updateContactNo(id, contactNo, authentication.getName());
            return ResponseEntity.ok(toSafeUser(updated));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping(UserServiceConstants.USER_URL + "/{id}/password")
    public ResponseEntity<?> changePassword(@PathVariable int id,
        @RequestBody Map<String, String> payload,
        Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String currentPassword = payload.get("currentPassword");
        String newPassword = payload.get("newPassword");

        if (currentPassword == null || currentPassword.isBlank() || newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                .body("Current password is required and new password must be at least 6 characters");
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

    @PutMapping(UserServiceConstants.USER_URL + "/{id}")
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody Users user) {
        if (user == null || user.getUserName() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username and password are required");
        }
        Users updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(toSafeUser(updatedUser));
    }

    @DeleteMapping(UserServiceConstants.USER_URL + "/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully!");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(UserServiceConstants.USER_URL + "/all")
    public ResponseEntity<List<Map<String, Object>>> getAllUsersForAdmin() {
        List<Map<String, Object>> users = userService.getAllUsers().stream()
            .map(this::toSafeUserResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(UserServiceConstants.USER_URL + "/organizers")
    public ResponseEntity<List<Map<String, Object>>> getAllOrganizersForAdmin() {
        List<Map<String, Object>> organizers = userService.getAllUsers().stream()
            .filter(u -> Role.ORGANIZER.equals(u.getRole()))
            .map(this::toSafeUserResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(organizers);
    }

    private Map<String, Object> toSafeUserResponse(Users user) {
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getUserId());
        response.put("userName", user.getUserName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("contactNo", user.getContactNo());
        response.put("createdOn", user.getCreatedOn());
        response.put("isDeleted", user.isDeleted());
        return response;
    }

    private Map<String, Object> toSafeUser(Users user) {
        return toSafeUserResponse(user);
    }
}
