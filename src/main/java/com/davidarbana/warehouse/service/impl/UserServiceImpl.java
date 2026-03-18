package com.davidarbana.warehouse.service.impl;

import com.davidarbana.warehouse.dto.request.AuthRequest;
import com.davidarbana.warehouse.dto.response.ResponseDtos;
import com.davidarbana.warehouse.entity.User;
import com.davidarbana.warehouse.enums.Role;
import com.davidarbana.warehouse.exception.InvalidOperationException;
import com.davidarbana.warehouse.exception.ResourceNotFoundException;
import com.davidarbana.warehouse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<ResponseDtos.UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ResponseDtos.UserResponse getUserById(Long id) {
        return mapToResponse(findById(id));
    }

    public ResponseDtos.UserResponse createUser(AuthRequest.Register request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new InvalidOperationException("Username already taken");
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
        return mapToResponse(user);
    }

    public ResponseDtos.UserResponse updateUser(Long id, AuthRequest.Register request) {
        User user = findById(id);

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        }

        userRepository.save(user);
        return mapToResponse(user);
    }

    public void deleteUser(Long id) {
        userRepository.delete(findById(id));
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private ResponseDtos.UserResponse mapToResponse(User user) {
        return ResponseDtos.UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .build();
    }
}
