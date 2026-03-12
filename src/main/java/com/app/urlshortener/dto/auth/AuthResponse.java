package com.app.urlshortener.dto.auth;

public record AuthResponse(
        String token,
        String tokenType,
        String username
) {
}
