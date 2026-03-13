package com.app.urlshortener.controller.v1;

import com.app.urlshortener.dto.link.*;
import com.app.urlshortener.entity.User;
import com.app.urlshortener.mapper.ShortUrlMapper;
import com.app.urlshortener.service.LinkService;
import com.app.urlshortener.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/links")
public class LinkController {

    private final LinkService linkService;
    private final UserService userService;
    private final ShortUrlMapper shortUrlMapper;

    public LinkController(LinkService linkService, UserService userService, ShortUrlMapper shortUrlMapper) {
        this.linkService = linkService;
        this.userService = userService;
        this.shortUrlMapper = shortUrlMapper;
    }

    @Operation(summary = "Create a new short URL")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShortUrlResponse create(@Valid @RequestBody CreateShortUrlRequest request,
                                   @AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByUsername(principal.getUsername());
        return shortUrlMapper.toResponse(linkService.create(request, user));
    }

    @Operation(summary = "Get all user's short links")
    @GetMapping
    public List<ShortUrlResponse> getAll(@RequestParam(defaultValue = "false") boolean activeOnly,
                                         @AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByUsername(principal.getUsername());
        return linkService.findAll(user, activeOnly)
                .stream()
                .map(shortUrlMapper::toResponse)
                .toList();
    }

    @Operation(summary = "Get one short link by id")
    @GetMapping("/{id}")
    public ShortUrlResponse getOne(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByUsername(principal.getUsername());
        return shortUrlMapper.toResponse(linkService.findOwnedById(id, user));
    }

    @Operation(summary = "Update short link")
    @PutMapping("/{id}")
    public ShortUrlResponse update(@PathVariable Long id,
                                   @Valid @RequestBody UpdateShortUrlRequest request,
                                   @AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByUsername(principal.getUsername());
        return shortUrlMapper.toResponse(linkService.update(id, request, user));
    }

    @Operation(summary = "Update link expiration")
    @PutMapping("/{id}/expiration")
    public ShortUrlResponse updateExpiration(@PathVariable Long id,
                                             @Valid @RequestBody UpdateExpirationRequest request,
                                             @AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByUsername(principal.getUsername());
        return shortUrlMapper.toResponse(linkService.updateExpiration(id, request, user));
    }

    @Operation(summary = "Get statistics for one short link")
    @GetMapping("/{id}/stats")
    public LinkStatsResponse getStats(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByUsername(principal.getUsername());
        return shortUrlMapper.toStats(linkService.findOwnedById(id, user));
    }

    @Operation(summary = "Delete short link")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByUsername(principal.getUsername());
        linkService.delete(id, user);
    }
}
