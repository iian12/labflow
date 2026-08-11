package com.labflow.project.key;

import java.time.Instant;
import java.util.Set;

public record IssueProjectApiKeyCommand(
        String name,
        Set<ProjectApiKeyScope> scopes,
        Instant expiresAt
) {
}
