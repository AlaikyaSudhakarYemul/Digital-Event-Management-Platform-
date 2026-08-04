package com.wipro.demp.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.InvalidParameterException;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleIllegalArgumentReturnsBadRequest() {
        ResponseEntity<String> response = handler.handleIllegalArgument(new IllegalArgumentException("bad input"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("bad input", response.getBody());
    }

    @Test
    void handleInvalidParamReturnsStructuredBody() {
        ResponseEntity<Map<String, String>> response = handler.handleInvalidParam(new InvalidParameterException("id must be positive"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid parameter", response.getBody().get("error"));
    }

    @Test
    void handleValidationReturnsAggregatedMessage() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("obj", "email", "must be valid"));
        bindingResult.addError(new FieldError("obj", "name", "must not be blank"));

        MethodParameter parameter = new MethodParameter(
                this.getClass().getDeclaredMethod("dummyMethod", String.class), 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidation(ex);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Validation failed", response.getBody().get("error"));
        String details = response.getBody().get("details");
        assertEquals(true, details.contains("email: must be valid"));
        assertEquals(true, details.contains("name: must not be blank"));
    }

    private void dummyMethod(String input) {
    }
}
