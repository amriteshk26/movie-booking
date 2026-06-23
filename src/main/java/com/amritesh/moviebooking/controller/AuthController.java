package com.amritesh.moviebooking.controller;

import com.amritesh.moviebooking.dto.request.RegisterRequest;
import com.amritesh.moviebooking.dto.response.UserResponse;
import com.amritesh.moviebooking.mapper.UserMapper;
import com.amritesh.moviebooking.security.SecurityUtils;
import com.amritesh.moviebooking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SecurityUtils securityUtils;
    private final UserMapper userMapper;

    public AuthController(AuthService authService, SecurityUtils securityUtils, UserMapper userMapper) {
        this.authService = authService;
        this.securityUtils = securityUtils;
        this.userMapper = userMapper;
    }

    /** Public self-service registration (always CUSTOMER). */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /** Returns the currently authenticated user (verifies HTTP Basic credentials). */
    @GetMapping("/me")
    public UserResponse me() {
        return userMapper.toResponse(securityUtils.getCurrentUser().getUser());
    }
}
