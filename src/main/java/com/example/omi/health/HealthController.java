package com.example.omi.health;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HealthController {

  private final JdbcTemplate jdbcTemplate;

  public HealthController(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @GetMapping("/health")
  public Map<String, Object> health() {
    Integer result = jdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
    return Map.of("status", "ok", "db", result);
  }
}
