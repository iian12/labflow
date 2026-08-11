package com.labflow.project.key;

import com.labflow.project.project.ProjectId;
import com.labflow.user.domain.UserId;

import java.util.Set;

public record ProjectApiKeyPrincipal(ProjectApiKeyId apiKeyId, ProjectId projectId, UserId createdBy, Set<ProjectApiKeyScope> scopes) {
    public boolean hasScope(ProjectApiKeyStatus scope) {
        return scopes.contains(scope);
    }
}
