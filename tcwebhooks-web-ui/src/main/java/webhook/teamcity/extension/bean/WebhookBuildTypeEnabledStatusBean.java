package webhook.teamcity.extension.bean;

public class WebhookBuildTypeEnabledStatusBean {
	
	boolean enabled;
	String buildTypeId;
	String buildTypeExternalId;
	String buildTypeName;
	
	public WebhookBuildTypeEnabledStatusBean(String buildTypeId, String buildTypeExternalId, String buildTypeName, boolean enabled) {
		this.buildTypeId = buildTypeId;
		this.buildTypeExternalId = buildTypeExternalId;
		this.buildTypeName = buildTypeName;
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public String getEnabledAsChecked(){
		return enabled ? "checked" : "";
	}

	public String getBuildTypeId() {
		return buildTypeId;
	}

	public String getBuildTypeName() {
		return buildTypeName;
	}
	
	public String getBuildTypeExternalId() {
		return buildTypeExternalId;
	}

}
