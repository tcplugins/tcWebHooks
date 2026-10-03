package webhook.teamcity.test.springmock;

import java.util.Collection;
import java.util.Set;

import webhook.teamcity.BuildTypeIdResolver;
import webhook.teamcity.ProjectIdResolver;

public class MockBuildTypeIdResolver implements BuildTypeIdResolver, ProjectIdResolver{

	@Override
	public String getExternalBuildTypeId(String internalBuildTypeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getExternalBuildTypeIds(Collection<String> internalBuildTypeIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInternalBuildTypeId(String externalBuildTypeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getInternalBuildTypeIds(Collection<String> externalBuildTypeIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getExternalProjectId(String internalProjectId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInternalProjectId(String externalProjectId) {
		// TODO Auto-generated method stub
		return null;
	}

}
