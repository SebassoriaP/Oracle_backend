package com.example.omi.role;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RoleRepository {

  private final JdbcTemplate jdbcTemplate;

  public RoleRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<RoleDto> findAll() {
    String sql = "SELECT id, name FROM role ORDER BY id";

    return jdbcTemplate.query(
        sql,
        (rs, rowNum) ->
            new RoleDto(
                rs.getLong("id"),
                rs.getString("name")
            )
    );
  }

  public void create(CreateRoleRequest req) {
    String sql =
        """
        INSERT INTO role (id, name)
        VALUES (
            (SELECT COALESCE(MAX(id), 0) + 1 FROM role),
            ?
        )
        """;

    jdbcTemplate.update(sql, req.getName());
  }

  public void delete(Long roleId) {
    int rows =
        jdbcTemplate.update(
            "DELETE FROM role WHERE id = ?",
            roleId
        );

    if (rows == 0) {
      throw new org.springframework.dao.EmptyResultDataAccessException(1);
    }
  }
}