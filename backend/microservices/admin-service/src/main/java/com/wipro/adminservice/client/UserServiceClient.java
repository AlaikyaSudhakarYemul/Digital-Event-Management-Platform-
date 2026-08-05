package com.wipro.adminservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/user/all")
    List<Map<String, Object>> getAllUsers();

    @GetMapping("/api/user/organizers")
    List<Map<String, Object>> getAllOrganizers();

    @DeleteMapping("/api/user/{id}")
    String deleteUser(@PathVariable("id") int id);
}
