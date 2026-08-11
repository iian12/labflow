package com.labflow.project.key;

import com.labflow.project.project.ProjectId;
import com.labflow.user.domain.UserId;
import lombok.Getter;

@Getter
public class ProjectApiKeyMapper {

    private ProjectApiKeyMapper() {

    }

    public static ProjectApiKeyEntity toEntity(ProjectApiKey apiKey) {
        return ProjectApiKeyEntity.builder()
                .id(apiKey.getId().value())
                .projectId(apiKey.getProjectId().value())
                .createdBy(apiKey.getCreatedBy().value())
                .name(apiKey.getName())
                .keyPrefix(apiKey.getKeyPrefix())
                .keyHash(apiKey.getKeyHash())
                .scopes(apiKey.getScopes())
                .status(apiKey.getStatus())
                .createdAt(apiKey.getCreatedAt())
                .expiresAt(apiKey.getExpiresAt())
                .revokedAt(apiKey.getRevokedAt())
                .lastUsedAt(apiKey.getLastUsedAt())
                .build();
    }

    public static ProjectApiKey toDomain(ProjectApiKeyEntity entity) {
        return ProjectApiKey.restore(
                ProjectApiKeyId.of(entity.getId()),
                ProjectId.of(entity.getProjectId()),
                UserId.of(entity.getCreatedBy()),
                entity.getName(),
                entity.getKeyPrefix(),
                entity.getKeyHash(),
                entity.getScopes(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getLastUsedAt()
        );
    }
}
