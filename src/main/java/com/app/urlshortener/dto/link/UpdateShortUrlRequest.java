package com.app.urlshortener.dto.link;

import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record UpdateShortUrlRequest(
        @Size(max = 2048, message = "Original URL must be at most 2048 characters")
        String originalUrl,
        OffsetDateTime expiresAt,
        Boolean active
) {
}

