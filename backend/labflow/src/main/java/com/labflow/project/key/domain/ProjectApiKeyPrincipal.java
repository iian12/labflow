package com.labflow.project.key.domain;

import com.labflow.project.project.ProjectId;
import com.labflow.user.domain.UserId;

public record ProjectApiKeyPrincipal(
        ProjectApiKeyId apiKeyId,
        ProjectId projectId,
        UserId createdBy
) {
}