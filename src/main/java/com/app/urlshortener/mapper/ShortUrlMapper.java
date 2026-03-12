package com.app.urlshortener.mapper;

import com.app.urlshortener.dto.link.LinkStatsResponse;
import com.app.urlshortener.dto.link.ShortUrlResponse;
import com.app.urlshortener.entity.ShortUrl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ShortUrlMapper {

    private final String publicBaseUrl;

    public ShortUrlMapper(@Value("${app.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public ShortUrlResponse toResponse(ShortUrl entity) {
        return new ShortUrlResponse(
                entity.getId(),
                entity.getCode(),
                publicBaseUrl + "/api/v1/redirect/" + entity.getCode(),
                entity.getOriginalUrl(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getClickCount(),
                entity.isActive(),
                entity.isExpired(),
                entity.getOwner().getUsername()
        );
    }

    public LinkStatsResponse toStats(ShortUrl entity) {
        return new LinkStatsResponse(
                entity.getId(),
                entity.getCode(),
                entity.getOriginalUrl(),
                entity.getClickCount(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.isActive(),
                entity.isExpired()
        );
    }
}