package webhook.teamcity.payload.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplate;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;

public class WebHookTemplateFromXml implements WebHookTemplate {
	
	List<String> supportedFormats = new ArrayList<>();
	Map<BuildStateEnum,WebHookTemplateContent> templateContent = new HashMap<>();
	Map<BuildStateEnum,WebHookTemplateContent> branchTemplateContent = new HashMap<>();
	
	protected WebHookTemplateManager manager;
	private int rank = 10; // Default to 10.
	private String shortName = "";
	private String toolTipText = "";
	private String description = "";
	private String preferredDateTimeFormat = "";

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
		/*
		 *  We are a special case. We don't need to do anything here.
		 *  Templates are registered by the file watcher, which is started 
		 *  in TemplateManager's register method.
		 *  
		 *  Most other templates should register themselves via Spring,
		 *  in which case, this method is used.
		 */
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
		for (String format : supportedFormats){
			if (payloadFormat.equalsIgnoreCase(format)){
				return true;
			}
		}
		return false;
	}

	@Override
	public WebHookTemplateContent getTemplateForState(BuildStateEnum buildState) {
		if (templateContent.containsKey(buildState)){
			return (templateContent.get(buildState)).copy(); 
		}
		return null;
	}
	
	@Override
	public WebHookTemplateContent getBranchTemplateForState(BuildStateEnum buildState) {
		if (branchTemplateContent.containsKey(buildState)){
			return (branchTemplateContent.get(buildState)).copy(); 
		}
		return null;
	}
	
	private void addTemplateContentForState(BuildStateEnum state, WebHookTemplateContent content){
		this.templateContent.put(state, content);
	}
	
	private void addBranchTemplateContentForState(BuildStateEnum state, WebHookTemplateContent content){
		this.branchTemplateContent.put(state, content);
	}

	public static WebHookTemplate build(
			webhook.teamcity.settings.entity.WebHookTemplate entityTemplate,
			WebHookPayloadManager payloadManager
			) {
		WebHookTemplateFromXml template = new WebHookTemplateFromXml();
		template.setRank(entityTemplate.getRank());
		template.setTemplateShortName(entityTemplate.getName());
		template.setPreferredDateTimeFormat(entityTemplate.getPreferredDateTimeFormat());
		
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
			for (BuildStateEnum state : BuildStateEnum.getNotifyStates()){
				
				template.addTemplateContentForState(state, WebHookTemplateContent.create(
						state.getShortName(), 
						entityTemplate.getDefaultTemplate(),
						true,
						template.getPreferredDateTimeFormat()));
				
			}
		}
		
		// If a default branch template is set, populate all BuildStates with it.
		// We will override later if we find a buildState specific one. 
		if (entityTemplate.getDefaultBranchTemplate() != null){
			for (BuildStateEnum state : BuildStateEnum.getNotifyStates()){
				
				template.addBranchTemplateContentForState(state, WebHookTemplateContent.create(
						state.getShortName(), 
						entityTemplate.getDefaultBranchTemplate(),
						true,
						template.getPreferredDateTimeFormat()));
				
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
									item.getTemplateText(),
									true,
									template.getPreferredDateTimeFormat()
									));
						}
					}
				}
			}
		}
		
		for (webhook.teamcity.settings.entity.WebHookTemplate.WebHookTemplateItem item : entityTemplate.getTemplates()){
			if (item.isEnabled() && item.getBranchTemplateText()!= null){
				for (webhook.teamcity.settings.entity.WebHookTemplate.WebHookTemplateState state :item.getStates()){
					if (state.isEnabled()){
						BuildStateEnum bse =  BuildStateEnum.findBuildState(state.getType());
						if (bse != null){
							template.addBranchTemplateContentForState(bse, WebHookTemplateContent.create(
									bse.getShortName(), 
									item.getBranchTemplateText(),
									true,
									template.getPreferredDateTimeFormat()));
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
	

	private void setPreferredDateTimeFormat(String preferredDateTimeFormat) {
		this.preferredDateTimeFormat = preferredDateTimeFormat;
	}

	@Override
	public Set<BuildStateEnum> getSupportedBuildStates() {
		return templateContent.keySet();
	}

	@Override
	public Set<BuildStateEnum> getSupportedBranchBuildStates() {
		return branchTemplateContent.keySet();
	}

	@Override
	public String getPreferredDateTimeFormat() {
		return this.preferredDateTimeFormat;
	}

}
