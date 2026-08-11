package com.labflow.project.project;

public record ProjectCreateRequest(String name, String description, ProjectVisibility visibility) {
    public ProjectCreateCommand toCommand() {
        return new ProjectCreateCommand(
                name,
                description,
                visibility
        );
    }
}
