package com.wipro.ticketsservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Forwards the caller's identity headers onto outgoing Feign calls so that
 * downstream services (which trust X-User-Email/X-User-Role, mirroring the
 * gateway's JwtAuthFilter) can authorize internal service-to-service calls
 * made on behalf of the original request.
 */
@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor authHeaderForwardingInterceptor() {
        return template -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return;
            }
            HttpServletRequest request = attrs.getRequest();
            forwardHeader(request, template, "Authorization");
            forwardHeader(request, template, "X-User-Email");
            forwardHeader(request, template, "X-User-Role");
        };
    }

    private void forwardHeader(HttpServletRequest request, feign.RequestTemplate template, String name) {
        String value = request.getHeader(name);
        if (value != null) {
            template.header(name, value);
        }
    }
}
