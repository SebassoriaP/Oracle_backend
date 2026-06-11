package com.example.omi.issue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.omi.EmbeddingService;

import oracle.jdbc.OracleType;

@Repository
public class IssueRepository {

  private final JdbcTemplate jdbc;

  public IssueRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<IssueDto> findByProject(
      Long projectId,
      Long sprintId,
      String status,
      Long assignedTo,
      LocalDate startDate,
      LocalDate endDate) {

    String normalizedStatus = normalizeIssueStatus(status);

    StringBuilder sql =
        new StringBuilder(
            """
            SELECT
                i.id,
                s.project_id,
                f.sprint_id,
                i.feature_id,
                i.title,
                i.description,
                i.status,
                i.type,
                i.assigned_to,
                i.created_at,
                i.updated_at,
                i.estimated_hours,
                i.actual_hours,
                i.is_visible,
                i.due_date
            FROM issues i
            JOIN feature f ON f.id = i.feature_id
            JOIN sprint s ON s.id = f.sprint_id
            WHERE s.project_id = ?
            """);

    List<Object> args = new ArrayList<>();
    args.add(projectId);

    if (sprintId != null) {
      sql.append(" AND f.sprint_id = ?");
      args.add(sprintId);
    }

    if (normalizedStatus != null) {
      sql.append(" AND i.status = ?");
      args.add(normalizedStatus);
    }

    if (assignedTo != null) {
      sql.append(" AND i.assigned_to = ?");
      args.add(assignedTo);
    }

    if (startDate != null) {
      sql.append(" AND TRUNC(i.created_at) >= ?");
      args.add(java.sql.Date.valueOf(startDate));
    }

    if (endDate != null) {
      sql.append(" AND TRUNC(i.created_at) <= ?");
      args.add(java.sql.Date.valueOf(endDate));
    }

    sql.append(" ORDER BY i.id");

    return jdbc.query(sql.toString(), this::mapIssueDto, args.toArray());
  }

  public List<IssueDto> searchByTitleSemantic(Long projectId, float[] queryEmbedding) {
    String sql =
        """
        SELECT
            i.id,
            s.project_id,
            f.sprint_id,
            i.feature_id,
            i.title,
            i.description,
            i.status,
            i.type,
            i.assigned_to,
            i.created_at,
            i.updated_at,
            i.estimated_hours,
            i.actual_hours,
            i.is_visible,
            i.due_date
        FROM issues i
        JOIN feature f ON f.id = i.feature_id
        JOIN sprint s ON s.id = f.sprint_id
        WHERE s.project_id = ?
          AND i.title_embedding IS NOT NULL
        ORDER BY COSINE_DISTANCE(i.title_embedding, ?), i.id
        FETCH FIRST 20 ROWS ONLY
        """;

    return jdbc.query(
        con -> {
          PreparedStatement ps = con.prepareStatement(sql);
          ps.setObject(1, projectId);
          ps.setObject(2, queryEmbedding, OracleType.VECTOR_FLOAT32);
          return ps;
        },
        this::mapIssueDto);
  }

  public int reindexTitleEmbeddingsByProject(Long projectId, EmbeddingService embeddingService) {
    List<IssueDto> issues = findByProject(projectId, null, null, null, null, null);

    int updated = 0;

    for (IssueDto issue : issues) {
      if (issue.title() == null || issue.title().isBlank()) {
        continue;
      }

      float[] embedding = embeddingService.embedTitle(issue.title());
      updated += updateTitleEmbedding(issue.id(), embedding);
    }

    return updated;
  }

  private int updateTitleEmbedding(Long issueId, float[] embedding) {
    String sql =
        """
        UPDATE issues
        SET title_embedding = ?,
            updated_at = SYSTIMESTAMP
        WHERE id = ?
        """;

    return jdbc.update(
        con -> {
          PreparedStatement ps = con.prepareStatement(sql);
          ps.setObject(1, embedding, OracleType.VECTOR_FLOAT32);
          ps.setLong(2, issueId);
          return ps;
        });
  }

  public IssueDto findById(Long issueId) {
    String sql =
        """
        SELECT
            i.id,
            s.project_id,
            f.sprint_id,
            i.feature_id,
            i.title,
            i.description,
            i.status,
            i.type,
            i.assigned_to,
            i.created_at,
            i.updated_at,
            i.estimated_hours,
            i.actual_hours,
            i.is_visible,
            i.due_date
        FROM issues i
        JOIN feature f ON f.id = i.feature_id
        JOIN sprint s ON s.id = f.sprint_id
        WHERE i.id = ?
        """;

    List<IssueDto> results = jdbc.query(sql, this::mapIssueDto, issueId);
    return results.isEmpty() ? null : results.get(0);
  }

