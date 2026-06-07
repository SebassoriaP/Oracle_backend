package com.example.omi.issue;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public class CreateIssueRequest {

  @NotBlank(message = "title is required")
  private String title;

  private String description;

  @NotBlank(message = "type is required")
  @Pattern(regexp = "TASK|BUG|TRAINING", message = "type must be TASK, BUG, or TRAINING")
  private String type;

  @NotBlank(message = "status is required")
  @Pattern(
      regexp = "open|in_progress|closed",
      message = "status must be open, in_progress, or closed")
  private String status;

  @NotNull(message = "estimatedHours is required")
  @Min(value = 0, message = "estimatedHours must be >= 0")
  private Integer estimatedHours;

  @NotNull(message = "actualHours is required")
  @Min(value = 0, message = "actualHours must be >= 0")
  private Integer actualHours;

  @NotNull(message = "featureId is required")
  private Long featureId;

  @NotNull(message = "assigneeId is required")
  private Long assigneeId;

  @NotNull(message = "isVisible is required")
  private Boolean isVisible;

  private LocalDate dueDate;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Integer getEstimatedHours() {
    return estimatedHours;
  }

  public void setEstimatedHours(Integer estimatedHours) {
    this.estimatedHours = estimatedHours;
  }

  public Integer getActualHours() {
    return actualHours;
  }

  public void setActualHours(Integer actualHours) {
    this.actualHours = actualHours;
  }

  public Long getFeatureId() {
    return featureId;
  }

  public void setFeatureId(Long featureId) {
    this.featureId = featureId;
  }

  public Long getAssigneeId() {
    return assigneeId;
  }

  public void setAssigneeId(Long assigneeId) {
    this.assigneeId = assigneeId;
  }

  public Boolean getIsVisible() {
    return isVisible;
  }

  public void setIsVisible(Boolean isVisible) {
    this.isVisible = isVisible;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }
}
