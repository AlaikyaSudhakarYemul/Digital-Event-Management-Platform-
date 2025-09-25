package com.wipro.demp.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.wipro.demp.config.JwtUtil;
import com.wipro.demp.entity.Address;
import com.wipro.demp.entity.Users;
import com.wipro.demp.service.UserService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    public UserController(UserService userService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, RestTemplate restTemplate) {
        logger.info("Initializing UserController with UserService, JwtUtil, and PasswordEncoder");
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
    }

    @Value("${admin.service.url}")
    private String adminServiceUrl;

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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{id}/address/{addressId}")
    public ResponseEntity<?> getUserAddress(
        @PathVariable int id,
        @PathVariable int addressId,
        @RequestHeader("Authorization") String authHeader) {

        HttpHeaders headers = new HttpHeaders();
        String token = authHeader;
        // Remove all leading 'Bearer ' prefixes
        while (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        token = "Bearer " + token;
        headers.set("Authorization", token);
        System.out.println("Headers: " + headers);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Address> response = restTemplate.exchange(
            "http://localhost:8081/api/admin/" + addressId,
            HttpMethod.GET,
            entity,
            Address.class
        );
        Address address = response.getBody();
        return ResponseEntity.ok(address);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/address")
    public ResponseEntity<?> createAddress(@RequestBody Address address, @RequestHeader("Authorization") String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<Address> entity = new HttpEntity<>(address, headers);
        ResponseEntity<Address> response = restTemplate.exchange(
            "http://localhost:8081/api/admin/add",
            HttpMethod.POST,
            entity,
            Address.class
        );
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/address/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable int id, @RequestBody Address address, @RequestHeader("Authorization") String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<Address> entity = new HttpEntity<>(address, headers);
        ResponseEntity<Address> response = restTemplate.exchange(
            "http://localhost:8081/api/admin/" + id,
            HttpMethod.PUT,
            entity,
            Address.class
        );
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/addresses")
    public ResponseEntity<?> getAllAddresses(@RequestHeader("Authorization") String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Address[]> response = restTemplate.exchange(
            "http://localhost:8081/api/admin/all",
            HttpMethod.GET,
            entity,
            Address[].class
        );
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/address/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable int id, @RequestHeader("Authorization") String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8081/api/admin/" + id,
            HttpMethod.DELETE,
            entity,
            String.class
        );
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{id}/addresses")
    public ResponseEntity<?> getUserAddresses(@PathVariable int id, @RequestHeader("Authorization") String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        // Call ADMIN microservice to get addresses for user
        ResponseEntity<Address[]> response = restTemplate.exchange(
            "http://localhost:8081/api/admin/all" ,
            HttpMethod.GET,
            entity,
            Address[].class
        );
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/user/{userId}/address/{addressId}")
    public ResponseEntity<?> updateAddressForUser(
        @PathVariable int userId,
        @PathVariable int addressId,
        @RequestBody Address address,
        @RequestHeader("Authorization") String authHeader) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<Address> entity = new HttpEntity<>(address, headers);

        ResponseEntity<Address> response = restTemplate.exchange(
            "http://localhost:8081/api/admin/" + addressId,
            HttpMethod.PUT,
            entity,
            Address.class
        );
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/user/{userId}/address/{addressId}")
    public ResponseEntity<?> deleteAddressForUser(
        @PathVariable int userId,
        @PathVariable int addressId,
        @RequestHeader("Authorization") String authHeader) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8081/api/admin/" + addressId,
            HttpMethod.DELETE,
            entity,
            String.class
        );
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}