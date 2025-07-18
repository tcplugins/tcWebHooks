package webhook.teamcity;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import webhook.teamcity.settings.ProjectFeatureDescriptorSorter;

public class TeamCityCoreFacadeImpl implements TeamCityCoreFacade {
	private final ProjectManager projectManager;
	private final int PROJECT_FEATURE_PREFIX_LENGTH = "PROJECT_EXT_".length(); //NOSONAR

	public TeamCityCoreFacadeImpl(ProjectManager projectManager) {
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

	@NotNull
	@Override
	public ProjectVcsStatus getProjectVcsStatus(SProject sProject) {
		ProjectVcsStatus status = new ProjectVcsStatus(false, "xml", true);
		Collection<SProjectFeatureDescriptor> versionedSettings = sProject.getAvailableFeaturesOfType("versionedSettings");
		Optional<SProjectFeatureDescriptor> vs = versionedSettings.stream().findFirst();
		if (vs.isPresent()){
			Map<String, String> params = vs.get().getParameters();
			if (params.containsKey("enabled")) {
				status.setVcsEnabled(Boolean.parseBoolean(params.get("enabled")));
			} else {
				// If the 'versionedSettings' feature is present, but we have no "enabled" key, default to enabled.
				status.setVcsEnabled(true);
			}
			if (params.containsKey("format")) {
				status.setVcsFormat(params.get("format"));
			}
			if (params.containsKey("twoWaySynchronization")) {
				status.setVcsSyncEnabled(Boolean.parseBoolean(params.get("twoWaySynchronization")));
			}
		}
		return status;
	}

    @Override
    public int getMaxDescripterId(SProject sProject) {
        final ProjectFeatureDescriptorSorter featureDescriptorSorter = new ProjectFeatureDescriptorSorter();
        return sProject.getOwnFeatures()
                .stream()
                .sorted(featureDescriptorSorter.reversed())
                .map(f -> Integer.parseInt(f.getId().substring(PROJECT_FEATURE_PREFIX_LENGTH)))
                .findFirst()
                .orElse(0);
    }

}