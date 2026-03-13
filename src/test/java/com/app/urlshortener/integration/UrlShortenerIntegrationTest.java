package com.app.urlshortener.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class UrlShortenerIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void fullFlowShouldWork() throws Exception {
        String registerJson = """
                {
                  "username": "andrii",
                  "password": "StrongPass1"
                }
                """;

        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("andrii"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode registerNode = objectMapper.readTree(registerResponse);
        String token = registerNode.get("token").asText();

        String createBody = objectMapper.writeValueAsString(Map.of(
                "originalUrl", "https://example.com/very/long/url",
                "expiresAt", OffsetDateTime.now().plusDays(2).toString()
        ));

        String createResponse = mockMvc.perform(post("/api/v1/links")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.shortCode").exists())
                .andExpect(jsonPath("$.shortUrl",
                        startsWith("http://localhost:8080/api/v1/redirect/")))
                .andExpect(jsonPath("$.originalUrl").value("https://example.com/very/long/url"))
                .andExpect(jsonPath("$.clickCount").value(0))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.ownerUsername").value("andrii"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createNode = objectMapper.readTree(createResponse);
        String code = createNode.get("shortCode").asText();
        long id = createNode.get("id").asLong();

        mockMvc.perform(get("/api/v1/links")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].shortCode").value(code));

        mockMvc.perform(get("/api/v1/links/" + id + "/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.shortCode").value(code))
                .andExpect(jsonPath("$.clickCount").value(0));

        String updatedUrl = "https://updated.example.com/path";
        OffsetDateTime updatedExpiresAt = OffsetDateTime.now().plusDays(5);

        String updateBody = objectMapper.writeValueAsString(Map.of(
                "originalUrl", updatedUrl,
                "expiresAt", updatedExpiresAt.toString(),
                "active", false
        ));

        mockMvc.perform(put("/api/v1/links/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.shortCode").value(code))
                .andExpect(jsonPath("$.originalUrl").value(updatedUrl))
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/api/v1/links/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.shortCode").value(code))
                .andExpect(jsonPath("$.originalUrl").value(updatedUrl))
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/api/v1/redirect/" + code))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", updatedUrl));

        mockMvc.perform(get("/api/v1/links/" + id + "/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value(code))
                .andExpect(jsonPath("$.clickCount").value(1));

        mockMvc.perform(delete("/api/v1/links/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}
