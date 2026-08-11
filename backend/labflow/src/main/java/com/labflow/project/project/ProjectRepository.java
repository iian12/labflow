package com.labflow.project.project;

import java.util.Optional;

public interface ProjectRepository {

    Project save(Project project);

    void update(Project project);

    Optional<Project> findById(ProjectId id);
}
