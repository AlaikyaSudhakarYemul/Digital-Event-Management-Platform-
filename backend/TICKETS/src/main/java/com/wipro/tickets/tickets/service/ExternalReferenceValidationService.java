package com.wipro.tickets.tickets.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalReferenceValidationService {

    private final RestTemplate restTemplate;

    @Value("${services.demp.base-url:http://DEMP}")
    private String dempServiceBaseUrl;

    @Value("${services.event.base-url:http://EVENT}")
    private String eventServiceBaseUrl;

    public ExternalReferenceValidationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retry(name = "userServiceValidation")
    @CircuitBreaker(name = "userServiceValidation", fallbackMethod = "userValidationFallback")
    public void validateUserExists(int userId, String authorizationHeader) {
        String url = dempServiceBaseUrl + "/api/user/" + userId;

        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity(authorizationHeader),
                    Object.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalArgumentException("Invalid user reference: user " + userId + " not found");
            }
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().value() == 401 || ex.getStatusCode().value() == 403) {
                throw new IllegalArgumentException("Authorization token is required to validate user");
            }
            if (ex.getStatusCode().value() == 404) {
                throw new IllegalArgumentException("Invalid user reference: user " + userId + " not found");
            }
            throw ex;
        } catch (RestClientException ex) {
            throw new IllegalStateException("User service is unavailable right now", ex);
        }
    }

    @Retry(name = "eventServiceValidation")
    @CircuitBreaker(name = "eventServiceValidation", fallbackMethod = "eventValidationFallback")
    public void validateEventExists(int eventId) {
        String url = eventServiceBaseUrl + "/api/events/" + eventId;

        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    Object.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalArgumentException("Invalid event reference: event " + eventId + " not found");
            }
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new IllegalArgumentException("Invalid event reference: event " + eventId + " not found");
            }
            throw ex;
        } catch (RestClientException ex) {
            throw new IllegalStateException("Event service is unavailable right now", ex);
        }
    }

    @SuppressWarnings("unused")
    private void userValidationFallback(int userId, String authorizationHeader, Throwable throwable) {
        if (throwable instanceof IllegalArgumentException illegalArgumentException) {
            throw illegalArgumentException;
        }
        throw new IllegalStateException("User service is unavailable right now");
    }

    @SuppressWarnings("unused")
    private void eventValidationFallback(int eventId, Throwable throwable) {
        if (throwable instanceof IllegalArgumentException illegalArgumentException) {
            throw illegalArgumentException;
        }
        throw new IllegalStateException("Event service is unavailable right now");
    }

    private HttpEntity<Void> requestEntity(String authorizationHeader) {
        HttpHeaders headers = new HttpHeaders();
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader.trim());
        }
        return new HttpEntity<>(headers);
    }
}
