package com.example.omi.issue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class IssueControllerTest {

  @Mock private IssueRepository repo;

  @InjectMocks private IssueController controller;

  @Test
  void list_parsesDateRangeAndCallsRepo() {
    when(repo.findByProject(
            1L, 2L, "open", 3L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
        .thenReturn(List.of());

    List<IssueDto> result = controller.list(1L, 2L, "open", 3L, "2026-01-01,2026-12-31");

    assertThat(result).isEmpty();
    verify(repo)
        .findByProject(1L, 2L, "open", 3L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
  }

  @Test
  void list_rejectsBadDateRange() {
    assertThrows(
        IllegalArgumentException.class, () -> controller.list(1L, null, null, null, "2026-01-01"));
  }

  @Test
  void getById_returnsIssue() {
    IssueDto issue =
        new IssueDto(
            99L,
            1L,
            2L,
            3L,
            "Title",
            "Desc",
            "open",
            "TASK",
            101L,
            OffsetDateTime.parse("2026-06-01T10:00:00Z"),
            null,
            5,
            2,
            true,
            null);

    when(repo.findById(99L)).thenReturn(issue);

    IssueDto result = controller.getById(99L);

    assertThat(result).isEqualTo(issue);
  }

  @Test
  void getById_throwsNotFoundWhenMissing() {
    when(repo.findById(99L)).thenReturn(null);

    assertThrows(ResponseStatusException.class, () -> controller.getById(99L));
  }

  @Test
  void create_rejectsFeatureOutsideProject() {
    CreateIssueRequest req = new CreateIssueRequest();
    req.setTitle("Issue");
    req.setType("TASK");
    req.setStatus("open");
    req.setEstimatedHours(5);
    req.setActualHours(0);
    req.setFeatureId(10L);
    req.setAssigneeId(20L);
    req.setIsVisible(true);

    when(repo.isFeatureInProject(10L, 1L)).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> controller.create(1L, req));
  }

  @Test
  void create_acceptsValidIssue() {
    CreateIssueRequest req = new CreateIssueRequest();
    req.setTitle("Issue");
    req.setDescription("Desc");
    req.setType("TASK");
    req.setStatus("open");
    req.setEstimatedHours(5);
    req.setActualHours(0);
    req.setFeatureId(10L);
    req.setAssigneeId(20L);
    req.setIsVisible(true);
    req.setDueDate(OffsetDateTime.parse("2026-06-16T10:00:00Z"));

    when(repo.isFeatureInProject(10L, 1L)).thenReturn(true);

    controller.create(1L, req);

    verify(repo).create(req);
  }

  @Test
  void patch_delegatesToRepository() {
    PatchIssueRequest req = new PatchIssueRequest();
    req.setTitle("Updated");
    req.setStatus("in_progress");

    controller.patch(77L, req);

    verify(repo).patch(77L, req);
  }

  @Test
  void getIssueTimeLogs_delegatesToRepository() {
    when(repo.findTimeLogsByIssue(50L)).thenReturn(List.of());

    List<TimeLogDto> result = controller.getIssueTimeLogs(50L);

    assertThat(result).isEmpty();
    verify(repo).findTimeLogsByIssue(50L);
  }

  @Test
  void createIssueTimeLog_delegatesToRepository() {
    CreateTimeLogRequest req = new CreateTimeLogRequest();
    req.setUserId(101L);
    req.setHoursLogged(java.math.BigDecimal.valueOf(2.5));
    req.setLogDate(LocalDate.of(2026, 4, 16));

    controller.createIssueTimeLog(99L, req);

    verify(repo).createTimeLog(99L, req);
  }

  @Test
  void getProjectTimeLogs_delegatesToRepository() {
    when(repo.findTimeLogsByProject(1L, 2L)).thenReturn(List.of());

    List<TimeLogDto> result = controller.getProjectTimeLogs(1L, 2L);

    assertThat(result).isEmpty();
    verify(repo).findTimeLogsByProject(1L, 2L);
  }

  @Test
  void delete_delegatesToRepository() {
    controller.delete(88L);

    verify(repo).delete(88L);
  }
}