  public void create(CreateIssueRequest r, float[] titleEmbedding) {
    String sql =
        """
        INSERT INTO issues (
          id,
          title,
          description,
          type,
          status,
          estimated_hours,
          actual_hours,
          feature_id,
          assigned_to,
          is_visible,
          created_at,
          updated_at,
          due_date,
          title_embedding
        )
        VALUES (
            (SELECT COALESCE(MAX(id), 0) + 1 FROM issues),
            ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, NULL, ?, ?
        )
        """;

    jdbc.update(
        con -> {
          PreparedStatement ps = con.prepareStatement(sql);
          int i = 1;

          ps.setString(i++, r.getTitle());
          ps.setString(i++, r.getDescription());
          ps.setString(i++, normalizeIssueType(r.getType()));
          ps.setString(i++, normalizeIssueStatus(r.getStatus()));
          ps.setObject(i++, r.getEstimatedHours());
          ps.setObject(i++, r.getActualHours());
          ps.setObject(i++, r.getFeatureId());
          ps.setObject(i++, r.getAssigneeId());
          ps.setObject(i++, Boolean.TRUE.equals(r.getIsVisible()) ? 1 : 0);
          ps.setObject(i++, r.getDueDate() != null ? java.sql.Date.valueOf(r.getDueDate()) : null);
          ps.setObject(i++, titleEmbedding, OracleType.VECTOR_FLOAT32);

          return ps;
        });
  }

  public void patch(Long issueId, PatchIssueRequest r, float[] titleEmbedding) {
    StringBuilder sql = new StringBuilder("UPDATE issues SET ");
    List<Object> params = new ArrayList<>();
    boolean first = true;

    if (r.getTitle() != null) {
      sql.append(first ? "" : ", ").append("title = ?");
      params.add(r.getTitle());
      first = false;

      sql.append(", title_embedding = ?");
      params.add(titleEmbedding);
    }

    if (r.getDescription() != null) {
      sql.append(first ? "" : ", ").append("description = ?");
      params.add(r.getDescription());
      first = false;
    }

    if (r.getType() != null) {
      sql.append(first ? "" : ", ").append("type = ?");
      params.add(normalizeIssueType(r.getType()));
      first = false;
    }

    if (r.getStatus() != null) {
      sql.append(first ? "" : ", ").append("status = ?");
      params.add(normalizeIssueStatus(r.getStatus()));
      first = false;
    }

    if (r.getEstimatedHours() != null) {
      sql.append(first ? "" : ", ").append("estimated_hours = ?");
      params.add(r.getEstimatedHours());
      first = false;
    }

    if (r.getActualHours() != null) {
      sql.append(first ? "" : ", ").append("actual_hours = ?");
      params.add(r.getActualHours());
      first = false;
    }

    if (r.getFeatureId() != null) {
      sql.append(first ? "" : ", ").append("feature_id = ?");
      params.add(r.getFeatureId());
      first = false;
    }

    if (r.getAssigneeId() != null) {
      sql.append(first ? "" : ", ").append("assigned_to = ?");
      params.add(r.getAssigneeId());
      first = false;
    }

    if (r.getIsVisible() != null) {
      sql.append(first ? "" : ", ").append("is_visible = ?");
      params.add(r.getIsVisible() ? 1 : 0);
      first = false;
    }

    if (first) {
      throw new IllegalArgumentException("No fields provided for patch");
    }

    sql.append(", updated_at = SYSTIMESTAMP WHERE id = ?");
    params.add(issueId);

    int rows =
        jdbc.update(
            con -> {
              PreparedStatement ps = con.prepareStatement(sql.toString());
              int i = 1;

              for (Object param : params) {
                if (param instanceof float[] vec) {
                  ps.setObject(i++, vec, OracleType.VECTOR_FLOAT32);
                } else {
                  ps.setObject(i++, param);
                }
              }

              return ps;
            });

    if (rows == 0) {
      throw new org.springframework.dao.EmptyResultDataAccessException(1);
    }
  }

  public List<TimeLogDto> findTimeLogsByIssue(Long issueId) {
    String sql =
        """
        SELECT
            t.id,
            t.issue_id,
            t.user_id,
            u.name AS user_name,
            t.hours_logged,
            t.log_date,
            f.sprint_id,
            s.project_id
        FROM timelog t
        JOIN issues i ON i.id = t.issue_id
        JOIN users u ON u.id = t.user_id
        JOIN feature f ON f.id = i.feature_id
        JOIN sprint s ON s.id = f.sprint_id
        WHERE t.issue_id = ?
        ORDER BY t.id
        """;

    return jdbc.query(sql, this::mapTimeLogDto, issueId);
  }

