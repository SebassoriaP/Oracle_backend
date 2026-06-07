package com.example.omi.issue;

import java.sql.Timestamp;
import java.time.LocalDate;

public record IssueDto(
    Long id,
    Long projectId,
    Long sprintId,
    Long featureId,
    String title,
    String description,
    String status,
    String type,
    Long assigneeId,
    Timestamp createdAt,
    Timestamp updatedAt,
    Integer estimatedHours,
    Integer actualHours,
    Boolean isVisible,
    LocalDate dueDate) {}
