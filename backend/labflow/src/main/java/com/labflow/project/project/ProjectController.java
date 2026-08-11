package com.labflow.project.project;

import com.labflow.global.resolver.CurrentUserId;
import com.labflow.user.domain.UserId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/project")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<Void> createProject(@CurrentUserId UserId currentUserId, @RequestBody ProjectCreateRequest request) {
        ProjectId projectId = projectService.create(currentUserId, request.toCommand());

        return ResponseEntity.ok().build();
    }
}
