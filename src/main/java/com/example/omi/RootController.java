package com.example.omi;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

  @GetMapping("/")
  public Map<String, String> root() {
    return Map.of(
        "status", "ok",
        "message", "OMI backend is running");
  }
}
