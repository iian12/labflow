package com.labflow.project.key.application;

import com.labflow.project.key.domain.ProjectApiKeyId;

public record IssueProjectApiKeyResult(ProjectApiKeyId apiKeyId, String rawApiKey) {
}
