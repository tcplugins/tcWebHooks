package webhook.teamcity;

import java.util.Objects;

import jetbrains.buildServer.serverSide.ProjectManager;
import webhook.teamcity.exception.NonExistantProjectException;

public class ProjectIdResolverImpl implements ProjectIdResolver {
	
	private ProjectManager myProjectManager;

	public ProjectIdResolverImpl(ProjectManager projectManager) {
		myProjectManager = projectManager;
	}
	
	@Override
	public String getExternalProjectId(String internalProjectId) {
		try {
			if (Objects.isNull(internalProjectId) || internalProjectId.isEmpty()) {
				return(myProjectManager.findProjectById("_Root").getExternalId());
			}
			return myProjectManager.findProjectById(internalProjectId).getExternalId();
		} catch (NullPointerException e) {
			throw new NonExistantProjectException("No project found with matching internal Id:" + internalProjectId, internalProjectId);
		}
	}

	@Override
	public String getInternalProjectId(String externalProjectId) {
		try {
			if (Objects.isNull(externalProjectId) || externalProjectId.isEmpty()) {
				return(myProjectManager.findProjectByExternalId("_Root").getProjectId());
			}			
			return myProjectManager.findProjectByExternalId(externalProjectId).getProjectId();
		} catch (NullPointerException npe) {
			throw new NonExistantProjectException("No project found with matching external Id:" + externalProjectId, externalProjectId);
		}
	}

}
