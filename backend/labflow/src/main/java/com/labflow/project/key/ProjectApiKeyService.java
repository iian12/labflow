package com.labflow.project.key;

import com.labflow.global.id.IdGenerator;
import com.labflow.project.project.Project;
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

    public ProjectApiKeyService(ProjectRepository projectRepository, ProjectApiKeyGenerator projectApiKeyGenerator, Clock clock, IdGenerator idGenerator, ProjectApiKeyHasher projectApiKeyHasher) {
        this.projectRepository = projectRepository;
        this.projectApiKeyGenerator = projectApiKeyGenerator;
        this.clock = clock;
        this.idGenerator = idGenerator;
        this.projectApiKeyHasher = projectApiKeyHasher;
    }

    @Transactional
    public IssueProjectApiKeyResult issue(
            UserId requesterId,
            ProjectId projectId,
            IssueProjectApiKeyCommand command
    ) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        GeneratedProjectApiKey generated = projectApiKeyGenerator.generate();

        Instant now = clock.instant();

        ProjectApiKey apiKey = ProjectApiKey.create(
                ProjectApiKeyId.of(idGenerator.nextId()),
                projectId,
                requesterId,
                command.name(),
                generated.keyPrefix(),
                projectApiKeyHasher.hash(generated.rawKey()),
                command.scopes(),
                ProjectApiKeyStatus.ACTIVE,
                now,
                command.expiresAt(),
                null,
                now
        );

        return new IssueProjectApiKeyResult(
                apiKey.getId(),
                generated.rawKey(),
                apiKey.getKeyPrefix(),
                apiKey.getExpiresAt()
        );
    }
}
