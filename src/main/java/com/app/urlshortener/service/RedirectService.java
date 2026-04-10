package com.app.urlshortener.service;

import com.app.urlshortener.entity.ShortUrl;
import com.app.urlshortener.exception.ExpiredLinkException;
import com.app.urlshortener.exception.NotFoundException;
import com.app.urlshortener.repository.ShortUrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RedirectService {

    private final ShortUrlRepository shortUrlRepository;

    public RedirectService(ShortUrlRepository shortUrlRepository) {
        this.shortUrlRepository = shortUrlRepository;
    }

    @Transactional
    public String resolveOriginalUrl(String code) {
        ShortUrl entity = shortUrlRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Short link not found"));
        if (!entity.isActive() || entity.isExpired()) {
            throw new ExpiredLinkException("Short link is inactive or expired");
        }
        shortUrlRepository.incrementClickCount(code);
        return entity.getOriginalUrl();
    }
}
