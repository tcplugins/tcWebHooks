package webhook.teamcity;

public interface ProjectIdResolver {
	
	String getExternalProjectId(String internalProjectId);
	String getInternalProjectId(String externalProjectId);

}
