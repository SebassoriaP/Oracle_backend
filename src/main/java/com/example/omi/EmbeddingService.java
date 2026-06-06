package com.example.omi;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {

  private final EmbeddingModel model = new AllMiniLmL6V2EmbeddingModel();

  public float[] embedTitle(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }

    Embedding embedding = model.embed(text.trim()).content();
    return embedding.vector();
  }
}
