package com.example.omi.role;

import jakarta.validation.constraints.NotBlank;

public class CreateRoleRequest {

  @NotBlank
  private String name;

  public String getName() {
    return name;
  }
}