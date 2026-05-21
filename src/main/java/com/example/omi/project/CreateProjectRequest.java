package com.example.omi.project;

import jakarta.validation.constraints.NotBlank;

public class CreateProjectRequest {

  @NotBlank
  private String name;

  private String description;

  private String status;

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getStatus() {
    return status;
  }
}