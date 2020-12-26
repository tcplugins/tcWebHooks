package webhook.teamcity;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import jetbrains.buildServer.serverSide.ProjectManager;
import webhook.teamcity.exception.NonExistantProjectException;

public class ProjectAndBuildTypeResolverImpl implements ProjectIdResolver, BuildTypeIdResolver {
	
	private ProjectManager myProjectManager;

	public ProjectAndBuildTypeResolverImpl(ProjectManager projectManager) {
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

	@Override
	public String getExternalBuildTypeId(String internalBuildTypeId) {
		try {
			if (Objects.isNull(internalBuildTypeId) || internalBuildTypeId.isEmpty()) {
				return null;
			}
			return myProjectManager.findBuildTypeById(internalBuildTypeId).getExternalId();
		} catch (NullPointerException e) {
			throw new NonExistantProjectException("No build type found with matching internal Id:" + internalBuildTypeId, internalBuildTypeId);
		}
	}

	@Override
	public String getInternalBuildTypeId(String externalBuildTypeId) {
		try {
			if (Objects.isNull(externalBuildTypeId) || externalBuildTypeId.isEmpty()) {
				return(myProjectManager.findProjectByExternalId("_Root").getProjectId());
			}			
			return myProjectManager.findBuildTypeByExternalId(externalBuildTypeId).getInternalId();
		} catch (NullPointerException npe) {
			throw new NonExistantProjectException("No build type found with matching external Id:" + externalBuildTypeId, externalBuildTypeId);
		}
	}

	@Override
	public Set<String> getExternalBuildTypeIds(Collection<String> internalBuildTypeIds) {
		Set<String> ids = new LinkedHashSet<>();
		for (String internalBuildTypeId : internalBuildTypeIds) {
			ids.add(getExternalBuildTypeId(internalBuildTypeId));
		}
		return ids;
	}

	@Override
	public Set<String> getInternalBuildTypeIds(Collection<String> externalBuildTypeIds) {
		Set<String> ids = new LinkedHashSet<>();
		for (String externalBuildTypeId : externalBuildTypeIds) {
			ids.add(getInternalBuildTypeId(externalBuildTypeId));
		}
		return ids;
	}

}
