package webhook.teamcity.settings.listener;

import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.BuildTypeSettings;
import jetbrains.buildServer.serverSide.CopiedObjects;
import jetbrains.buildServer.serverSide.CustomSettingsMapper;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.util.EventDispatcher;
import webhook.teamcity.Loggers;

public class BuildAndProjectMoveOrCopyListener extends BuildServerAdapter implements CustomSettingsMapper {

	public BuildAndProjectMoveOrCopyListener(@NotNull EventDispatcher<BuildServerListener> dispatcher) {
		dispatcher.addListener(this);
	}

	/**
	 * Handle Project Moved event.
	 */
	@Override
	public void projectMoved(@NotNull final SProject project, @NotNull final SProject originalParentProject) {
		Loggers.SERVER.info("Moving webhooks for Project " + project.getExternalId()
		+ " from project " + originalParentProject.getExternalId() + " to " + project.getParentProject().getExternalId());

	}

	/**
	 * Handle Build Moved event.
	 */
	@Override
	public void buildTypeMoved(@NotNull final SBuildType buildType, @NotNull final SProject original) {
		Loggers.SERVER.info("Moving webhooks for Build " + buildType.getExternalId()
				+ " from project " + original.getExternalId() + " to " + buildType.getProject().getExternalId());

	}

	/**
	 * Handle copied Builds and Projects
	 */
	@Override
	public void mapData(CopiedObjects copiedObjects) {

		for (Entry<SProject, SProject> projectEntry : copiedObjects.getCopiedProjectsMap().entrySet()) {
			copyWebHooksToNewProject(projectEntry.getKey(), projectEntry.getValue());
		}
		for (Entry<BuildTypeSettings, BuildTypeSettings> buildTypeEntry : copiedObjects.getCopiedSettingsMap().entrySet()) {
			copyWebHooksToNewBuild(buildTypeEntry.getKey(), buildTypeEntry.getValue());			
		}

	}

	private void copyWebHooksToNewBuild(BuildTypeSettings oldBuildTypeSettings, BuildTypeSettings newBuildTypeSettings) {
		Loggers.SERVER.info("Copying webhooks from Build in project " 
					+ oldBuildTypeSettings.getProject().getExternalId()
					+ " to "
					+ newBuildTypeSettings.getProject().getExternalId()
				);
		
	}

	private void copyWebHooksToNewProject(SProject oldProject, SProject newProject) {
		Loggers.SERVER.info("Copying webhooks from project " 
				+ oldProject.getExternalId()
				+ " to "
				+ newProject.getExternalId()
			);		
	}

}
