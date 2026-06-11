package com.example.omi.user;

import java.sql.Timestamp;

public record UserDto(
    Long id,
    String name,
    String email,
    String passwordHash,
    String workMode,
    Long roleId,
    Long managerId,
    Timestamp createdAt,
    String status,
    String chatId) {}
