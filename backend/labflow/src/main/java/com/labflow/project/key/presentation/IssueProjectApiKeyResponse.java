package com.labflow.project.key.presentation;

import com.labflow.project.key.application.IssueProjectApiKeyResult;

public record IssueProjectApiKeyResponse(String rawApiKey) {
    public static IssueProjectApiKeyResponse from(IssueProjectApiKeyResult result) {
        return new IssueProjectApiKeyResponse(result.rawApiKey());
    }
}
