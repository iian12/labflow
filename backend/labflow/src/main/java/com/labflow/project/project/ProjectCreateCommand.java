package com.labflow.project.project;

public record ProjectCreateCommand(String name, String description, ProjectVisibility visibility) {
}
