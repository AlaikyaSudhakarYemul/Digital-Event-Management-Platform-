package com.wipro.organizerservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "registration-service")
public interface RegistrationServiceClient {

    @GetMapping("/api/registrations/event/{eventId}")
    List<Object> getRegistrationsByEventId(@PathVariable("eventId") int eventId);
}
