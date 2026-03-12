package com.app.urlshortener.service;

import com.app.urlshortener.dto.link.CreateShortUrlRequest;
import com.app.urlshortener.dto.link.UpdateExpirationRequest;
import com.app.urlshortener.entity.ShortUrl;
import com.app.urlshortener.entity.User;
import com.app.urlshortener.exception.BadRequestException;
import com.app.urlshortener.repository.ShortUrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LinkServiceTest {

    @Mock private ShortUrlRepository shortUrlRepository;
    @Mock private CodeGenerator codeGenerator;
    @InjectMocks private LinkService linkService;

    @Test
    void shouldCreateShortLink() {
        User user = new User();
        user.setUsername("andrii");
        var request = new CreateShortUrlRequest("https://example.com", OffsetDateTime.now().plusDays(1));

        when(codeGenerator.generate()).thenReturn("abc12345");
        when(shortUrlRepository.existsByCode("abc12345")).thenReturn(false);
        when(shortUrlRepository.save(any(ShortUrl.class))).thenAnswer(inv -> inv.getArgument(0));

        ShortUrl result = linkService.create(request, user);

        assertEquals("abc12345", result.getCode());
        assertEquals("https://example.com", result.getOriginalUrl());
        assertEquals(user, result.getOwner());
    }

    @Test
    void shouldRejectInvalidUrl() {
        User user = new User();
        var request = new CreateShortUrlRequest("notaurl", OffsetDateTime.now().plusDays(1));
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
}
