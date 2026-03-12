package com.app.urlshortener.service;

import com.app.urlshortener.dto.auth.LoginRequest;
import com.app.urlshortener.dto.auth.RegisterRequest;
import com.app.urlshortener.entity.User;
import com.app.urlshortener.exception.ConflictException;
import com.app.urlshortener.exception.UnauthorizedException;
import com.app.urlshortener.repository.UserRepository;
import com.app.urlshortener.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @InjectMocks private AuthService authService;

    @Test
    void shouldRegisterNewUser() {
        RegisterRequest request = new RegisterRequest("andrii", "StrongPass1");
        when(userRepository.existsByUsernameIgnoreCase("andrii")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass1")).thenReturn("encoded");
        when(jwtService.generateToken("andrii")).thenReturn("jwt-token");

        var response = authService.register(request);

        assertEquals("jwt-token", response.token());
        assertEquals("andrii", response.username());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowWhenUsernameExists() {
        when(userRepository.existsByUsernameIgnoreCase("andrii")).thenReturn(true);
        assertThrows(ConflictException.class, () -> authService.register(new RegisterRequest("andrii", "StrongPass1")));
    }

    @Test
    void shouldLoginUser() {
        User user = new User();
        user.setUsername("andrii");
        user.setPasswordHash("encoded");

        when(userRepository.findByUsernameIgnoreCase("andrii")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("StrongPass1", "encoded")).thenReturn(true);
        when(jwtService.generateToken("andrii")).thenReturn("jwt-token");

        var response = authService.login(new LoginRequest("andrii", "StrongPass1"));

        assertEquals("jwt-token", response.token());
        assertEquals("andrii", response.username());
    }

    @Test
    void shouldRejectInvalidPassword() {
        User user = new User();
        user.setUsername("andrii");
        user.setPasswordHash("encoded");

        when(userRepository.findByUsernameIgnoreCase("andrii")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(new LoginRequest("andrii", "wrong")));
    }
}
