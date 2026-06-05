package com.example.omi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class RootControllerTest {

  private final RootController controller = new RootController();

  @Test
  void root_returnsOkMessage() {
    Map<String, String> response = controller.root();

    assertThat(response).containsEntry("status", "ok");
    assertThat(response).containsEntry("message", "OMI backend is running");
  }
}
