package com.app.urlshortener.repository;

import com.app.urlshortener.entity.ShortUrl;
import com.app.urlshortener.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = "owner")
    Optional<ShortUrl> findByCode(String code);

    @EntityGraph(attributePaths = "owner")
    Optional<ShortUrl> findByIdAndOwner(Long id, User owner);

    @EntityGraph(attributePaths = "owner")
    List<ShortUrl> findAllByOwnerOrderByCreatedAtDesc(User owner);

    @EntityGraph(attributePaths = "owner")
    List<ShortUrl> findAllByOwnerAndActiveTrueAndExpiresAtAfterOrderByCreatedAtDesc(User owner, OffsetDateTime now);
}