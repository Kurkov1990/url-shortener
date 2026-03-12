package com.app.urlshortener.controller;

import com.app.urlshortener.controller.v1.AuthController;
import com.app.urlshortener.dto.auth.AuthResponse;
import com.app.urlshortener.dto.auth.LoginRequest;
import com.app.urlshortener.dto.auth.RegisterRequest;
import com.app.urlshortener.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private final AuthService authService = Mockito.mock(AuthService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldRegister() throws Exception {
        var controller = new AuthController(authService);
        var mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        Mockito.when(authService.register(Mockito.any(RegisterRequest.class)))
                .thenReturn(new AuthResponse("jwt", "Bearer", "andrii"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("andrii", "StrongPass1"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt"));
    }

    @Test
    void shouldLogin() throws Exception {
        var controller = new AuthController(authService);
        var mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        Mockito.when(authService.login(Mockito.any(LoginRequest.class)))
                .thenReturn(new AuthResponse("jwt", "Bearer", "andrii"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("andrii", "StrongPass1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("andrii"));
    }
}
