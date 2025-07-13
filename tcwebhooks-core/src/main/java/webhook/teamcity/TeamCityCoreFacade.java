package webhook.teamcity;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jetbrains.buildServer.serverSide.SProject;
import lombok.Data;

public interface TeamCityCoreFacade {

	// PROJECTS
	@Nullable
	SProject findProjectByExtId(@Nullable String projectExtId);

	@Nullable
	SProject findProjectByIntId(String projectIntId);

	@NotNull
	List<SProject> getActiveProjects();

	void persist(@NotNull String project, @NotNull String description);

	@NotNull
	ProjectVcsStatus getProjectVcsStatus(SProject sProject);
	int getMaxDescripterId(SProject sProject);

	@Data
	public static class ProjectVcsStatus {
		private boolean vcsEnabled;
		private String vcsFormat;
		private boolean vcsSyncEnabled;
		public boolean isKotlin() {
			return vcsFormat != null && vcsFormat.equalsIgnoreCase("kotlin");
		}
	}


}
