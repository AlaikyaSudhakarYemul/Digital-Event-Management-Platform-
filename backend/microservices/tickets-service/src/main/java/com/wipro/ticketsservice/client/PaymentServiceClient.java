package com.wipro.ticketsservice.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "payment-service")
public interface PaymentServiceClient {

    @GetMapping("/api/payments/status/{registrationId}")
    Map<String, Object> getPaymentStatus(@PathVariable("registrationId") long registrationId);
}
