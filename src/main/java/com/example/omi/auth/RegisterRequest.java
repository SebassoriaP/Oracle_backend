package com.example.omi.auth;

public record RegisterRequest(
    String name,
    String email,
    String password,
    String workMode,
    Long roleId,
    Long managerId,
    String status,
    String chatId
) {}