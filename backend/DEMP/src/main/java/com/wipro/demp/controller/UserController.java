package com.wipro.demp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.wipro.demp.entity.Address;
import com.wipro.demp.entity.Users;
import com.wipro.demp.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final RestTemplate restTemplate;

    public UserController(UserService userService, RestTemplate restTemplate) {
        logger.info("Initializing UserController with UserService and RestTemplate");
        this.userService = userService;
        this.restTemplate = restTemplate;
    }

    @Value("${admin.service.url}")
    private String adminServiceUrl;

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable int id) {
        if(id<=0){
            logger.warn("User cannot be fetched with user ID: {}",id);
            return ResponseEntity.badRequest().body("USer ID given is incorrect");
        }
        Users user = userService.getUserById(id);
        return ResponseEntity.ok(user);
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

    // // @PreAuthorize("hasRole('ADMIN')")
    // @GetMapping("/user/{id}/address/{addressId}")
    // public ResponseEntity<?> getUserAddress(
    //     @PathVariable int id,
    //     @PathVariable int addressId,
    //     @RequestHeader("Authorization") String authHeader) {

    //     HttpHeaders headers = new HttpHeaders();
    //     String token = authHeader;
    //     // Remove all leading 'Bearer ' prefixes
    //     while (token.startsWith("Bearer ")) {
    //         token = token.substring(7).trim();
    //     }
    //     token = "Bearer " + token;
    //     headers.set("Authorization", token);
    //     System.out.println("Headers: " + headers);
    //     HttpEntity<String> entity = new HttpEntity<>(headers);
    //     ResponseEntity<Address> response = restTemplate.exchange(
    //         "http://localhost:8081/api/admin/" + addressId,
    //         HttpMethod.GET,
    //         entity,
    //         Address.class
    //     );
    //     Address address = response.getBody();
    //     return ResponseEntity.ok(address);
    // }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/address")
    public ResponseEntity<?> createAddress(@RequestBody Address address, @RequestHeader("Authorization") String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
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
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Address> entity = new HttpEntity<>(address, headers);
        ResponseEntity<Address> response = restTemplate.exchange(
            "http://localhost:8081/api/admin/" + id,
            HttpMethod.PUT,
            entity,
            Address.class
        );
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    // @PreAuthorize("hasRole('ADMIN')")
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

    // @PreAuthorize("hasRole('ADMIN')")
    // @GetMapping("/user/{id}/addresses")
    // public ResponseEntity<?> getUserAddresses(@PathVariable int id, @RequestHeader("Authorization") String authHeader) {
    //     HttpHeaders headers = new HttpHeaders();
    //     headers.set("Authorization", authHeader);
    //     HttpEntity<String> entity = new HttpEntity<>(headers);
    //     // Call ADMIN microservice to get addresses for user
    //     ResponseEntity<Address[]> response = restTemplate.exchange(
    //         "http://localhost:8081/api/admin/all" ,
    //         HttpMethod.GET,
    //         entity,
    //         Address[].class
    //     );
    //     return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    // }

    // @PreAuthorize("hasRole('ADMIN')")
    // @PutMapping("/user/{userId}/address/{addressId}")
    // public ResponseEntity<?> updateAddressForUser(
    //     @PathVariable int userId,
    //     @PathVariable int addressId,
    //     @RequestBody Address address,
    //     @RequestHeader("Authorization") String authHeader) {

    //     HttpHeaders headers = new HttpHeaders();
    //     headers.set("Authorization", authHeader);
    //     HttpEntity<Address> entity = new HttpEntity<>(address, headers);

    //     ResponseEntity<Address> response = restTemplate.exchange(
    //         "http://localhost:8081/api/admin/" + addressId,
    //         HttpMethod.PUT,
    //         entity,
    //         Address.class
    //     );
    //     return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    // }

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

    
    // @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/speakers/{id}")
    public ResponseEntity<?> getSpeakerById(@PathVariable int id, @RequestHeader("Authorization") String authHeader){
        logger.info("Fetching speaker by ID:{}",id);

        HttpHeaders headers = new HttpHeaders();
        String token = authHeader;
        while (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        token = "Bearer " + token;
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8081/api/speakers/" + id,
            HttpMethod.GET,
            entity,
            String.class
        );

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

    }

    @GetMapping("/speakers/all")
    public ResponseEntity<?> getAllSpeakers(@RequestHeader("Authorization") String authHeader){
        logger.info("Fetching all speakers");

        HttpHeaders headers = new HttpHeaders();
        String token = authHeader;
        while (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        token = "Bearer " + token;
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8081/api/speakers",
            HttpMethod.GET,
            entity,
            String.class
        );

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/speakers")
    public ResponseEntity<?> createSpeaker(@RequestBody String speakerData, @RequestHeader("Authorization") String authHeader){
        logger.info("Creating new speaker with data: {}", speakerData);

        HttpHeaders headers = new HttpHeaders();
        String token = authHeader;
        while (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        token = "Bearer " + token;
        headers.set("Authorization", token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(speakerData, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8081/api/speakers",
            HttpMethod.POST,
            entity,
            String.class
        );

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/speakers/{id}")
    public ResponseEntity<?> updateSpeaker(@PathVariable int id, @RequestBody String speakerData, @RequestHeader("Authorization") String authHeader){
        logger.info("Updating speaker with ID: {} and data: {}", id, speakerData);

        HttpHeaders headers = new HttpHeaders();
        String token = authHeader;
        while (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        token = "Bearer " + token;
        headers.set("Authorization", token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(speakerData, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8081/api/speakers/" + id,
            HttpMethod.PUT,
            entity,
            String.class
        );

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/speakers/{id}")
    public ResponseEntity<?> deleteSpeaker(@PathVariable int id, @RequestHeader("Authorization") String authHeader){
        logger.info("Deleting speaker with ID: {}", id);

        HttpHeaders headers = new HttpHeaders();
        String token = authHeader;
        while (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        token = "Bearer " + token;
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:8081/api/speakers/" + id,
            HttpMethod.DELETE,
            entity,
            String.class
        );

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

    }
}