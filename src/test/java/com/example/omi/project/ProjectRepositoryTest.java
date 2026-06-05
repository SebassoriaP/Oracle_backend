package com.example.omi.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class ProjectRepositoryTest {

  @Mock private JdbcTemplate jdbcTemplate;

  @InjectMocks private ProjectRepository repo;

  @Test
  void findAll_returnsRows() {
    when(jdbcTemplate.queryForList("SELECT * FROM project ORDER BY id"))
        .thenReturn(List.of(Map.of("id", 1L, "name", "Project A")));

    assertThat(repo.findAll()).hasSize(1);
  }

  @Test
  void findMembers_mapsResults() {
    when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(1L)))
        .thenReturn(List.of(new ProjectMemberDto(101L, "Ana", "DEV")));

    assertThat(repo.findMembers(1L)).containsExactly(new ProjectMemberDto(101L, "Ana", "DEV"));
  }

  @Test
  void addMember_writesRow() {
    CreateProjectMemberRequest req = new CreateProjectMemberRequest();
    req.setUserId(101L);
    req.setRole("DEV");

    repo.addMember(1L, req);

    verify(jdbcTemplate).update(anyString(), eq(1L), eq(101L), eq("DEV"));
  }

  @Test
  void userExists_handlesCount() {
    when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(101L))).thenReturn(1);
    assertThat(repo.userExists(101L)).isTrue();

    when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(102L))).thenReturn(0);
    assertThat(repo.userExists(102L)).isFalse();
  }

  @Test
  void memberExists_handlesCount() {
    when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(1L), eq(101L)))
        .thenReturn(1);
    assertThat(repo.memberExists(1L, 101L)).isTrue();

    when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(1L), eq(102L)))
        .thenReturn(0);
    assertThat(repo.memberExists(1L, 102L)).isFalse();
  }

  @Test
  void deleteMember_throwsWhenMissing() {
    when(jdbcTemplate.update(anyString(), eq(1L), eq(101L))).thenReturn(0);

    assertThatThrownBy(() -> repo.deleteMember(1L, 101L))
        .isInstanceOf(EmptyResultDataAccessException.class);
  }

  @Test
  void createProject_callsJdbcUpdate() {
    CreateProjectRequest req = new CreateProjectRequest();

    repo.createProject(req);

    verify(jdbcTemplate).update(anyString(), any(), any(), any());
  }
}
