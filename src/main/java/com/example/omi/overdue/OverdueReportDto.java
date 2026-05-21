package com.example.omi.overdue;

import java.time.LocalDateTime;

public record OverdueReportDto(
    Long id,
    Long issueId,
    String taskTitle,
    String developerName,
    LocalDateTime dueDate,
    LocalDateTime submittedAt,
    String reason,
    String aiSummary,
    String aiCategory,
    String severity,
    Integer delayDays,
    String impactLevel,
    String description,
    String recommendation
) {}