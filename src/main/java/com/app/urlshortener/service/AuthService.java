package com.app.urlshortener.service;

import com.app.urlshortener.dto.auth.AuthResponse;
import com.app.urlshortener.dto.auth.LoginRequest;
import com.app.urlshortener.dto.auth.RegisterRequest;
import com.app.urlshortener.entity.User;
import com.app.urlshortener.exception.ConflictException;
import com.app.urlshortener.exception.UnauthorizedException;
import com.app.urlshortener.repository.UserRepository;
import com.app.urlshortener.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ConflictException("Username is already taken");
        }
        User user = new User();
        user.setUsername(request.username().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(user.getUsername()), "Bearer", user.getUsername());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(request.username())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }
        return new AuthResponse(jwtService.generateToken(user.getUsername()), "Bearer", user.getUsername());
    }
}
