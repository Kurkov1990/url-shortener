package com.app.urlshortener.service;

import com.app.urlshortener.dto.link.CreateShortUrlRequest;
import com.app.urlshortener.dto.link.UpdateExpirationRequest;
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
        validateUrl(request.originalUrl());
        String code = generateUniqueCode();

        ShortUrl entity = new ShortUrl();
        entity.setCode(code);
        entity.setOriginalUrl(request.originalUrl().trim());
        entity.setExpiresAt(request.expiresAt());
        entity.setOwner(owner);
        entity.setActive(true);
        entity.setClickCount(0);

        return shortUrlRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<ShortUrl> findAll(User owner, boolean activeOnly) {
        if (activeOnly) {
            return shortUrlRepository.findAllByOwnerAndActiveTrueAndExpiresAtAfterOrderByCreatedAtDesc(owner, OffsetDateTime.now());
        }
        return shortUrlRepository.findAllByOwnerOrderByCreatedAtDesc(owner);
    }

    @Transactional(readOnly = true)
    public ShortUrl findOwnedById(Long id, User owner) {
        return shortUrlRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new NotFoundException("Short link not found"));
    }

    @Transactional
    public ShortUrl updateExpiration(Long id, UpdateExpirationRequest request, User owner) {
        ShortUrl entity = findOwnedById(id, owner);
        entity.setExpiresAt(request.expiresAt());
        if (entity.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BadRequestException("Expiration date must be in the future");
        }
        return shortUrlRepository.save(entity);
    }

    @Transactional
    public void delete(Long id, User owner) {
        shortUrlRepository.delete(findOwnedById(id, owner));
    }

    private void validateUrl(String rawUrl) {
        try {
            URI uri = URI.create(rawUrl);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException("Missing scheme or host");
            }
            String scheme = uri.getScheme().toLowerCase();
            if (!scheme.equals("http") && !scheme.equals("https")) {
                throw new IllegalArgumentException("Unsupported scheme");
            }
        } catch (Exception ex) {
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
