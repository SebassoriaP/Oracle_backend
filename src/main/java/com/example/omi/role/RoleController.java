package com.example.omi.role;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

  private final RoleRepository repo;

  public RoleController(RoleRepository repo) {
    this.repo = repo;
  }

  @GetMapping
  public List<RoleDto> getAll() {
    return repo.findAll();
  }

  @PostMapping
  public void create(@Valid @RequestBody CreateRoleRequest req) {
    repo.create(req);
  }

  @DeleteMapping("/{roleId}")
  public void delete(@PathVariable Long roleId) {
    repo.delete(roleId);
  }
}