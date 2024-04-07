package webhook.teamcity.test.jerseyprovider;

import webhook.teamcity.ProjectIdResolver;

public class ProjectIdResolverMock implements ProjectIdResolver {

	@Override
	public String getExternalProjectId(String internalProjectId) {
		if (internalProjectId.equalsIgnoreCase("_Root")) {
			return "_Root";
		}
		return "TestProject";
	}

	@Override
	public String getInternalProjectId(String externalProjectId) {
		if (externalProjectId.equalsIgnoreCase("_Root")) {
			return "_Root";
		}
		return "project1";
	}

}
