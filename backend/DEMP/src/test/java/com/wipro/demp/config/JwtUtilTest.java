package com.wipro.demp.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.wipro.demp.entity.Role;

class JwtUtilTest {

    private static String validBase64Secret() {
        String raw = "01234567890123456789012345678901";
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void generateAndExtractTokenClaims() {
        JwtUtil jwtUtil = new JwtUtil(validBase64Secret(), 3_600_000L);

        String token = jwtUtil.generateToken("demo@user.com", Role.USER);

        assertNotNull(token);
        assertEquals("demo@user.com", jwtUtil.extractEmail(token));
        assertEquals("ROLE_USER", jwtUtil.extractRole(token));
    }

    @Test
    void constructorRejectsEmptySecret() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new JwtUtil("", 3_600_000L));

        assertEquals("JWT secret key is missing or empty.", ex.getMessage());
    }
}
