package com.labflow.project.key.application;

import com.labflow.project.key.domain.ProjectApiKey;
import com.labflow.project.key.util.ProjectApiKeyHasher;
import com.labflow.project.key.domain.ProjectApiKeyPrincipal;
import com.labflow.project.key.domain.repository.ProjectApiKeyRepository;
import org.springframework.stereotype.Service;
import com.labflow.project.key.exception.InvalidProjectApiKeyException;

@Service
public class ProjectApiKeyAuthenticationService {

    private final ProjectApiKeyRepository projectApiKeyRepository;
    private final ProjectApiKeyHasher projectApiKeyHasher;

    public ProjectApiKeyAuthenticationService(ProjectApiKeyRepository projectApiKeyRepository, ProjectApiKeyHasher projectApiKeyHasher) {
        this.projectApiKeyRepository = projectApiKeyRepository;
        this.projectApiKeyHasher = projectApiKeyHasher;
    }

    public ProjectApiKeyPrincipal authenticate(String rawApiKey) {
        if (rawApiKey == null || rawApiKey.isBlank()) {
            throw new InvalidProjectApiKeyException("Invalid API key");
        }
        String keyHash = projectApiKeyHasher.hash(rawApiKey);
        ProjectApiKey apiKey = projectApiKeyRepository.findByKeyHash(keyHash)
                .orElseThrow(() -> new InvalidProjectApiKeyException("Invalid API key"));
        try {
            apiKey.validateUsable();
        } catch (IllegalStateException e) {
            throw new InvalidProjectApiKeyException("Invalid API key");
        }

        return new ProjectApiKeyPrincipal(
                apiKey.getId(),
                apiKey.getProjectId(),
                apiKey.getCreatedBy()
        );
    }
}
