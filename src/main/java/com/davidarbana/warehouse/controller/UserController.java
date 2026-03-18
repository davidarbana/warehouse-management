package com.davidarbana.warehouse.controller;

import com.davidarbana.warehouse.dto.request.AuthRequest;
import com.davidarbana.warehouse.dto.response.ResponseDtos;
import com.davidarbana.warehouse.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@Tag(name = "User Management")
public class UserController {

    private final UserServiceImpl userService;

    @GetMapping
    public ResponseEntity<List<ResponseDtos.UserResponse>> getAll() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDtos.UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<ResponseDtos.UserResponse> create(@Valid @RequestBody AuthRequest.Register request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDtos.UserResponse> update(@PathVariable Long id,
                                                             @RequestBody AuthRequest.Register request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
