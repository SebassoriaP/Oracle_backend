package com.example.omi.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock private UserRepository repo;

  @InjectMocks private UserController controller;

  @Test
  void getAll_delegatesToRepository() {
    when(repo.findAll())
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

    List<UserDto> result = controller.getAll();

    assertThat(result).hasSize(1);
    verify(repo).findAll();
  }

  @Test
  void create_rejectsUnknownRole() {
    CreateUserRequest req = new CreateUserRequest();
    req.setName("User");
    req.setEmail("user@mail.com");
    req.setRoleId(99L);

    when(repo.roleExists(99L)).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> controller.create(req));
  }

  @Test
  void create_rejectsUnknownManager() {
    CreateUserRequest req = new CreateUserRequest();
    req.setName("User");
    req.setEmail("user@mail.com");
    req.setRoleId(10L);
    req.setManagerId(77L);

    when(repo.roleExists(10L)).thenReturn(true);
    when(repo.userExists(77L)).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> controller.create(req));
  }

  @Test
  void create_rejectsDuplicateEmail() {
    CreateUserRequest req = new CreateUserRequest();
    req.setName("User");
    req.setEmail("user@mail.com");
    req.setRoleId(10L);

    when(repo.roleExists(10L)).thenReturn(true);
    when(repo.emailExists("user@mail.com")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> controller.create(req));
  }

  @Test
  void create_acceptsValidUser() {
    CreateUserRequest req = new CreateUserRequest();
    req.setName("User");
    req.setEmail("user@mail.com");
    req.setPasswordHash("hash");
    req.setWorkMode("remote");
    req.setRoleId(10L);
    req.setManagerId(77L);
    req.setStatus("active");

    when(repo.roleExists(10L)).thenReturn(true);
    when(repo.userExists(77L)).thenReturn(true);
    when(repo.emailExists("user@mail.com")).thenReturn(false);

    controller.create(req);

    verify(repo).create(req);
  }

  @Test
  void delete_delegatesToRepository() {
    controller.delete(103L);

    verify(repo).delete(103L);
  }
}
