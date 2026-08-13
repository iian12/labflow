package com.labflow.project.key.presentation;

import com.labflow.global.resolver.CurrentUserId;
import com.labflow.project.key.application.IssueProjectApiKeyResult;
import com.labflow.project.key.application.ProjectApiKeyService;
import com.labflow.project.project.ProjectId;
import com.labflow.user.domain.UserId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/project-api-keys")
public class ProjectApiKeyController {

    private final ProjectApiKeyService projectApiKeyService;

    public ProjectApiKeyController(ProjectApiKeyService projectApiKeyService) {
        this.projectApiKeyService = projectApiKeyService;
    }

    @PostMapping("/issue/{projectId}")
    public ResponseEntity<IssueProjectApiKeyResponse> issueProjectApiKey(@CurrentUserId UserId userId,
                                                                         @PathVariable("projectId") Long projectId,
                                                                         @RequestBody IssueProjectApiKeyRequest request) {
        IssueProjectApiKeyResult result = projectApiKeyService.issue(userId, ProjectId.of(projectId), request);
        IssueProjectApiKeyResponse response = IssueProjectApiKeyResponse.from(result);
        return ResponseEntity.ok(response);
    }
}
