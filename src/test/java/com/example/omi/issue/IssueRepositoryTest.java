package com.example.omi.issue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class IssueRepositoryTest {

  @Mock private JdbcTemplate jdbc;

  @InjectMocks private IssueRepository repo;

  @Test
  void patch_throwsWhenNoFieldsProvided() {
    assertThatThrownBy(() -> repo.patch(10L, new PatchIssueRequest()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("No fields provided for patch");
  }

  @Test
  void isFeatureInProject_returnsTrueWhenCountIsPositive() {
    when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(7L), eq(3L))).thenReturn(1);

    assertThat(repo.isFeatureInProject(7L, 3L)).isTrue();
  }

  @Test
  void delete_throwsWhenNothingIsDeleted() {
    when(jdbc.update("DELETE FROM timelog WHERE issue_id = ?", 99L)).thenReturn(1);
    when(jdbc.update("DELETE FROM issue_log WHERE issue_id = ?", 99L)).thenReturn(1);
    when(jdbc.update("DELETE FROM issues WHERE id = ?", 99L)).thenReturn(0);

    assertThatThrownBy(() -> repo.delete(99L)).isInstanceOf(EmptyResultDataAccessException.class);
  }

  @Test
  void create_normalizesTypeStatusAndUsesDueDate() {
    CreateIssueRequest req = new CreateIssueRequest();
    req.setTitle("Bug");
    req.setDescription("desc");
    req.setType("bug");
    req.setStatus("DONE");
    req.setEstimatedHours(5);
    req.setActualHours(2);
    req.setFeatureId(11L);
    req.setAssigneeId(22L);
    req.setIsVisible(true);
    req.setDueDate(OffsetDateTime.parse("2026-04-16T10:00:00Z"));

    repo.create(req);

    ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
    verify(jdbc).update(anyString(), argsCaptor.capture());

    Object[] args = argsCaptor.getValue();
    assertThat(args[0]).isEqualTo("Bug");
    assertThat(args[2]).isEqualTo("BUG");
    assertThat(args[3]).isEqualTo("closed");
    assertThat(args[8]).isEqualTo(1);
    assertThat(args[9]).isEqualTo(Timestamp.from(req.getDueDate().toInstant()));
  }
}
