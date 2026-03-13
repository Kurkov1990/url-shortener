package com.app.urlshortener.service;

import com.app.urlshortener.dto.link.CreateShortUrlRequest;
import com.app.urlshortener.dto.link.UpdateExpirationRequest;
import com.app.urlshortener.dto.link.UpdateShortUrlRequest;
import com.app.urlshortener.entity.ShortUrl;
import com.app.urlshortener.entity.User;
import com.app.urlshortener.exception.BadRequestException;
import com.app.urlshortener.exception.ConflictException;
import com.app.urlshortener.exception.NotFoundException;
import com.app.urlshortener.repository.ShortUrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class LinkService {

    private static final int MAX_ATTEMPTS = 20;

    private final ShortUrlRepository shortUrlRepository;
    private final CodeGenerator codeGenerator;

    public LinkService(ShortUrlRepository shortUrlRepository, CodeGenerator codeGenerator) {
        this.shortUrlRepository = shortUrlRepository;
        this.codeGenerator = codeGenerator;
    }

    @Transactional
    public ShortUrl create(CreateShortUrlRequest request, User owner) {
        OffsetDateTime now = OffsetDateTime.now();
        String normalizedUrl = normalizeAndValidateUrl(request.originalUrl());
        validateFutureExpiration(request.expiresAt(), now);

        String code = generateUniqueCode();

        ShortUrl entity = new ShortUrl();
        entity.setCode(code);
        entity.setOriginalUrl(normalizedUrl);
        entity.setExpiresAt(request.expiresAt());
        entity.setOwner(owner);
        entity.setActive(true);
        entity.setClickCount(0);

        return shortUrlRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<ShortUrl> findAll(User owner, boolean activeOnly) {
        if (activeOnly) {
            return shortUrlRepository.findAllByOwnerAndActiveTrueAndExpiresAtAfterOrderByCreatedAtDesc(
                    owner, OffsetDateTime.now()
            );
        }
        return shortUrlRepository.findAllByOwnerOrderByCreatedAtDesc(owner);
    }

    @Transactional(readOnly = true)
    public ShortUrl findOwnedById(Long id, User owner) {
        return shortUrlRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new NotFoundException("Short link not found"));
    }

    @Transactional
    public ShortUrl update(Long id, UpdateShortUrlRequest request, User owner) {
        ShortUrl entity = findOwnedById(id, owner);

        boolean changed =
                request.originalUrl() != null
                        || request.expiresAt() != null
                        || request.active() != null;

        if (!changed) {
            throw new BadRequestException("At least one field must be provided for update");
        }

        OffsetDateTime now = OffsetDateTime.now();

        String newOriginalUrl = null;
        OffsetDateTime newExpiresAt = null;
        Boolean newActive = null;

        if (request.originalUrl() != null) {
            newOriginalUrl = normalizeAndValidateUrl(request.originalUrl());
        }

        if (request.expiresAt() != null) {
            validateFutureExpiration(request.expiresAt(), now);
            newExpiresAt = request.expiresAt();
        }

        if (request.active() != null) {
            newActive = request.active();
        }

        if (newOriginalUrl != null) {
            entity.setOriginalUrl(newOriginalUrl);
        }
        if (newExpiresAt != null) {
            entity.setExpiresAt(newExpiresAt);
        }
        if (newActive != null) {
            entity.setActive(newActive);
        }

        return shortUrlRepository.save(entity);
    }

    @Transactional
    public ShortUrl updateExpiration(Long id, UpdateExpirationRequest request, User owner) {
        ShortUrl entity = findOwnedById(id, owner);

        validateFutureExpiration(request.expiresAt(), OffsetDateTime.now());

        entity.setExpiresAt(request.expiresAt());
        return shortUrlRepository.save(entity);
    }

    @Transactional
    public void delete(Long id, User owner) {
        shortUrlRepository.delete(findOwnedById(id, owner));
    }

    private String normalizeAndValidateUrl(String rawUrl) {
        if (rawUrl == null) {
            throw new BadRequestException("Original URL must not be null");
        }

        String normalizedUrl = rawUrl.trim();
        if (normalizedUrl.isBlank()) {
            throw new BadRequestException("Original URL must not be blank");
        }

        validateUrl(normalizedUrl);
        return normalizedUrl;
    }

    private void validateFutureExpiration(OffsetDateTime expiresAt, OffsetDateTime now) {
        if (expiresAt == null) {
            throw new BadRequestException("Expiration date must not be null");
        }

        if (!expiresAt.isAfter(now)) {
            throw new BadRequestException("Expiration date must be in the future");
        }
    }

    private void validateUrl(String rawUrl) {
        URI uri;
        try {
            uri = URI.create(rawUrl);
        } catch (Exception ex) {
            throw new BadRequestException("Original URL is not valid");
        }

        if (uri.getScheme() == null || uri.getHost() == null) {
            throw new BadRequestException("Original URL is not valid");
        }

        String scheme = uri.getScheme().toLowerCase();
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new BadRequestException("Original URL is not valid");
        }
    }

    private String generateUniqueCode() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String code = codeGenerator.generate();
            if (!shortUrlRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new ConflictException("Failed to generate a unique short code");
    }
}
