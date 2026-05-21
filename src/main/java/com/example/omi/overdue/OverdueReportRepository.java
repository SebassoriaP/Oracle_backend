package com.example.omi.overdue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OverdueReportRepository {

  private final JdbcTemplate jdbc;

  public OverdueReportRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<OverdueReportDto> findAll() {
    String sql =
        """
        SELECT
            id,
            issue_id,
            task_title,
            developer_name,
            due_date,
            submitted_at,
            reason,
            ai_summary,
            ai_category,
            severity,
            delay_days,
            impact_level,
            description,
            recommendation
        FROM overdue_report
        ORDER BY id
        """;

    return jdbc.query(sql, this::mapOverdueReportDto);
  }

  public OverdueReportDto findById(Long reportId) {
    String sql =
        """
        SELECT
            id,
            issue_id,
            task_title,
            developer_name,
            due_date,
            submitted_at,
            reason,
            ai_summary,
            ai_category,
            severity,
            delay_days,
            impact_level,
            description,
            recommendation
        FROM overdue_report
        WHERE id = ?
        """;

    return jdbc.queryForObject(sql, this::mapOverdueReportDto, reportId);
  }

  public void create(CreateOverdueReportRequest req) {
    String sql =
        """
        INSERT INTO overdue_report (
            id,
            issue_id,
            task_title,
            developer_name,
            due_date,
            submitted_at,
            reason,
            ai_summary,
            ai_category,
            severity,
            delay_days,
            impact_level,
            description,
            recommendation
        ) VALUES (
            (SELECT NVL(MAX(id), 0) + 1 FROM overdue_report),
            ?,
            ?,
            ?,
            ?,
            SYSTIMESTAMP,
            ?,
            ?,
            ?,
            ?,
            ?,
            ?,
            ?,
            ?
        )
        """;

    jdbc.update(sql,
        req.getIssueId(),
        req.getTaskTitle(),
        req.getDeveloperName(),
        req.getDueDate(),
        req.getReason(),
        req.getAiSummary(),
        req.getAiCategory(),
        req.getSeverity(),
        req.getDelayDays(),
        req.getImpactLevel(),
        req.getDescription(),
        req.getRecommendation()
    );
  }

  public void update(Long reportId, UpdateOverdueReportRequest req) {
    String sql =
        """
        UPDATE overdue_report
        SET
            issue_id = ?,
            task_title = ?,
            developer_name = ?,
            due_date = ?,
            reason = ?,
            ai_summary = ?,
            ai_category = ?,
            severity = ?,
            delay_days = ?,
            impact_level = ?,
            description = ?,
            recommendation = ?
        WHERE id = ?
        """;

    int rows = jdbc.update(sql,
        req.getIssueId(),
        req.getTaskTitle(),
        req.getDeveloperName(),
        req.getDueDate(),
        req.getReason(),
        req.getAiSummary(),
        req.getAiCategory(),
        req.getSeverity(),
        req.getDelayDays(),
        req.getImpactLevel(),
        req.getDescription(),
        req.getRecommendation(),
        reportId
    );

    if (rows == 0) {
      throw new org.springframework.dao.EmptyResultDataAccessException(1);
    }
  }

  public void delete(Long reportId) {
    int rows = jdbc.update(
        "DELETE FROM overdue_report WHERE id = ?",
        reportId
    );

    if (rows == 0) {
      throw new org.springframework.dao.EmptyResultDataAccessException(1);
    }
  }

  public boolean issueExists(Long issueId) {
    Integer count =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM issues WHERE id = ?",
            Integer.class,
            issueId
        );

    return count != null && count > 0;
  }

  private OverdueReportDto mapOverdueReportDto(ResultSet rs, int rowNum) throws SQLException {
    return new OverdueReportDto(
        rs.getLong("id"),
        rs.getLong("issue_id"),
        rs.getString("task_title"),
        rs.getString("developer_name"),
        safeToLocalDateTime(rs, "due_date"),
        safeToLocalDateTime(rs, "submitted_at"),
        rs.getString("reason"),
        rs.getString("ai_summary"),
        rs.getString("ai_category"),
        rs.getString("severity"),
        rs.getObject("delay_days", Integer.class),
        rs.getString("impact_level"),
        rs.getString("description"),
        rs.getString("recommendation")
    );
  }

  private LocalDateTime safeToLocalDateTime(ResultSet rs, String column) throws SQLException {
    var ts = rs.getTimestamp(column);
    return ts != null ? ts.toLocalDateTime() : null;
  }
}