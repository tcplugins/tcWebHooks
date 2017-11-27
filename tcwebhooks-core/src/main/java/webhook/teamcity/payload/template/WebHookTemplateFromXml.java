package webhook.teamcity.payload.template;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.config.builder.WebHookTemplateConfigBuilder;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;

public class WebHookTemplateFromXml implements WebHookPayloadTemplate {
	
	private static final String BRANCH_TYPE_BRANCH = "branch";
	private static final String BRANCH_TYPE_NON_BRANCH = "nonBranch";
	List<String> supportedFormats = new ArrayList<>();
	Map<BuildStateEnum,WebHookTemplateContent> templateContent = new EnumMap<>(BuildStateEnum.class);
	Map<BuildStateEnum,WebHookTemplateContent> branchTemplateContent = new EnumMap<>(BuildStateEnum.class);
	
	protected WebHookTemplateManager templateManager;
	private int rank = 10; // Default to 10.
	private String id = "";
	private String toolTipText = "";
	private String description = "";
	private String preferredDateTimeFormat = "";
	private WebHookTemplateEntity entity;

	@Override
	public void setTemplateManager(WebHookTemplateManager webhookTemplateManager) {
		this.templateManager = webhookTemplateManager;
	}

	@Override
	public int getRank() {
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
	public String getTemplateToolTip() {
		return this.toolTipText;
	}
	
	public void setTemplateToolTipText(String toolTipText) {
		this.toolTipText = toolTipText;
	}

	@Override
	public String getTemplateId() {
		return this.id;
	}
	
	public void setTemplateId(String id){
		this.id = id;
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
		throw new UnSupportedBuildStateException(buildState, BRANCH_TYPE_NON_BRANCH, this.getTemplateId(), this.getTemplateDescription(), this.getSupportedBranchBuildStates());
	}
	
	@Override
	public WebHookTemplateContent getBranchTemplateForState(BuildStateEnum buildState) {
		if (branchTemplateContent.containsKey(buildState)){
			return (branchTemplateContent.get(buildState)).copy(); 
		}
		throw new UnSupportedBuildStateException(buildState, BRANCH_TYPE_BRANCH, this.getTemplateId(), this.getTemplateDescription(), this.getSupportedBranchBuildStates());
	}
	
	private void addTemplateContentForState(BuildStateEnum state, WebHookTemplateContent content){
		this.templateContent.put(state, content);
	}
	
	private void addBranchTemplateContentForState(BuildStateEnum state, WebHookTemplateContent content){
		this.branchTemplateContent.put(state, content);
	}

	public static WebHookPayloadTemplate build(
			WebHookTemplateEntity entityTemplate,
			WebHookPayloadManager payloadManager
			) {
		WebHookTemplateFromXml template = new WebHookTemplateFromXml();
		template.entity = entityTemplate;
		template.setRank(entityTemplate.getRank());
		template.setTemplateId(entityTemplate.getId());
		template.setPreferredDateTimeFormat(entityTemplate.getPreferredDateTimeFormat());
		
		if (entityTemplate.getTemplateDescription() != null){
			template.setTemplateDescription(entityTemplate.getTemplateDescription());
		}
		
		if (entityTemplate.getTemplateToolTip() != null){
			template.setTemplateToolTipText(entityTemplate.getTemplateToolTip());
		} else if( entityTemplate.getTemplateDescription() != null){
			template.setTemplateToolTipText(entityTemplate.getId() + ":" + entityTemplate.getTemplateDescription());
		} else {
			template.setTemplateToolTipText(entityTemplate.getId());
		}
		
		// If a default template is set, populate all BuildStates with it.
		// We will override later if we find a buildState specific one. 
		if (entityTemplate.getDefaultTemplate() != null){
			for (BuildStateEnum state : BuildStateEnum.getNotifyStates()){
				
				template.addTemplateContentForState(state, WebHookTemplateContent.create(
						state.getShortName(), 
						entityTemplate.getDefaultTemplate().getTemplateContent(),
						true,
						template.getPreferredDateTimeFormat()));
				
			}
		}
		
		// If a default branch template is set, populate all BuildStates with it.
		// We will override later if we find a buildState specific one. 
		if ((entityTemplate.getDefaultTemplate() != null && entityTemplate.getDefaultTemplate().isUseTemplateTextForBranch()) || entityTemplate.getDefaultBranchTemplate() != null){
			for (BuildStateEnum state : BuildStateEnum.getNotifyStates()){
				if (entityTemplate.getDefaultTemplate().isUseTemplateTextForBranch()){
					template.addBranchTemplateContentForState(state, WebHookTemplateContent.create(
							state.getShortName(), 
							entityTemplate.getDefaultTemplate().getTemplateContent(),
							true,
							template.getPreferredDateTimeFormat()));
				} else {
					template.addBranchTemplateContentForState(state, WebHookTemplateContent.create(
							state.getShortName(), 
							entityTemplate.getDefaultBranchTemplate().getTemplateContent(),
							true,
							template.getPreferredDateTimeFormat()));
				}
			}
		}
		if (entityTemplate.getTemplates() != null){
			for (webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateItem item : entityTemplate.getTemplates().getTemplates()){
				if (item.getTemplateText() != null){
					for (webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateState state :item.getStates()){
						if (state.isEnabled()){
							BuildStateEnum bse =  BuildStateEnum.findBuildState(state.getType());
							if (bse != null){
								template.addTemplateContentForState(bse, WebHookTemplateContent.create(
										bse.getShortName(), 
										item.getTemplateText().getTemplateContent(),
										true,
										template.getPreferredDateTimeFormat()
										));
							}
						}
					}
				}
			}
			
			for (webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateItem item : entityTemplate.getTemplates().getTemplates()){
				if ((item.getTemplateText()!= null && item.getTemplateText().isUseTemplateTextForBranch()) || item.getBranchTemplateText()!= null){
					for (webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateState state :item.getStates()){
						if (state.isEnabled()){
							BuildStateEnum bse =  BuildStateEnum.findBuildState(state.getType());
							if (bse != null){
								if (item.getTemplateText() != null && item.getTemplateText().isUseTemplateTextForBranch()){
									template.addBranchTemplateContentForState(bse, WebHookTemplateContent.create(
											bse.getShortName(), 
											item.getTemplateText().getTemplateContent(),
											true,
											template.getPreferredDateTimeFormat()));
								} else {
									template.addBranchTemplateContentForState(bse, WebHookTemplateContent.create(
											bse.getShortName(), 
											item.getBranchTemplateText().getTemplateContent(),
											true,
											template.getPreferredDateTimeFormat()));
								}
							}
						}
					}
				}
			}
		} // End if entityTemplate.getTemplates() != null
		
		if (entityTemplate.getFormat() != null && payloadManager.isRegisteredFormat(entityTemplate.getFormat())){
			template.supportedFormats.add(entityTemplate.getFormat());
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
	
	public void persist(){
		
	}

	@Override
	public WebHookTemplateEntity getAsEntity() {
		return entity;
	}

	@Override
	public WebHookTemplateConfig getAsConfig() {
		return WebHookTemplateConfigBuilder.buildConfig(getAsEntity());
	}



}
