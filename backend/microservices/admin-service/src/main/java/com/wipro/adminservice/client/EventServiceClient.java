package com.wipro.adminservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "event-service")
public interface EventServiceClient {

    @GetMapping("/api/events/all")
    Object getAllEvents();

    @DeleteMapping("/api/events/{id}")
    String deleteEvent(@PathVariable("id") int id);

    @PostMapping("/api/admin/add")
    Object addAddress(@RequestBody Map<String, Object> address);

    @GetMapping("/api/admin/{id}")
    Object getAddress(@PathVariable("id") int id);

    @GetMapping("/api/speakers")
    List<Object> getAllSpeakers();

    @PostMapping("/api/speakers")
    Object createSpeaker(@RequestBody Map<String, Object> speaker);

    @DeleteMapping("/api/speakers/{id}")
    Object deleteSpeaker(@PathVariable("id") int id);
}
