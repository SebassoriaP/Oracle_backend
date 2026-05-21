package com.example.omi.project;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectRepository {

  private final JdbcTemplate jdbcTemplate;

  public ProjectRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<Map<String, Object>> findAll() {
    return jdbcTemplate.queryForList("SELECT * FROM project ORDER BY id");
  }

  public List<ProjectMemberDto> findMembers(Long projectId) {
    String sql =
        """
        SELECT
            u.id AS user_id,
            u.name,
            pm.role_in_project AS role_name
        FROM project_member pm
        JOIN users u ON u.id = pm.user_id
        WHERE pm.project_id = ?
        ORDER BY u.id
        """;

    return jdbcTemplate.query(
        sql,
        (rs, rowNum) ->
            new ProjectMemberDto(
                rs.getLong("user_id"), rs.getString("name"), rs.getString("role_name")),
        projectId);
  }

  public void addMember(Long projectId, CreateProjectMemberRequest req) {
    String sql =
        """
        INSERT INTO project_member (
            id,
            project_id,
            user_id,
            role_in_project,
            joined_at
        ) VALUES (
            (SELECT COALESCE(MAX(id), 0) + 1 FROM project_member),
            ?, ?, ?, SYSTIMESTAMP
        )
        """;

    jdbcTemplate.update(sql, projectId, req.getUserId(), req.getRole());
  }

  public boolean userExists(Long userId) {
    String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
    return count != null && count > 0;
  }

  public boolean memberExists(Long projectId, Long userId) {
    String sql =
        """
        SELECT COUNT(*)
        FROM project_member
        WHERE project_id = ?
          AND user_id = ?
        """;

    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, projectId, userId);
    return count != null && count > 0;
  }

  public void deleteMember(Long projectId, Long userId) {
    String sql =
        """
        DELETE FROM project_member
        WHERE project_id = ?
          AND user_id = ?
        """;

    int rows = jdbcTemplate.update(sql, projectId, userId);

    if (rows == 0) {
      throw new org.springframework.dao.EmptyResultDataAccessException(1);
    }
  }

  public void createProject(CreateProjectRequest req) {
    String sql =
        """
        INSERT INTO project (
            id,
            name,
            description,
            status,
            created_at
        ) VALUES (
            (SELECT COALESCE(MAX(id), 0) + 1 FROM project),
            ?, ?, ?, SYSTIMESTAMP
        )
        """;

    jdbcTemplate.update(
        sql,
        req.getName(),
        req.getDescription(),
        req.getStatus()
    );
  }
}
