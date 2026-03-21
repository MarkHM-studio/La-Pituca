package com.restobar.lapituca.controller;

import com.restobar.lapituca.dto.auth.LoginRequest;
import com.restobar.lapituca.dto.auth.LoginResponse;
import com.restobar.lapituca.dto.request.UsuarioClienteRequest;
import com.restobar.lapituca.dto.response.ClienteResponse;
import com.restobar.lapituca.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ClienteResponse> register(@Valid @RequestBody UsuarioClienteRequest request) {
        return ResponseEntity.ok(authService.registerClienteLocal(request));
    }
}
