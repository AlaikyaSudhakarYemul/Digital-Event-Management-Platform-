package com.wipro.demp.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExceptionHandlerTest {

    @Test
    void constructorSetsMessage() {
        ExceptionHandler ex = new ExceptionHandler("custom error");

        assertEquals("custom error", ex.getMessage());
    }
}
