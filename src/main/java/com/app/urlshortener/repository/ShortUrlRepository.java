package com.app.urlshortener.repository;

import com.app.urlshortener.entity.ShortUrl;
import com.app.urlshortener.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = "owner")
    Optional<ShortUrl> findByCode(String code);

    @EntityGraph(attributePaths = "owner")
    Optional<ShortUrl> findByIdAndOwner(Long id, User owner);

    @EntityGraph(attributePaths = "owner")
    Page<ShortUrl> findAllByOwnerOrderByCreatedAtDesc(User owner, Pageable pageable);

    @EntityGraph(attributePaths = "owner")
    Page<ShortUrl> findAllByOwnerAndActiveTrueAndExpiresAtAfterOrderByCreatedAtDesc(User owner, OffsetDateTime now, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ShortUrl s SET s.clickCount = s.clickCount + 1 WHERE s.code = :code")
    void incrementClickCount(@Param("code") String code);
}