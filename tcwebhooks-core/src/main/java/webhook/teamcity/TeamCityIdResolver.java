package webhook.teamcity;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;

public final class TeamCityIdResolver {
	
	public static String getBuildTypeId(SBuildType buildType){
		try {
			return buildType.getExternalId();
		} catch (NoSuchMethodError ex) {
			return buildType.getBuildTypeId();
		}
	}
	
	public static String getExternalBuildId(SBuildType buildType){
		try {
			return buildType.getExternalId();
		} catch (NoSuchMethodError ex) {
			return buildType.getBuildTypeId();
		}
	}
	
	public static String getExternalBuildIdOrNull(SBuildType buildType){
		try {
			return buildType.getExternalId();
		} catch (NoSuchMethodError ex) {
			return null;
		}
	}
	
	public static String getInternalBuildId(SBuildType buildType){
		try {
			return buildType.getInternalId();
		} catch (NoSuchMethodError ex) {
			return buildType.getBuildTypeId();
		}
	}
	
	public static String getInternalBuildIdOrNull(SBuildType buildType){
		try {
			return buildType.getInternalId();
		} catch (NoSuchMethodError ex) {
			return null;
		}
	}
	
	public static String getProjectId(SProject project){
		try {
			return project.getExternalId();
		} catch (NoSuchMethodError ex) {
			return project.getProjectId();
		}
	}

	public static String getInternalProjectId(SProject project){
		return project.getProjectId();
	}
	
	public static String getExternalProjectId(SProject project){
		try {
			return project.getExternalId();
		} catch (NoSuchMethodError ex) {
			return project.getProjectId();
		}
	}
	
	public static String getExternalProjectIdOrNull(SProject project){
		try {
			return project.getExternalId();
		} catch (NoSuchMethodError ex) {
			return null;
		}
	}

	/**
	 * Finds a TeamCity project in the ProjectManager by ProjectId.
	 * Uses findProjectByExternalId() if available, otherwise uses findProjectById()
	 * @param TeamCity projectManager instance
	 * @param projectId string
	 * @return TeamCity Project Config object
	 */
	public static SProject findProjectById(ProjectManager projectManager, String projectId) {
		try {
			return projectManager.findProjectByExternalId(projectId);
		} catch (NoSuchMethodError ex){
			return projectManager.findProjectById(projectId);
		}
	}

}
