package com.ticketbook.service;

import com.ticketbook.dto.LoginRequest;
import com.ticketbook.dto.LoginResponse;
import com.ticketbook.dto.RegisterRequest;
import com.ticketbook.dto.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}
