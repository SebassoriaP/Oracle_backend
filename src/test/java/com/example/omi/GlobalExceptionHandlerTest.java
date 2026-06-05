package com.example.omi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void illegalArgument_returns400() {
    ResponseEntity<Map<String, String>> response =
        handler.handleIllegalArgument(new IllegalArgumentException("bad input"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).containsEntry("error", "Invalid Argument");
  }

  @Test
  void dataIntegrity_returns409() {
    ResponseEntity<Map<String, String>> response =
        handler.handleDataIntegrity(new DataIntegrityViolationException("dup"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).containsEntry("error", "Database Conflict");
  }

  @Test
  void emptyResult_returns404() {
    ResponseEntity<Map<String, String>> response =
        handler.handleNotFoundData(new EmptyResultDataAccessException(1));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).containsEntry("error", "Not Found");
  }

  @Test
  void genericException_returns500() {
    ResponseEntity<Map<String, String>> response =
        handler.handleOther(new RuntimeException("boom"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).containsEntry("error", "Internal Server Error");
  }
}
