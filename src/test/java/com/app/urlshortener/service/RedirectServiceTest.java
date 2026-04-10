package com.app.urlshortener.service;

import com.app.urlshortener.entity.ShortUrl;
import com.app.urlshortener.exception.ExpiredLinkException;
import com.app.urlshortener.exception.NotFoundException;
import com.app.urlshortener.repository.ShortUrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class RedirectServiceTest {

    @Mock private ShortUrlRepository shortUrlRepository;
    @InjectMocks private RedirectService redirectService;

    @Test
    void shouldResolveOriginalUrlAndIncrementCounter() {
        ShortUrl entity = new ShortUrl();
        entity.setCode("abc12345");
        entity.setOriginalUrl("https://example.com");
        entity.setActive(true);
        entity.setExpiresAt(OffsetDateTime.now().plusDays(1));
        entity.setClickCount(2);

        when(shortUrlRepository.findByCode("abc12345")).thenReturn(Optional.of(entity));

        String result = redirectService.resolveOriginalUrl("abc12345");

        assertEquals("https://example.com", result);
        verify(shortUrlRepository).incrementClickCount("abc12345");
        verify(shortUrlRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenExpired() {
        ShortUrl entity = new ShortUrl();
        entity.setCode("abc12345");
        entity.setOriginalUrl("https://example.com");
        entity.setActive(true);
        entity.setExpiresAt(OffsetDateTime.now().minusDays(1));

        when(shortUrlRepository.findByCode("abc12345")).thenReturn(Optional.of(entity));
        assertThrows(ExpiredLinkException.class, () -> redirectService.resolveOriginalUrl("abc12345"));
    }

    @Test
    void shouldThrowWhenNotFound() {
        when(shortUrlRepository.findByCode("missing")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> redirectService.resolveOriginalUrl("missing"));
    }
}
