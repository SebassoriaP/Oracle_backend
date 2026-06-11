package com.example.omi.issue;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class IssueService {

    private final IssueRepository issueRepository;

    public IssueService(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    public List<IssueDto> getOverdueIssues() {
        return issueRepository.findOverdueIssues();
    }

    public void markIssueAsNotified(List<Long> issueIds) {
        if (issueIds == null || issueIds.isEmpty()) {
            return;
        }
        issueRepository.markAsNotified(issueIds);
    }
}