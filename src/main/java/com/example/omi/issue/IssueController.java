package com.example.omi.issue;

import com.example.omi.EmbeddingService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class IssueController {

  private final IssueRepository repo;
  private final EmbeddingService embeddingService;

  public IssueController(IssueRepository repo, EmbeddingService embeddingService) {
    this.repo = repo;
    this.embeddingService = embeddingService;
  }

  @GetMapping("/projects/{projectId}/issues")
  public List<IssueDto> list(
      @PathVariable Long projectId,
      @RequestParam(required = false) Long sprintId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long assignedTo,
      @RequestParam(required = false) String dateRange) {

    LocalDate startDate = null;
    LocalDate endDate = null;

    if (dateRange != null && !dateRange.isBlank()) {
      String[] parts = dateRange.split(",");
      if (parts.length == 2) {
        startDate = LocalDate.parse(parts[0].trim());
        endDate = LocalDate.parse(parts[1].trim());
      } else {
        throw new IllegalArgumentException("dateRange must have format yyyy-MM-dd,yyyy-MM-dd");
      }
    }

    return repo.findByProject(projectId, sprintId, status, assignedTo, startDate, endDate);
  }

  @GetMapping("/projects/{projectId}/issues/semantic-search")
  public List<IssueDto> searchByTitle(@PathVariable Long projectId, @RequestParam String search) {
    if (search == null || search.isBlank()) {
      throw new IllegalArgumentException("search is required");
    }

    float[] queryEmbedding = embeddingService.embedTitle(search.trim());
    return repo.searchByTitleSemantic(projectId, queryEmbedding);
  }

  @PostMapping("/projects/{projectId}/issues/reindex-embeddings")
  public Map<String, Object> reindexIssueEmbeddings(@PathVariable Long projectId) {
    int updated = repo.reindexTitleEmbeddingsByProject(projectId, embeddingService);
    return Map.of(
        "projectId", projectId,
        "updated", updated);
  }

  @GetMapping("/issues/{issueId}")
  public IssueDto getById(@PathVariable Long issueId) {
    IssueDto issue = repo.findById(issueId);
    if (issue == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Issue not found");
    }

    return issue;
  }

  @PostMapping("/projects/{projectId}/issues")
  public void create(@PathVariable Long projectId, @Valid @RequestBody CreateIssueRequest req) {

    if (!repo.isFeatureInProject(req.getFeatureId(), projectId)) {
      throw new IllegalArgumentException("Feature does not belong to the given project");
    }

    float[] titleEmbedding = embeddingService.embedTitle(req.getTitle());
    repo.create(req, titleEmbedding);
  }

  @PatchMapping("/issues/{issueId}")
  public void patch(@PathVariable Long issueId, @Valid @RequestBody PatchIssueRequest req) {
    float[] titleEmbedding =
        req.getTitle() != null ? embeddingService.embedTitle(req.getTitle()) : null;
    repo.patch(issueId, req, titleEmbedding);
  }

  @GetMapping("/issues/{issueId}/timelogs")
  public List<TimeLogDto> getIssueTimeLogs(@PathVariable Long issueId) {
    return repo.findTimeLogsByIssue(issueId);
  }

  @PostMapping("/issues/{issueId}/timelogs")
  public void createIssueTimeLog(
      @PathVariable Long issueId, @Valid @RequestBody CreateTimeLogRequest req) {
    repo.createTimeLog(issueId, req);
  }

  @GetMapping("/projects/{projectId}/timelogs")
  public List<TimeLogDto> getProjectTimeLogs(
      @PathVariable Long projectId, @RequestParam(required = false) Long sprintId) {
    return repo.findTimeLogsByProject(projectId, sprintId);
  }

  @DeleteMapping("/issues/{issueId}")
  public void delete(@PathVariable Long issueId) {
    repo.delete(issueId);
  }
}
