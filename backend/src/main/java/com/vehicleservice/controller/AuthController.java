package com.vehicleservice.controller;

import com.vehicleservice.dto.auth.AuthResponse;
import com.vehicleservice.dto.auth.LoginRequest;
import com.vehicleservice.dto.auth.RegisterRequest;
import com.vehicleservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Public self-registration - creates a CUSTOMER account. */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.registerCustomer(req));
    }

    /** Works for Customer, Mechanic and Admin logins alike. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}
