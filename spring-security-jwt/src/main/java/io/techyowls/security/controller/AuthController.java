package io.techyowls.security.controller;

import io.techyowls.security.dto.AuthResponse;
import io.techyowls.security.dto.LoginRequest;
import io.techyowls.security.dto.RefreshRequest;
import io.techyowls.security.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authManager, JwtService jwtService,
                          UserDetailsService userDetailsService) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        var user = userDetailsService.loadUserByUsername(request.username());

        return new AuthResponse(
            jwtService.generateAccessToken(user),
            jwtService.generateRefreshToken(user)
        );
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest request) {
        String refreshToken = request.refreshToken();

        if (jwtService.isExpired(refreshToken)) {
            throw new RuntimeException("Refresh token expired");
        }

        String username = jwtService.extractUsername(refreshToken);
        var user = userDetailsService.loadUserByUsername(username);

        return new AuthResponse(
            jwtService.generateAccessToken(user),
            refreshToken
        );
    }
}
