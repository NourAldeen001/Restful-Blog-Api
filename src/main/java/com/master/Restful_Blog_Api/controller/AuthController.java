package com.master.Restful_Blog_Api.controller;

import com.master.Restful_Blog_Api.dto.AuthResponse;
import com.master.Restful_Blog_Api.dto.LoginRequest;
import com.master.Restful_Blog_Api.dto.RegisterRequest;
import com.master.Restful_Blog_Api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request received: username={}, email={}", request.getUsername(), request.getEmail());
        AuthResponse response = authService.register(request);
        log.info("User registered successfully: username={}, email={}", response.getUsername(), response.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt: email={}", request.getEmail());
        AuthResponse response = authService.login(request);
        log.info("Login successfully: email={}, userId={}, role={}", response.getEmail(), response.getUserId(), response.getRole());
        return ResponseEntity.ok(response);
    }
}
