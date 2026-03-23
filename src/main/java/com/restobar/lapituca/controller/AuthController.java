package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.auth.*;
import com.restobar.lapituca.dto.request.UsuarioClienteRequest;
import com.restobar.lapituca.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody UsuarioClienteRequest request) {
        return ResponseEntity.ok(authService.registerClienteLocal(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.requestPasswordReset(request));
    }

    @PostMapping("/verify-reset-token")
    public ResponseEntity<VerifyResetTokenResponse> verifyResetToken(@Valid @RequestBody VerifyResetTokenRequest request) {
        return ResponseEntity.ok(authService.verifyResetToken(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.getAuthenticatedUser(authentication.getName()));
    }
}