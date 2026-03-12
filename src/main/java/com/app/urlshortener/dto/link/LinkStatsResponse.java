package com.app.urlshortener.dto.link;

import java.time.OffsetDateTime;

public record LinkStatsResponse(
        Long id,
        String shortCode,
        String originalUrl,
        long clickCount,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt,
        boolean active,
        boolean expired
) {
}
