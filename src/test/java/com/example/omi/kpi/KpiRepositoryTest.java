package com.example.omi.kpi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
class KpiRepositoryTest {

  @Mock private JdbcTemplate jdbc;

  @InjectMocks private KpiRepository repo;

  @Test
  void getSummary_returnsZerosWhenNoRows() {
    doThrow(new EmptyResultDataAccessException(1))
        .when(jdbc)
        .queryForObject(anyString(), any(RowMapper.class), eq(1L));

    KpiSummaryDto dto = repo.getSummary(1L, null);

    assertThat(dto.totalTasks()).isZero();
    assertThat(dto.totalActualHours()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(dto.avgTasksPerDev()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(dto.avgHoursPerDev()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @SuppressWarnings("unchecked")
  @Test
  void getTasksByUser_acceptsSprintFilter() {
    when(jdbc.query(anyString(), any(RowMapper.class), eq(1L), eq(9L)))
        .thenReturn(List.of(new TasksByUserDto(1L, "Ana", 3)));

    List<TasksByUserDto> result = repo.getTasksByUser(1L, 9L);

    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(new TasksByUserDto(1L, "Ana", 3));
  }

  @SuppressWarnings("unchecked")
  @Test
  void getRealHoursByUser_withoutSprintFilter() {
    when(jdbc.query(anyString(), any(RowMapper.class), eq(1L)))
        .thenReturn(List.of(new HoursByUserDto(2L, "Bea", new BigDecimal("12.5"))));

    List<HoursByUserDto> result = repo.getRealHoursByUser(1L, null);

    assertThat(result).containsExactly(new HoursByUserDto(2L, "Bea", new BigDecimal("12.5")));
  }

  @SuppressWarnings("unchecked")
  @Test
  void getEstimatedHoursByUser_acceptsSprintFilter() {
    when(jdbc.query(anyString(), any(RowMapper.class), eq(1L), eq(9L)))
        .thenReturn(List.of(new HoursByUserDto(3L, "Carla", new BigDecimal("8.0"))));

    List<HoursByUserDto> result = repo.getEstimatedHoursByUser(1L, 9L);

    assertThat(result).containsExactly(new HoursByUserDto(3L, "Carla", new BigDecimal("8.0")));
  }
}
