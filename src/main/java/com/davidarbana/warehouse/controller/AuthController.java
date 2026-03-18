package com.davidarbana.warehouse.controller;

import com.davidarbana.warehouse.dto.request.AuthRequest;
import com.davidarbana.warehouse.dto.response.ResponseDtos;
import com.davidarbana.warehouse.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and registration endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login", description = "Authenticate and receive a JWT token")
    @PostMapping("/login")
    public ResponseEntity<ResponseDtos.AuthResponse> login(@Valid @RequestBody AuthRequest.Login request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Register", description = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<ResponseDtos.AuthResponse> register(@Valid @RequestBody AuthRequest.Register request) {
        return ResponseEntity.ok(authService.register(request));
    }
}
