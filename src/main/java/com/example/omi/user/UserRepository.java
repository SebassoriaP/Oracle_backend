package com.example.omi.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

  private final JdbcTemplate jdbc;

  public UserRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<UserDto> findAll() {
    String sql =
        """
        SELECT
            id,
            name,
            email,
            password_hash,
            work_mode,
            role_id,
            manager_id,
            created_at,
            status,
            chat_id
        FROM users
        ORDER BY id
        """;

    return jdbc.query(sql, this::mapUserDto);
  }

  public void create(CreateUserRequest req) {
    String sql =
        """
        INSERT INTO users (
            id,
            name,
            email,
            password_hash,
            work_mode,
            role_id,
            manager_id,
            created_at,
            status
        ) VALUES (
            (SELECT COALESCE(MAX(id), 0) + 1 FROM users),
            ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, ?
        )
        """;

    jdbc.update(
        sql,
        new Object[] {
          req.getName(),
          req.getEmail(),
          req.getPasswordHash(),
          req.getWorkMode(),
          req.getRoleId(),
          req.getManagerId(),
          req.getStatus()
        },
        new int[] {
          Types.VARCHAR,
          Types.VARCHAR,
          Types.VARCHAR,
          Types.VARCHAR,
          Types.NUMERIC,
          Types.NUMERIC,
          Types.VARCHAR
        });
  }

  public void delete(Long userId) {
    int rows = jdbc.update("DELETE FROM users WHERE id = ?", userId);

    if (rows == 0) {
      throw new org.springframework.dao.EmptyResultDataAccessException(1);
    }
  }

  public boolean roleExists(Long roleId) {
    Integer count =
        jdbc.queryForObject("SELECT COUNT(*) FROM role WHERE id = ?", Integer.class, roleId);
    return count != null && count > 0;
  }

  public boolean userExists(Long userId) {
    Integer count =
        jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, userId);
    return count != null && count > 0;
  }

  public boolean emailExists(String email) {
    Integer count =
        jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
    return count != null && count > 0;
  }

  private UserDto mapUserDto(ResultSet rs, int rowNum) throws SQLException {
    Timestamp createdAt = rs.getTimestamp("created_at");

    return new UserDto(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getString("email"),
        rs.getString("password_hash"),
        rs.getString("work_mode"),
        rs.getObject("role_id", Long.class),
        rs.getObject("manager_id", Long.class),
        createdAt,
        rs.getString("status"),
        rs.getString("chat_id"));
  }

  public Optional<UserDto> findByEmail(String email) {
    String sql =
        """
        SELECT
            id,
            name,
            email,
            password_hash,
            work_mode,
            role_id,
            manager_id,
            created_at,
            status,
            chat_id
        FROM users
        WHERE email = ?
        """;

    return jdbc.query(sql, this::mapUserDto, email)
              .stream()
              .findFirst();
  }

  public void createFromSignup(
          String name,
          String email,
          String passwordHash,
          String workMode,
          Long roleId,
          Long managerId,
          String status,
          String chatId) {

      String sql =
          """
          INSERT INTO users (
              id,
              name,
              email,
              password_hash,
              work_mode,
              role_id,
              manager_id,
              created_at,
              status,
              chat_id
          ) VALUES (
              (SELECT COALESCE(MAX(id),0)+1 FROM users),
              ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, ?, ?
          )
          """;

      jdbc.update(
          sql,
          new Object[] {
              name,
              email,
              passwordHash,
              workMode,
              roleId,
              managerId,
              status,
              chatId
          },
          new int[] {
              Types.VARCHAR,
              Types.VARCHAR,
              Types.VARCHAR,
              Types.VARCHAR,
              Types.NUMERIC,
              Types.NUMERIC,
              Types.VARCHAR,
              Types.VARCHAR
          }
      );
  }
}
