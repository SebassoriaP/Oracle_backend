package com.example.omi.overdue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OverdueReportControllerTest {

  @Mock private OverdueReportRepository repo;

  @InjectMocks private OverdueReportController controller;

  @Test
  void getAll_delegatesToRepository() {
    when(repo.findAll()).thenReturn(List.of());

    List<OverdueReportDto> result = controller.getAll();

    assertThat(result).isEmpty();
    verify(repo).findAll();
  }

  @Test
  void getById_delegatesToRepository() {
    OverdueReportDto dto =
        new OverdueReportDto(
            1L,
            10L,
            "Task",
            "Dev",
            LocalDateTime.parse("2026-06-01T10:00:00"),
            LocalDateTime.parse("2026-06-01T12:00:00"),
            "reason",
            "summary",
            "category",
            "high",
            3,
            "high",
            "desc",
            "rec");

    when(repo.findById(1L)).thenReturn(dto);

    OverdueReportDto result = controller.getById(1L);

    assertThat(result).isEqualTo(dto);
    verify(repo).findById(1L);
  }

  @Test
  void create_rejectsMissingIssue() {
    CreateOverdueReportRequest req = new CreateOverdueReportRequest();
    req.setIssueId(10L);
    req.setTaskTitle("Task");
    req.setDeveloperName("Dev");
    req.setDueDate(LocalDateTime.parse("2026-06-01T10:00:00"));
    req.setReason("reason");

    when(repo.issueExists(10L)).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> controller.create(req));
  }

  @Test
  void create_acceptsValidRequest() {
    CreateOverdueReportRequest req = new CreateOverdueReportRequest();
    req.setIssueId(10L);
    req.setTaskTitle("Task");
    req.setDeveloperName("Dev");
    req.setDueDate(LocalDateTime.parse("2026-06-01T10:00:00"));
    req.setReason("reason");

    when(repo.issueExists(10L)).thenReturn(true);

    controller.create(req);

    verify(repo).create(req);
  }

  @Test
  void update_rejectsMissingIssue() {
    UpdateOverdueReportRequest req = new UpdateOverdueReportRequest();
    req.setIssueId(10L);
    req.setTaskTitle("Task");

    when(repo.issueExists(10L)).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> controller.update(7L, req));
  }

  @Test
  void update_acceptsValidRequest() {
    UpdateOverdueReportRequest req = new UpdateOverdueReportRequest();
    req.setIssueId(10L);
    req.setTaskTitle("Task");

    when(repo.issueExists(10L)).thenReturn(true);

    controller.update(7L, req);

    verify(repo).update(7L, req);
  }

  @Test
  void delete_delegatesToRepository() {
    controller.delete(7L);

    verify(repo).delete(7L);
  }
}
