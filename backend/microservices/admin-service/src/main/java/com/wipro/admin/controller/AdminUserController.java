package com.wipro.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wipro.admin.constants.AdminServiceConstants;
import com.wipro.admin.entity.Users;
import com.wipro.admin.service.AdminUserService;

@RestController
@RequestMapping(AdminServiceConstants.API_URL + AdminServiceConstants.USER_URL)
@CrossOrigin(origins = AdminServiceConstants.FRONTEND_URL)
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllUsersForAdmin() {
        List<Map<String, Object>> users = adminUserService.getAllUsers().stream()
            .map(this::toSafeUserResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/organizers")
    public ResponseEntity<List<Map<String, Object>>> getAllOrganizersForAdmin() {
        List<Map<String, Object>> organizers = adminUserService.getOrganizers().stream()
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
}
