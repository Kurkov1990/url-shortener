package com.app.urlshortener.dto.link;

import java.time.OffsetDateTime;

public record ShortUrlResponse(
        Long id,
        String shortCode,
        String shortUrl,
        String originalUrl,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt,
        long clickCount,
        boolean active,
        boolean expired,
        String ownerUsername
) {
}
