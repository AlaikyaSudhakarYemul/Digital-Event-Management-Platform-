package com.wipro.organizerservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "event-service")
public interface EventServiceClient {

    @GetMapping("/api/events/organizer/{userId}")
    List<Object> getEventsByOrganizerId(@PathVariable("userId") int userId);

    @PostMapping("/api/events/create")
    Object createEvent(@RequestBody Object event);

    @PutMapping("/api/events/{id}")
    Object updateEvent(@PathVariable("id") int id, @RequestBody Object event);

    @DeleteMapping("/api/events/{id}")
    String deleteEvent(@PathVariable("id") int id);
}
