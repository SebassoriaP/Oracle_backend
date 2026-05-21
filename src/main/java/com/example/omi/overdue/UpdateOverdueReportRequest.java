package com.example.omi.overdue;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class UpdateOverdueReportRequest {

  @NotNull
  private Long issueId;

  private String taskTitle;
  private String developerName;

  private LocalDateTime dueDate;

  private String reason;

  private String aiSummary;
  private String aiCategory;
  private String severity;

  private Integer delayDays;
  private String impactLevel;
  private String description;

  private String recommendation;

  public Long getIssueId() {
    return issueId;
  }

  public void setIssueId(Long issueId) {
    this.issueId = issueId;
  }

  public String getTaskTitle() {
    return taskTitle;
  }

  public void setTaskTitle(String taskTitle) {
    this.taskTitle = taskTitle;
  }

  public String getDeveloperName() {
    return developerName;
  }

  public void setDeveloperName(String developerName) {
    this.developerName = developerName;
  }

  public LocalDateTime getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDateTime dueDate) {
    this.dueDate = dueDate;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getAiSummary() {
    return aiSummary;
  }

  public void setAiSummary(String aiSummary) {
    this.aiSummary = aiSummary;
  }

  public String getAiCategory() {
    return aiCategory;
  }

  public void setAiCategory(String aiCategory) {
    this.aiCategory = aiCategory;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public Integer getDelayDays() {
    return delayDays;
  }

  public void setDelayDays(Integer delayDays) {
    this.delayDays = delayDays;
  }

  public String getImpactLevel() {
    return impactLevel;
  }

  public void setImpactLevel(String impactLevel) {
    this.impactLevel = impactLevel;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getRecommendation() {
    return recommendation;
  }

  public void setRecommendation(String recommendation) {
    this.recommendation = recommendation;
  }
}