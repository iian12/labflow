package com.labflow.project.key;

import com.labflow.project.project.ProjectId;
import com.labflow.user.domain.UserId;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

@Getter
public class ProjectApiKey {
    private final ProjectApiKeyId id;
    private final ProjectId projectId;
    private final UserId createdBy;
    private String name;
    private String keyPrefix;
    private String keyHash;
    private final Set<ProjectApiKeyScope> scopes;
    private ProjectApiKeyStatus status;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant revokedAt;
    private Instant lastUsedAt;

    private ProjectApiKey(ProjectApiKeyId id, ProjectId projectId, UserId createdBy, String name, String keyPrefix, String keyHash, Set<ProjectApiKeyScope> scopes, ProjectApiKeyStatus status, Instant createdAt, Instant expiresAt, Instant revokedAt, Instant lastUsedAt) {
        this.id = id;
        this.projectId = projectId;
        this.createdBy = createdBy;
        this.name = name;
        this.keyPrefix = keyPrefix;
        this.keyHash = keyHash;
        this.scopes = scopes;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.lastUsedAt = lastUsedAt;
    }

    public static ProjectApiKey create(ProjectApiKeyId id, ProjectId projectId, UserId createdBy, String name, String keyPrefix, String keyHash, Set<ProjectApiKeyScope> scopes, ProjectApiKeyStatus status, Instant createdAt, Instant expiresAt, Instant revokedAt, Instant lastUsedAt) {
        return new ProjectApiKey(id, projectId, createdBy, name, keyPrefix, keyHash, scopes, status, createdAt, expiresAt, revokedAt, lastUsedAt);
    }

    public static ProjectApiKey restore(ProjectApiKeyId id, ProjectId projectId, UserId createdBy, String name, String keyPrefix, String keyHash, Set<ProjectApiKeyScope> scopes, ProjectApiKeyStatus status, Instant createdAt, Instant expiresAt, Instant revokedAt, Instant lastUsedAt) {
        return new ProjectApiKey(id, projectId, createdBy, name, keyPrefix, keyHash, scopes, status, createdAt, expiresAt, revokedAt, lastUsedAt);
    }

    public void revoke(Instant revokedAt) {
        Objects.requireNonNull(revokedAt, "revokedAt must not be null");

        if (isRevoked()) {
            throw new IllegalStateException("API key is already revoked");
        }

        this.revokedAt = revokedAt;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }
}
