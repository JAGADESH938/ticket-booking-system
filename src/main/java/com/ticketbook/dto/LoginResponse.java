package com.ticketbook.dto;

import lombok.Getter;

@Getter
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String name;
    private String email;
    private String role;

    public LoginResponse(String token, Long userId, String name, String email, String role) {
        this.token = token;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
