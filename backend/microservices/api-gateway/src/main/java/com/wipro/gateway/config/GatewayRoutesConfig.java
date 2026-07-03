package com.wipro.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

	@Bean
	RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
			.route("admin-service", route -> route
				.path("/api/admin/**", "/api/speakers/**", "/api/user/all", "/api/user/organizers")
				.uri("lb://ADMIN-SERVICE"))
			.route("user-service", route -> route
				.path("/api/auth/**", "/api/user/**")
				.uri("lb://USER-SERVICE"))
			.route("tickets-service", route -> route
				.path("/api/tickets/**")
				.uri("lb://TICKETS-SERVICE"))
			.route("demp-service", route -> route
				.path("/api/**")
				.uri("lb://DEMP-SERVICE"))
			.build();
	}
}
