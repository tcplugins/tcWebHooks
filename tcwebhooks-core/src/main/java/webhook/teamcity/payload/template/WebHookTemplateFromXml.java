package webhook.teamcity.payload.template;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.payload.WebHookTemplate;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;

public class WebHookTemplateFromXml implements WebHookTemplate {
	
	List<String> supportedFormats;
	Map<BuildStateEnum,WebHookTemplateContent> templateContent = new HashMap<BuildStateEnum, WebHookTemplateContent>();
	
	protected WebHookTemplateManager manager;
	int rank = 10; // Default to 10.

	@Override
	public void setTemplateManager(WebHookTemplateManager webhookTemplateManager) {
		this.manager = webhookTemplateManager;
	}

	@Override
	public Integer getRank() {
		return rank;
	}

	@Override
	public void setRank(Integer rank) {
		this.rank = rank;
	}
	
	@Override
	public void register() {
		templateContent.clear();
		if (!templateContent.isEmpty()){
			this.manager.registerTemplateFormatFromXmlConfig(this);
		} else {
			Loggers.SERVER.error("WebHookTemplateFromXml :: Failed to register template " + getTemplateShortName() + ". No template configurations were found.");
		}
		
	}

	@Override
	public String getTemplateDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTemplateToolTipText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTemplateShortName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean supportsPayloadFormat(String payloadFormat) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public WebHookTemplateContent getTemplateForState(BuildStateEnum buildState) {
		if (templateContent.containsKey(buildState)){
			return (templateContent.get(buildState)).copy(); 
		}
		return null;
	}
	

}
