package webhook.teamcity;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jetbrains.buildServer.serverSide.SProject;

public interface TeamCityCoreFacade {

    //PROJECTS
    @Nullable
    SProject findProjectByExtId(@Nullable String projectExtId);

    @Nullable
    SProject findProjectByIntId(String projectIntId);

    @NotNull
    List<SProject> getActiveProjects();

    void persist(@NotNull String project, @NotNull String description);

}
