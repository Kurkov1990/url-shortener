package com.app.urlshortener.dto.link;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record UpdateExpirationRequest(
        @NotNull
        @Future
        OffsetDateTime expiresAt
) {
}