  public void createTimeLog(Long issueId, CreateTimeLogRequest r) {
    String sql =
        """
        INSERT INTO timelog (
            id,
            issue_id,
            user_id,
            hours_logged,
            log_date
        ) VALUES (
            (SELECT COALESCE(MAX(id), 0) + 1 FROM timelog),
            ?, ?, ?, ?
        )
        """;

    jdbc.update(
        sql, issueId, r.getUserId(), r.getHoursLogged(), java.sql.Date.valueOf(r.getLogDate()));
  }

  public List<TimeLogDto> findTimeLogsByProject(Long projectId, Long sprintId) {
    StringBuilder sql =
        new StringBuilder(
            """
            SELECT
                t.id,
                t.issue_id,
                t.user_id,
                u.name AS user_name,
                t.hours_logged,
                t.log_date,
                f.sprint_id,
                s.project_id
            FROM timelog t
            JOIN issues i ON i.id = t.issue_id
            JOIN users u ON u.id = t.user_id
            JOIN feature f ON f.id = i.feature_id
            JOIN sprint s ON s.id = f.sprint_id
            WHERE s.project_id = ?
            """);

    List<Object> args = new ArrayList<>();
    args.add(projectId);

    if (sprintId != null) {
      sql.append(" AND f.sprint_id = ?");
      args.add(sprintId);
    }

    sql.append(" ORDER BY t.id");

    return jdbc.query(sql.toString(), this::mapTimeLogDto, args.toArray());
  }

  private IssueDto mapIssueDto(ResultSet rs, int rowNum) throws SQLException {
    java.sql.Date dueDate = rs.getDate("due_date");

    return new IssueDto(
        rs.getLong("id"),
        rs.getLong("project_id"),
        rs.getLong("sprint_id"),
        rs.getLong("feature_id"),
        rs.getString("title"),
        rs.getString("description"),
        rs.getString("status"),
        rs.getString("type"),
        rs.getLong("assigned_to"),
        rs.getTimestamp("created_at"),
        rs.getTimestamp("updated_at"),
        rs.getObject("estimated_hours", Integer.class),
        rs.getObject("actual_hours", Integer.class),
        rs.getInt("is_visible") == 1,
        dueDate != null ? dueDate.toLocalDate() : null);
  }

  private TimeLogDto mapTimeLogDto(ResultSet rs, int rowNum) throws SQLException {
    return new TimeLogDto(
        rs.getLong("id"),
        rs.getLong("issue_id"),
        rs.getLong("user_id"),
        rs.getString("user_name"),
        rs.getBigDecimal("hours_logged"),
        rs.getDate("log_date").toLocalDate(),
        rs.getLong("sprint_id"),
        rs.getLong("project_id"));
  }

  private String normalizeIssueStatus(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }

    return switch (status.trim().toUpperCase()) {
      case "OPEN", "TODO", "TO_DO" -> "open";
      case "IN_PROGRESS", "INPROGRESS" -> "in_progress";
      case "DONE", "CLOSED" -> "closed";
      default -> status;
    };
  }

  private String normalizeIssueType(String type) {
    if (type == null || type.isBlank()) {
      return null;
    }
    return type.trim().toUpperCase();
  }

  public boolean isFeatureInProject(Long featureId, Long projectId) {
    String sql =
        """
        SELECT COUNT(*)
        FROM feature f
        JOIN sprint s ON s.id = f.sprint_id
        WHERE f.id = ?
          AND s.project_id = ?
        """;

    Integer count = jdbc.queryForObject(sql, Integer.class, featureId, projectId);
    return count != null && count > 0;
  }

  public void delete(Long issueId) {
      jdbc.update("DELETE FROM timelog WHERE issue_id = ?", issueId);
      jdbc.update("DELETE FROM issue_log WHERE issue_id = ?", issueId);

      int rows = jdbc.update("DELETE FROM issues WHERE id = ?", issueId);

      if (rows == 0) {
        throw new org.springframework.dao.EmptyResultDataAccessException(1);
      }
    }

    public List<IssueDto> findOverdueIssues() {
      String sql = """
          SELECT
              i.id,
              s.project_id,
              f.sprint_id,
              i.feature_id,
              i.title,
              i.description,
              i.status,
              i.type,
              i.assigned_to,
              i.created_at,
              i.updated_at,
              i.estimated_hours,
              i.actual_hours,
              i.is_visible,
              i.due_date
          FROM issues i
          JOIN feature f ON f.id = i.feature_id
          JOIN sprint s ON s.id = f.sprint_id
          WHERE i.due_date < SYSDATE
            AND i.status != 'closed'
            AND i.overdue_notified = 0
      """;

      return jdbc.query(sql, this::mapIssueDto);
  }

  public void markAsNotified(List<Long> issueIds) {
      String sql = """
          UPDATE issues
          SET overdue_notified = 1
          WHERE id = ? AND overdue_notified = 0
      """;

      jdbc.batchUpdate(sql, issueIds, 50, (ps, id) -> {
          ps.setLong(1, id);
      });
  }
}
