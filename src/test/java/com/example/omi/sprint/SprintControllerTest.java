package com.example.omi.sprint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SprintControllerTest {

  @Mock private SprintRepository repo;

  @InjectMocks private SprintController controller;

  @Test
  void getAll_delegatesToRepository() {
    when(repo.findByProject(1L)).thenReturn(List.of());

    List<SprintDto> result = controller.getAll(1L);

    assertThat(result).isEmpty();
    verify(repo).findByProject(1L);
  }

  @Test
  void create_rejectsEndBeforeStart() {
    CreateSprintRequest req = new CreateSprintRequest();
    req.setName("Sprint");
    req.setStartDate(LocalDate.of(2026, 5, 10));
    req.setEndDate(LocalDate.of(2026, 5, 1));
    req.setStatus("active");

    assertThrows(IllegalArgumentException.class, () -> controller.create(1L, req));
  }

  @Test
  void create_acceptsValidRange() {
    CreateSprintRequest req = new CreateSprintRequest();
    req.setName("Sprint");
    req.setStartDate(LocalDate.of(2026, 5, 1));
    req.setEndDate(LocalDate.of(2026, 5, 10));
    req.setGoal("Goal");
    req.setStatus("active");

    controller.create(1L, req);

    verify(repo).create(1L, req);
  }

  @Test
  void delete_delegatesToRepository() {
    controller.delete(1L, 9L);

    verify(repo).delete(1L, 9L);
  }
}
