package com.wipro.ticketsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TicketsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TicketsServiceApplication.class, args);
    }
}
