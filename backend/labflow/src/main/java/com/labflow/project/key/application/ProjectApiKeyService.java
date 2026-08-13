package com.labflow.project.key.application;

import com.labflow.global.id.IdGenerator;
import com.labflow.project.key.domain.ProjectApiKey;
import com.labflow.project.key.domain.ProjectApiKeyId;
import com.labflow.project.key.domain.ProjectApiKeyStatus;
import com.labflow.project.key.domain.repository.ProjectApiKeyRepository;
import com.labflow.project.key.presentation.IssueProjectApiKeyRequest;
import com.labflow.project.key.util.ProjectApiKeyGenerator;
import com.labflow.project.key.util.ProjectApiKeyHasher;
import com.labflow.project.project.ProjectId;
import com.labflow.project.project.ProjectRepository;
import com.labflow.user.domain.UserId;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class ProjectApiKeyService {

    private final ProjectRepository projectRepository;
    private final ProjectApiKeyGenerator projectApiKeyGenerator;
    private final Clock clock;
    private final IdGenerator idGenerator;
    private final ProjectApiKeyHasher projectApiKeyHasher;
    private final ProjectApiKeyRepository projectApiKeyRepository;

    public ProjectApiKeyService(ProjectRepository projectRepository, ProjectApiKeyGenerator projectApiKeyGenerator, Clock clock, IdGenerator idGenerator, ProjectApiKeyHasher projectApiKeyHasher, ProjectApiKeyRepository projectApiKeyRepository) {
        this.projectRepository = projectRepository;
        this.projectApiKeyGenerator = projectApiKeyGenerator;
        this.clock = clock;
        this.idGenerator = idGenerator;
        this.projectApiKeyHasher = projectApiKeyHasher;
        this.projectApiKeyRepository = projectApiKeyRepository;
    }

    @Transactional
    public IssueProjectApiKeyResult issue(
            UserId requesterId,
            ProjectId projectId,
            IssueProjectApiKeyRequest request
    ) {
        if (projectRepository.findById(projectId).isEmpty())
            throw new IllegalArgumentException("Project not found");

        GeneratedProjectApiKey generated = projectApiKeyGenerator.generate();

        Instant now = clock.instant();

        ProjectApiKey apiKey = ProjectApiKey.create(
                ProjectApiKeyId.of(idGenerator.nextId()),
                projectId,
                requesterId,
                request.name(),
                generated.keyPrefix(),
                projectApiKeyHasher.hash(generated.rawKey()),
                ProjectApiKeyStatus.ACTIVE,
                now,
                null,
                now
        );

        projectApiKeyRepository.save(apiKey);

        return new IssueProjectApiKeyResult(
                apiKey.getId(),
                generated.rawKey()
        );
    }
}
