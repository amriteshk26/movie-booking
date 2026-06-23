package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.dto.request.RegisterRequest;
import com.amritesh.moviebooking.dto.response.UserResponse;
import com.amritesh.moviebooking.entity.User;
import com.amritesh.moviebooking.entity.enums.Role;
import com.amritesh.moviebooking.exception.BadRequestException;
import com.amritesh.moviebooking.mapper.UserMapper;
import com.amritesh.moviebooking.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Handles self-service registration. New accounts always receive the CUSTOMER
 * role; admin accounts are provisioned via seed data.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already registered: " + request.email());
        }
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(Role.CUSTOMER));
        return userMapper.toResponse(userRepository.save(user));
    }
}
