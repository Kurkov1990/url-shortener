package com.app.urlshortener.service;

import com.app.urlshortener.dto.link.CreateShortUrlRequest;
import com.app.urlshortener.dto.link.UpdateExpirationRequest;
import com.app.urlshortener.dto.link.UpdateShortUrlRequest;
import com.app.urlshortener.entity.ShortUrl;
import com.app.urlshortener.entity.User;
import com.app.urlshortener.exception.BadRequestException;
import com.app.urlshortener.repository.ShortUrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LinkServiceTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private CodeGenerator codeGenerator;

    @InjectMocks
    private LinkService linkService;

    @Test
    void shouldCreateShortLink() {
        User user = new User();
        user.setUsername("andrii");

        CreateShortUrlRequest request =
                new CreateShortUrlRequest("https://example.com", OffsetDateTime.now().plusDays(1));

        when(codeGenerator.generate()).thenReturn("abc12345");
        when(shortUrlRepository.existsByCode("abc12345")).thenReturn(false);
        when(shortUrlRepository.save(any(ShortUrl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShortUrl result = linkService.create(request, user);

        assertEquals("abc12345", result.getCode());
        assertEquals("https://example.com", result.getOriginalUrl());
        assertEquals(user, result.getOwner());
        assertTrue(result.isActive());
        assertEquals(0, result.getClickCount());
    }

    @Test
    void shouldRejectInvalidUrl() {
        User user = new User();
        CreateShortUrlRequest request =
                new CreateShortUrlRequest("notaurl", OffsetDateTime.now().plusDays(1));

        assertThrows(BadRequestException.class, () -> linkService.create(request, user));
    }

    @Test
    void shouldRejectPastExpirationOnCreate() {
        User user = new User();
        CreateShortUrlRequest request =
                new CreateShortUrlRequest("https://example.com", OffsetDateTime.now().minusDays(1));

        assertThrows(BadRequestException.class, () -> linkService.create(request, user));
    }

    @Test
    void shouldUpdateExpiration() {
        User user = new User();
        ShortUrl entity = new ShortUrl();
        entity.setId(1L);
        entity.setOwner(user);
        entity.setExpiresAt(OffsetDateTime.now().plusDays(1));

        when(shortUrlRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(entity));
        when(shortUrlRepository.save(entity)).thenReturn(entity);

        OffsetDateTime newDate = OffsetDateTime.now().plusDays(5);
        ShortUrl result = linkService.updateExpiration(1L, new UpdateExpirationRequest(newDate), user);

        assertEquals(newDate, result.getExpiresAt());
    }

    @Test
    void shouldUpdateShortLinkFields() {
        User user = new User();
        ShortUrl entity = new ShortUrl();
        entity.setId(1L);
        entity.setOwner(user);
        entity.setOriginalUrl("https://old.example.com");
        entity.setExpiresAt(OffsetDateTime.now().plusDays(1));
        entity.setActive(true);

        OffsetDateTime newExpiresAt = OffsetDateTime.now().plusDays(10);
        UpdateShortUrlRequest request = new UpdateShortUrlRequest(
                "https://new.example.com",
                newExpiresAt,
                false
        );

        when(shortUrlRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(entity));
        when(shortUrlRepository.save(entity)).thenReturn(entity);

        ShortUrl result = linkService.update(1L, request, user);

        assertEquals("https://new.example.com", result.getOriginalUrl());
        assertEquals(newExpiresAt, result.getExpiresAt());
        assertFalse(result.isActive());
    }

    @Test
    void shouldUpdateOnlyActiveFlag() {
        User user = new User();
        ShortUrl entity = new ShortUrl();
        entity.setId(1L);
        entity.setOwner(user);
        entity.setOriginalUrl("https://example.com");
        entity.setExpiresAt(OffsetDateTime.now().plusDays(3));
        entity.setActive(true);

        UpdateShortUrlRequest request = new UpdateShortUrlRequest(null, null, false);

        when(shortUrlRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(entity));
        when(shortUrlRepository.save(entity)).thenReturn(entity);

        ShortUrl result = linkService.update(1L, request, user);

        assertEquals("https://example.com", result.getOriginalUrl());
        assertFalse(result.isActive());
    }

    @Test
    void shouldRejectUpdateWhenNoFieldsProvided() {
        User user = new User();
        ShortUrl entity = new ShortUrl();
        entity.setId(1L);
        entity.setOwner(user);

        UpdateShortUrlRequest request = new UpdateShortUrlRequest(null, null, null);

        when(shortUrlRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(entity));

        assertThrows(BadRequestException.class, () -> linkService.update(1L, request, user));
        verify(shortUrlRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWithBlankOriginalUrl() {
        User user = new User();
        ShortUrl entity = new ShortUrl();
        entity.setId(1L);
        entity.setOwner(user);

        UpdateShortUrlRequest request = new UpdateShortUrlRequest("   ", null, null);

        when(shortUrlRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(entity));

        assertThrows(BadRequestException.class, () -> linkService.update(1L, request, user));
        verify(shortUrlRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWithInvalidOriginalUrl() {
        User user = new User();
        ShortUrl entity = new ShortUrl();
        entity.setId(1L);
        entity.setOwner(user);

        UpdateShortUrlRequest request = new UpdateShortUrlRequest("invalid-url", null, null);

        when(shortUrlRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(entity));

        assertThrows(BadRequestException.class, () -> linkService.update(1L, request, user));
        verify(shortUrlRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWithPastExpiration() {
        User user = new User();
        ShortUrl entity = new ShortUrl();
        entity.setId(1L);
        entity.setOwner(user);

        UpdateShortUrlRequest request = new UpdateShortUrlRequest(
                null,
                OffsetDateTime.now().minusHours(1),
                null
        );

        when(shortUrlRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(entity));

        assertThrows(BadRequestException.class, () -> linkService.update(1L, request, user));
        verify(shortUrlRepository, never()).save(any());
    }
}
