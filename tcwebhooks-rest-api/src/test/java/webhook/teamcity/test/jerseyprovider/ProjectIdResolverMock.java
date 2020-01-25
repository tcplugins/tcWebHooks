package webhook.teamcity.test.jerseyprovider;

import webhook.teamcity.ProjectIdResolver;

public class ProjectIdResolverMock implements ProjectIdResolver {

	@Override
	public String getExternalProjectId(String internalProjectId) {
		return "TestProject";
	}

	@Override
	public String getInternalProjectId(String externalProjectId) {
		return "project1";
	}

}
