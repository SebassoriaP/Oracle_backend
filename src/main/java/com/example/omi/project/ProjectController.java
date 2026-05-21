package com.example.omi.project;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

  private final ProjectRepository repo;

  public ProjectController(ProjectRepository repo) {
    this.repo = repo;
  }

  @GetMapping
  public List<Map<String, Object>> getAll() {
    return repo.findAll();
  }

  @PostMapping
  public void create(@Valid @RequestBody CreateProjectRequest req) {
    repo.createProject(req);
  }

  @GetMapping("/{projectId}/members")
  public List<ProjectMemberDto> getMembers(@PathVariable Long projectId) {
    return repo.findMembers(projectId);
  }

  @PostMapping("/{projectId}/member")
  public void addMember(
      @PathVariable Long projectId, @Valid @RequestBody CreateProjectMemberRequest req) {

    if (!repo.userExists(req.getUserId())) {
      throw new IllegalArgumentException(
          "Cannot add member: User " + req.getUserId() + " not found");
    }

    if (repo.memberExists(projectId, req.getUserId())) {
      throw new IllegalArgumentException("User is already in this project");
    }

    repo.addMember(projectId, req);
  }

  @DeleteMapping("/{projectId}/members/{userId}")
  public void deleteMember(@PathVariable Long projectId, @PathVariable Long userId) {

    if (!repo.memberExists(projectId, userId)) {
      throw new IllegalArgumentException("User is not a member of this project");
    }

    repo.deleteMember(projectId, userId);
  }
}
