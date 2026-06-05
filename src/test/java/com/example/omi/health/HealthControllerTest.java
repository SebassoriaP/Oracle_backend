package com.example.omi.health;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

  @Autowired private MockMvc mvc;

  @MockitoBean private JdbcTemplate jdbcTemplate;

  @Test
  void health_returnsOkAndDbStatus() throws Exception {
    when(jdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class)).thenReturn(1);

    mvc.perform(get("/api/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok_6"))
        .andExpect(jsonPath("$.db").value(1));

    verify(jdbcTemplate).queryForObject("SELECT 1 FROM DUAL", Integer.class);
  }
}
