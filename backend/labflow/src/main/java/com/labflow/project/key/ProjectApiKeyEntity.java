package com.labflow.project.key;

import com.labflow.global.id.AssignedIdEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectApiKeyEntity extends AssignedIdEntity {

    private Long projectId;
    private Long createdBy;
    private String name;
    private String keyPrefix;
    private String keyHash;

    @Enumerated(EnumType.STRING)
    private Set<ProjectApiKeyScope> scopes;

    @Enumerated(EnumType.STRING)
    private ProjectApiKeyStatus status;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant revokedAt;
    private Instant lastUsedAt;

    @Builder
    public ProjectApiKeyEntity(Long id, Long projectId, Long createdBy, String name, String keyPrefix, String keyHash, Set<ProjectApiKeyScope> scopes, ProjectApiKeyStatus status, Instant createdAt, Instant expiresAt, Instant revokedAt, Instant lastUsedAt) {
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
}
