package com.wipro.ticketsservice.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service")
public interface EventServiceClient {

    @GetMapping("/api/events/{id}")
    Map<String, Object> getEventById(@PathVariable("id") int id);
}
