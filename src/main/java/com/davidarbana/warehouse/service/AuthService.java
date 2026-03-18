package com.davidarbana.warehouse.service;

import com.davidarbana.warehouse.dto.request.AuthRequest;
import com.davidarbana.warehouse.dto.response.ResponseDtos;

public interface AuthService {
    ResponseDtos.AuthResponse login(AuthRequest.Login request);
    ResponseDtos.AuthResponse register(AuthRequest.Register request);
}
