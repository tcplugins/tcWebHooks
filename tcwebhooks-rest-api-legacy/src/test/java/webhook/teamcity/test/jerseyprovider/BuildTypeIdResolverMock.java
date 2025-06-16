package webhook.teamcity.test.jerseyprovider;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import webhook.teamcity.BuildTypeIdResolver;

public class BuildTypeIdResolverMock implements BuildTypeIdResolver {

	@Override
	public String getExternalBuildTypeId(String internalBuildTypeId) {
		return "MyBuild";
	}

	@Override
	public Set<String> getExternalBuildTypeIds(Collection<String> internalBuildTypeIds) {
		return Collections.singleton("MyBuild");
	}

	@Override
	public String getInternalBuildTypeId(String externalBuildTypeId) {
		return "bt1";
	}

	public Set<String> getInternalBuildTypeIds(Collection<String> externalBuildTypeIds) {
		return Collections.singleton("bt1");
	}

}
