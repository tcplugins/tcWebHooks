package webhook.teamcity;

import java.util.Collection;
import java.util.Set;

public interface BuildTypeIdResolver {
	
	String getExternalBuildTypeId(String internalBuildTypeId);
	Set<String> getExternalBuildTypeIds(Collection<String> internalBuildTypeIds);
	String getInternalBuildTypeId(String externalBuildTypeId);
	Set<String> getInternalBuildTypeIds(Collection<String> externalBuildTypeIds);

}
