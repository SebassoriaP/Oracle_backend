package com.example.omi.role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

  @Mock private RoleRepository repo;

  @InjectMocks private RoleController controller;

  @Test
  void getAll_delegatesToRepository() {
    when(repo.findAll()).thenReturn(List.of(new RoleDto(1L, "DEV")));

    List<RoleDto> result = controller.getAll();

    assertThat(result).hasSize(1);
    verify(repo).findAll();
  }

  @Test
  void create_delegatesToRepository() {
    CreateRoleRequest req = new CreateRoleRequest();

    controller.create(req);

    verify(repo).create(req);
  }

  @Test
  void delete_delegatesToRepository() {
    controller.delete(5L);

    verify(repo).delete(5L);
  }
}
