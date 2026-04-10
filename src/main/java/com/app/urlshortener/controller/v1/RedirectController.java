package com.app.urlshortener.controller.v1;

import com.app.urlshortener.service.RedirectService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/redirect")
public class RedirectController {

    private final RedirectService redirectService;

    public RedirectController(RedirectService redirectService) { this.redirectService = redirectService; }

    @Operation(summary = "Redirect by short code and increment statistics")
    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String originalUrl = redirectService.resolveOriginalUrl(code);
        return ResponseEntity.status(301).header(HttpHeaders.LOCATION, URI.create(originalUrl).toString()).build();
    }
}
