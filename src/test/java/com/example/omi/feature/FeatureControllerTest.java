package com.example.omi.feature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeatureControllerTest {

  @Mock private FeatureRepository repo;

  @InjectMocks private FeatureController controller;

  @Test
  void create_delegatesToRepository() {
    CreateFeatureRequest req = new CreateFeatureRequest();
    req.setTitle("Feature");
    req.setDescription("Desc");
    req.setPriority("high");
    req.setStatus("open");

    controller.create(7L, req);

    verify(repo).create(7L, req);
  }

  @Test
  void list_delegatesToRepository() {
    when(repo.findBySprint(7L)).thenReturn(List.of());

    List<FeatureDto> result = controller.list(7L);

    assertThat(result).isEmpty();
    verify(repo).findBySprint(7L);
  }

  @Test
  void delete_delegatesToRepository() {
    controller.delete(7L, 3L);

    verify(repo).delete(7L, 3L);
  }
}
