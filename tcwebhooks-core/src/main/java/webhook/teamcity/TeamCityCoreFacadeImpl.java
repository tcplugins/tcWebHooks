package webhook.teamcity;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;

public class TeamCityCoreFacadeImpl implements TeamCityCoreFacade {
    private final ProjectManager projectManager;

    public TeamCityCoreFacadeImpl(ProjectManager projectManager) 
    {
        this.projectManager = projectManager;
    }

    @Nullable
    @Override
    public SProject findProjectByExtId(@Nullable String projectExtId) {
        return projectManager.findProjectByExternalId(projectExtId);
    }

    @Nullable
    @Override
    public SProject findProjectByIntId(String projectIntId) {
        return projectManager.findProjectById(projectIntId);
    }

    @NotNull
    @Override
    public List<SProject> getActiveProjects() {
        return projectManager.getActiveProjects();
    }

    @Override
    public void persist(@NotNull String projectId, @NotNull String description) {
    	findProjectByIntId(projectId).persist();
    }

}