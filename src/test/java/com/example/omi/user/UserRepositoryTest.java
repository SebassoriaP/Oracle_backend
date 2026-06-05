package com.example.omi.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

  @Mock private JdbcTemplate jdbc;

  @InjectMocks private UserRepository repo;

  @Test
  void findAll_mapsUsers() {
    when(jdbc.query(anyString(), any(RowMapper.class)))
        .thenReturn(
            List.of(
                new UserDto(
                    1L,
                    "Ana",
                    "ana@mail.com",
                    "remote",
                    10L,
                    null,
                    OffsetDateTime.parse("2026-06-01T10:00:00Z"),
                    "active",
                    null)));

    assertThat(repo.findAll()).hasSize(1);
  }

  @Test
  void roleExists_userExists_emailExists_handleCounts() {
    when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(1L))).thenReturn(1);
    assertThat(repo.roleExists(1L)).isTrue();

    when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(101L))).thenReturn(0);
    assertThat(repo.userExists(101L)).isFalse();

    when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("ana@mail.com"))).thenReturn(1);
    assertThat(repo.emailExists("ana@mail.com")).isTrue();
  }

  @Test
  void create_writesRow() {
    CreateUserRequest req = new CreateUserRequest();
    req.setName("User");
    req.setEmail("user@mail.com");
    req.setPasswordHash("hash");
    req.setWorkMode("remote");
    req.setRoleId(10L);
    req.setManagerId(77L);
    req.setStatus("active");

    repo.create(req);

    verify(jdbc)
        .update(
            anyString(),
            eq("User"),
            eq("user@mail.com"),
            eq("hash"),
            eq("remote"),
            eq(10L),
            eq(77L),
            eq("active"));
  }

  @Test
  void delete_throwsWhenMissing() {
    when(jdbc.update(anyString(), eq(103L))).thenReturn(0);

    assertThatThrownBy(() -> repo.delete(103L)).isInstanceOf(EmptyResultDataAccessException.class);
  }
}
