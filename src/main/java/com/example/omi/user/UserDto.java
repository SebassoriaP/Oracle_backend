package com.example.omi.user;

import java.time.OffsetDateTime;

public record UserDto(
    Long id,
    String name,
    String email,
    String passwordHash,
    String workMode,
    Long roleId,
    Long managerId,
    OffsetDateTime createdAt,
    String status,
    String chatId) {}
