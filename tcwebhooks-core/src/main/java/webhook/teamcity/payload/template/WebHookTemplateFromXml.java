package webhook.teamcity.payload.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplate;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.WebHookConfig;

public class WebHookTemplateFromXml implements WebHookTemplate {
	
	List<String> supportedFormats = new ArrayList<String>();
	Map<BuildStateEnum,WebHookTemplateContent> templateContent = new HashMap<BuildStateEnum, WebHookTemplateContent>();
	
	protected WebHookTemplateManager manager;
	private int rank = 10; // Default to 10.
	private String shortName = "";
	private String toolTipText = "";
	private String description = "";

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
		return this.description;
	}
	
	public void setTemplateDescription(String description) {
		this.description = description;
	}

	@Override
	public String getTemplateToolTipText() {
		return this.toolTipText;
	}
	
	public void setTemplateToolTipText(String toolTipText) {
		this.toolTipText = toolTipText;
	}

	@Override
	public String getTemplateShortName() {
		return this.shortName;
	}
	
	public void setTemplateShortName(String shortName){
		this.shortName = shortName;
	}

	@Override
	public boolean supportsPayloadFormat(String payloadFormat) {
		return supportedFormats.contains(payloadFormat);
	}

	@Override
	public WebHookTemplateContent getTemplateForState(BuildStateEnum buildState) {
		if (templateContent.containsKey(buildState)){
			return (templateContent.get(buildState)).copy(); 
		}
		return null;
	}
	
	private void addTemplateContentForState(BuildStateEnum state, WebHookTemplateContent content){
		this.templateContent.put(state, content);
	}

	public static WebHookTemplate build(
			webhook.teamcity.settings.entity.WebHookTemplate entityTemplate,
			WebHookPayloadManager payloadManager
			) {
		WebHookTemplateFromXml template = new WebHookTemplateFromXml();
		template.setRank(entityTemplate.getRank());
		template.setTemplateShortName(entityTemplate.getName());
		
		if (entityTemplate.getTemplateDescription() != null){
			template.setTemplateDescription(entityTemplate.getTemplateDescription());
		}
		
		if (entityTemplate.getTemplateToolTip() != null){
			template.setTemplateToolTipText(entityTemplate.getTemplateToolTip());
		} else if( entityTemplate.getTemplateDescription() != null){
			template.setTemplateToolTipText(entityTemplate.getName() + ":" + entityTemplate.getTemplateDescription());
		} else {
			template.setTemplateToolTipText(entityTemplate.getName());
		}
		
		// If a default template is set, populate all BuildStates with it.
		// We will override later if we find a buildState specific one. 
		if (entityTemplate.getDefaultTemplate() != null){
			for (BuildStateEnum state : BuildStateEnum.values()){
				
				template.addTemplateContentForState(state, WebHookTemplateContent.create(
						state.getShortName(), 
						entityTemplate.getDefaultTemplate(),
						true));
				
			}
		}
		
		for (webhook.teamcity.settings.entity.WebHookTemplate.WebHookTemplateItem item : entityTemplate.getTemplates()){
			if (item.isEnabled() && item.getTemplateText()!= null){
				for (webhook.teamcity.settings.entity.WebHookTemplate.WebHookTemplateState state :item.getStates()){
					if (state.isEnabled()){
						BuildStateEnum bse =  BuildStateEnum.findBuildState(state.getType());
						if (bse != null){
							template.addTemplateContentForState(bse, WebHookTemplateContent.create(
									bse.getShortName(), 
									entityTemplate.getDefaultTemplate(),
									true));
						}
					}
				}
			}
		}
		
		for (webhook.teamcity.settings.entity.WebHookTemplate.WebHookTemplateFormat format : entityTemplate.getFormats()){
			if (format.isEnabled() && payloadManager.isRegisteredFormat(format.getName())){
				template.supportedFormats.add(format.getName());
			}
		}
		return template;
	}
	

}
