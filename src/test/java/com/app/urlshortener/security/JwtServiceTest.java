package com.app.urlshortener.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Test
    void shouldGenerateAndValidateToken() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("ThisIsATestJwtSecretThatIsDefinitelyLongEnough12345");
        properties.setExpirationMinutes(60);

        JwtService jwtService = new JwtService(properties);

        String token = jwtService.generateToken("andrii");

        assertNotNull(token);
        assertEquals("andrii", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, "andrii"));
        assertFalse(jwtService.isTokenValid(token, "other"));
    }
}
