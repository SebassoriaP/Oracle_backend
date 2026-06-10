package com.example.omi.role;

public class RoleDto {

  private final Long id;
  private final String name;

  public RoleDto(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}