package com.example.omi.feature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class FeatureRepositoryTest {

  @Mock private JdbcTemplate jdbc;

  @InjectMocks private FeatureRepository repo;

  @Test
  void create_writesRow() {
    CreateFeatureRequest req = new CreateFeatureRequest();
    req.setTitle("Feature");
    req.setDescription("Desc");
    req.setPriority("high");
    req.setStatus("open");

    repo.create(7L, req);

    verify(jdbc).update(anyString(), eq("Feature"), eq("Desc"), eq(7L), eq("high"), eq("open"));
  }

  @Test
  void findBySprint_mapsRows() {
    when(jdbc.query(anyString(), any(RowMapper.class), eq(7L)))
        .thenReturn(List.of(new FeatureDto(1L, "Feature", "Desc", 7L, "high", "open")));

    assertThat(repo.findBySprint(7L)).hasSize(1);
  }

  @Test
  void delete_throwsWhenMissing() {
    when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(3L), eq(7L))).thenReturn(0);

    assertThatThrownBy(() -> repo.delete(7L, 3L))
        .isInstanceOf(EmptyResultDataAccessException.class);
  }
}
