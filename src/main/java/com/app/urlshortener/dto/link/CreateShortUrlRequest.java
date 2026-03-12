package com.app.urlshortener.dto.link;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateShortUrlRequest(
        @NotBlank
        @Size(max = 2048)
        String originalUrl,

        @NotNull
        @Future
        OffsetDateTime expiresAt
) {
}
