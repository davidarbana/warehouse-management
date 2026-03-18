package com.davidarbana.warehouse.service.impl;

import com.davidarbana.warehouse.dto.request.AuthRequest;
import com.davidarbana.warehouse.dto.response.ResponseDtos;
import com.davidarbana.warehouse.entity.User;
import com.davidarbana.warehouse.enums.Role;
import com.davidarbana.warehouse.exception.InvalidOperationException;
import com.davidarbana.warehouse.repository.UserRepository;
import com.davidarbana.warehouse.security.JwtService;
import com.davidarbana.warehouse.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public ResponseDtos.AuthResponse login(AuthRequest.Login request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidOperationException("User not found"));

        log.info("User logged in: {}", user.getUsername());

        return ResponseDtos.AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public ResponseDtos.AuthResponse register(AuthRequest.Register request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new InvalidOperationException("Username already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .enabled(true)
                .build();

        userRepository.save(user);

        return ResponseDtos.AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}
