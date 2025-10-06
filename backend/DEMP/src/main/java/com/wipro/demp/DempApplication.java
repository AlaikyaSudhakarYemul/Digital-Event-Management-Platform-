package com.wipro.demp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;

@SpringBootApplication
public class DempApplication {

	public static void main(String[] args) {
		SpringApplication.run(DempApplication.class, args);
	}

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
    	return new RestTemplate();
	}

}
