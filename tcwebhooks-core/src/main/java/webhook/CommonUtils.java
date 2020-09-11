package webhook;

import jetbrains.buildServer.serverSide.SProject;

public class CommonUtils {
	
	public static String getSensibleProjectName(SProject project){
		if (project.getProjectId().equals(Constants.ROOT_PROJECT_ID)) {
			return project.getProjectId();
		}
		return project.getName();
	}

	public static String getSensibleProjectFullName(SProject project){
		if (project.getProjectId().equals(Constants.ROOT_PROJECT_ID)) {
			return project.getProjectId();
		}
		return project.getFullName();
	}
}
