package com.labflow.project.key;

import java.time.Instant;

public record IssueProjectApiKeyResult(ProjectApiKeyId apiKeyId, String rawApiKey, String keyPrefix, Instant expiresAt) {
}
