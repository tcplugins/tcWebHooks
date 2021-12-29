package webhook.teamcity.settings.project;

public interface WebHookParameter {
	
	public String getId();
	public void setId(String id);
	
	public String getName();
	public void setName(String name);
	
	public String getValue();
	public void setValue(String value);
	
	public Boolean getSecure();
	public void setSecure(Boolean isSecure);
	
	public Boolean getIncludedInLegacyPayloads();
	public void setIncludedInLegacyPayloads(Boolean isIncluded);
	public Boolean getForceResolveTeamCityVariable();
	public void setForceResolveTeamCityVariable(Boolean isForceResolved);
	
	public String getTemplateEngine();
	
	public void setTemplateEngine(String payloadTemplateEngineType);
	
}
