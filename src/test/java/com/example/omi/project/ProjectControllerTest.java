package com.example.omi.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

  @Mock private ProjectRepository repo;

  @InjectMocks private ProjectController controller;

  @Test
  void getAll_delegatesToRepository() {
    when(repo.findAll()).thenReturn(List.of(Map.of("id", 1L, "name", "Project A")));

    List<Map<String, Object>> result = controller.getAll();

    assertThat(result).hasSize(1);
    verify(repo).findAll();
  }

  @Test
  void create_delegatesToRepository() {
    CreateProjectRequest req = new CreateProjectRequest();

    controller.create(req);

    verify(repo).createProject(req);
  }

  @Test
  void getMembers_delegatesToRepository() {
    when(repo.findMembers(1L)).thenReturn(List.of());

    List<ProjectMemberDto> result = controller.getMembers(1L);

    assertThat(result).isEmpty();
    verify(repo).findMembers(1L);
  }

  @Test
  void addMember_rejectsMissingUser() {
    CreateProjectMemberRequest req = new CreateProjectMemberRequest();
    req.setUserId(101L);
    req.setRole("DEV");

    when(repo.userExists(101L)).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> controller.addMember(1L, req));
  }

  @Test
  void addMember_rejectsDuplicateMember() {
    CreateProjectMemberRequest req = new CreateProjectMemberRequest();
    req.setUserId(101L);
    req.setRole("DEV");

    when(repo.userExists(101L)).thenReturn(true);
    when(repo.memberExists(1L, 101L)).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> controller.addMember(1L, req));
  }

  @Test
  void addMember_acceptsValidMember() {
    CreateProjectMemberRequest req = new CreateProjectMemberRequest();
    req.setUserId(101L);
    req.setRole("DEV");

    when(repo.userExists(101L)).thenReturn(true);
    when(repo.memberExists(1L, 101L)).thenReturn(false);

    controller.addMember(1L, req);

    verify(repo).addMember(1L, req);
  }

  @Test
  void deleteMember_rejectsMissingMember() {
    when(repo.memberExists(1L, 101L)).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> controller.deleteMember(1L, 101L));
  }

  @Test
  void deleteMember_delegatesToRepository() {
    when(repo.memberExists(1L, 101L)).thenReturn(true);

    controller.deleteMember(1L, 101L);

    verify(repo).deleteMember(1L, 101L);
  }
}
