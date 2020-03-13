package webhook.teamcity.payload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;

import webhook.teamcity.Loggers;
import webhook.teamcity.ProbableJaxbJarConflictErrorException;
import webhook.teamcity.payload.template.WebHookTemplateFromXml;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.config.builder.WebHookTemplateConfigBuilder;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.settings.entity.WebHookTemplates;

public class WebHookTemplateManager {
	
	private static final String TEMPLATES_ARE_RANKED_IN_THE_FOLLOWING_ORDER = " items long. Templates are ranked in the following order..";
	private static final String TEMPLATE_NAME = " :: Template Name: ";
	private static final String TEMPLATES_LIST_IS = " :: Templates list is ";
	private HashMap<String, WebHookPayloadTemplate> springTemplates = new HashMap<>();
	private HashMap<String, WebHookPayloadTemplate> xmlConfigTemplates = new HashMap<>();
	private Comparator<WebHookPayloadTemplate> rankComparator = new WebHookTemplateRankingComparator();
	private List<WebHookPayloadTemplate> orderedTemplateCollection = new ArrayList<>();
	private final WebHookPayloadManager webHookPayloadManager;
	private final WebHookTemplateJaxHelper webHookTemplateJaxHelper;
	private String configFilePath;
	
	public WebHookTemplateManager(
			WebHookPayloadManager webHookPayloadManager, 
			WebHookTemplateJaxHelper webHookTemplateJaxHelper)
	{
		this.webHookPayloadManager = webHookPayloadManager;
		this.webHookTemplateJaxHelper = webHookTemplateJaxHelper;
		Loggers.SERVER.debug("WebHookTemplateManager :: Starting (" + toString() + ")");
	}
	
	public void registerTemplateFormatFromSpring(WebHookPayloadTemplate payloadTemplate){
		synchronized (orderedTemplateCollection) {
			Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Registering Spring template " 
					+ payloadTemplate.getTemplateDescription() + " (" + payloadTemplate.getTemplateId() + ")"
					+ " with rank of " + payloadTemplate.getRank());
			springTemplates.put(payloadTemplate.getTemplateId(),payloadTemplate);
			rebuildOrderedListOfTemplates();
			Loggers.SERVER.debug(this.getClass().getSimpleName() + TEMPLATES_LIST_IS + this.orderedTemplateCollection.size() + TEMPLATES_ARE_RANKED_IN_THE_FOLLOWING_ORDER);
			for (WebHookPayloadTemplate pl : this.orderedTemplateCollection){
				Loggers.SERVER.debug(this.getClass().getSimpleName() + TEMPLATE_NAME + pl.getTemplateDescription() + " (" + pl.getTemplateId() + ")" + " Rank: " + pl.getRank());
			}
		}
	}
	
	private void registerTemplateFormatFromXmlEntityUnsyncd(WebHookTemplateEntity payloadTemplate){
		Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Registering XML template " 
				+ payloadTemplate.getId() 
				+ " with rank of " + payloadTemplate.getRank());
		payloadTemplate.fixTemplateIds();
		xmlConfigTemplates.put(payloadTemplate.getId(),WebHookTemplateFromXml.build(payloadTemplate, webHookPayloadManager));
	}
	
	
	
	public void registerTemplateFormatFromXmlEntity(WebHookTemplateEntity payloadTemplate){
		synchronized (orderedTemplateCollection) {
			registerTemplateFormatFromXmlEntityUnsyncd(payloadTemplate);	
			rebuildOrderedListOfTemplates();
			Loggers.SERVER.debug(this.getClass().getSimpleName() + TEMPLATES_LIST_IS + this.orderedTemplateCollection.size() + TEMPLATES_ARE_RANKED_IN_THE_FOLLOWING_ORDER);
			for (WebHookPayloadTemplate pl : this.orderedTemplateCollection){
				Loggers.SERVER.debug(this.getClass().getSimpleName() + TEMPLATE_NAME + pl.getTemplateId() + " Rank: " + pl.getRank());
			}
		}
	}
	
	public void registerTemplateFormatFromXmlConfig(WebHookTemplateConfig payloadTemplateConfig){
		WebHookTemplateEntity payloadTemplate = WebHookTemplateConfigBuilder.buildEntity(payloadTemplateConfig);
		synchronized (orderedTemplateCollection) {
			registerTemplateFormatFromXmlEntityUnsyncd(payloadTemplate);	
			rebuildOrderedListOfTemplates();
			Loggers.SERVER.debug(this.getClass().getSimpleName() + TEMPLATES_LIST_IS + this.orderedTemplateCollection.size() + TEMPLATES_ARE_RANKED_IN_THE_FOLLOWING_ORDER);
			for (WebHookPayloadTemplate pl : this.orderedTemplateCollection){
				Loggers.SERVER.debug(this.getClass().getSimpleName() + TEMPLATE_NAME + pl.getTemplateId() + " Rank: " + pl.getRank());
			}
		}
	}
	
	private void unregisterAllXmlConfigTemplates(){
			Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: un-registering all XML config templates.");
			xmlConfigTemplates.clear();
			rebuildOrderedListOfTemplates();
			Loggers.SERVER.debug(this.getClass().getSimpleName() + TEMPLATES_LIST_IS + this.orderedTemplateCollection.size() + TEMPLATES_ARE_RANKED_IN_THE_FOLLOWING_ORDER);
			for (WebHookPayloadTemplate pl : this.orderedTemplateCollection){
				Loggers.SERVER.debug(this.getClass().getSimpleName() + TEMPLATE_NAME + pl.getTemplateId() + " Rank: " + pl.getRank());
			}
	}
	
	public boolean persistAllXmlConfigTemplates() throws ProbableJaxbJarConflictErrorException{
		synchronized (orderedTemplateCollection) {
			WebHookTemplates templates = new WebHookTemplates();
			for (WebHookPayloadTemplate xmlConfig : xmlConfigTemplates.values()){
				templates.addWebHookTemplate(xmlConfig.getAsEntity());
			}
			try {
				webHookTemplateJaxHelper.writeTemplates(templates, configFilePath);
				return true;
			} catch (JAXBException jaxbException){
				if (jaxbException.getMessage().startsWith("ClassCastException")) {
					throw new ProbableJaxbJarConflictErrorException(jaxbException);
				}
				Loggers.SERVER.debug(jaxbException);
				return false;
			} 
		}
	}
	
	public boolean xmlTemplateExists(String name){
		return xmlConfigTemplates.containsKey(name);
	}

	private void rebuildOrderedListOfTemplates() {
		this.orderedTemplateCollection.clear();
		
		HashMap<String, WebHookPayloadTemplate> combinedTemplates = new HashMap<>();
		
		// Rebuild the list of configured templates.
		// Add all the spring ones.
		for (WebHookPayloadTemplate payloadTemplate : springTemplates.values()){
			combinedTemplates.put(payloadTemplate.getTemplateId(), payloadTemplate);
		}
		
		// Now add the XML ones. If any have the same name
		// as a spring one, it should overwrite it. 
		// If we've just cleared the XML ones, the list will be empty of course.
		for (WebHookPayloadTemplate payloadTemplate : xmlConfigTemplates.values()){
			combinedTemplates.put(payloadTemplate.getTemplateId(), payloadTemplate);
		}
		
		this.orderedTemplateCollection.addAll(combinedTemplates.values());
		Collections.sort(this.orderedTemplateCollection, rankComparator);
	}

	public WebHookPayloadTemplate getTemplate(String formatShortname){
		synchronized (orderedTemplateCollection) {
			if (xmlConfigTemplates.containsKey(formatShortname)){
				return xmlConfigTemplates.get(formatShortname);
			}
			if (springTemplates.containsKey(formatShortname)){
				return springTemplates.get(formatShortname);
			}
			return null;
		}
	}
	
	public WebHookTemplateConfig getTemplateConfig(String templateId, TemplateState templateState){
		synchronized (orderedTemplateCollection) {
			if (springTemplates.containsKey(templateId) 
					&& xmlConfigTemplates.containsKey(templateId)
					&& TemplateState.PROVIDED.equals(templateState))
			{
				return WebHookTemplateConfigBuilder.buildConfig(springTemplates.get(templateId).getAsEntity());
			}
			if (xmlConfigTemplates.containsKey(templateId) 
				&& ( TemplateState.BEST.equals(templateState) || getTemplateState(templateId, templateState).equals(templateState)))
			{
				return WebHookTemplateConfigBuilder.buildConfig(xmlConfigTemplates.get(templateId).getAsEntity());
			}
			if (springTemplates.containsKey(templateId)
				&& ( TemplateState.BEST.equals(templateState) || TemplateState.PROVIDED.equals(templateState) || getTemplateState(templateId, templateState).equals(templateState)))
			{
				return WebHookTemplateConfigBuilder.buildConfig(springTemplates.get(templateId).getAsEntity());
			}
			return null;
		}
	}
	
	public Boolean isRegisteredTemplate(String template){
		return xmlConfigTemplates.containsKey(template) || springTemplates.containsKey(template);
	}
	
	public List<WebHookPayloadTemplate> getRegisteredTemplates(){
		return orderedTemplateCollection;
	}
	
	public List<WebHookTemplateConfig> getRegisteredTemplateConfigs(){
		List<WebHookTemplateConfig> orderedEntities = new ArrayList<>();
		for (WebHookPayloadTemplate xmlConfig : orderedTemplateCollection){
			if (xmlConfig.getAsEntity()!=null){
				orderedEntities.add(WebHookTemplateConfigBuilder.buildConfig(xmlConfig.getAsEntity()));
			}
		}
		return orderedEntities;
	}

	public TemplateState getTemplateState(String template, TemplateState templateState){
		if ((TemplateState.BEST.equals(templateState) || TemplateState.USER_OVERRIDDEN.equals(templateState)) && springTemplates.containsKey(template) && xmlConfigTemplates.containsKey(template)){
			return TemplateState.USER_OVERRIDDEN;
		} else if (springTemplates.containsKey(template)){
			return TemplateState.PROVIDED;
		} else if (xmlConfigTemplates.containsKey(template)){
			return TemplateState.USER_DEFINED;
		}
		return TemplateState.UNKNOWN;
	}
	
	public static enum TemplateState {
		PROVIDED 		("Template bundled with tcWebhooks"), 
		USER_DEFINED 	("User defined template"), 
		USER_OVERRIDDEN ("Overridden by user defined template"), 
		BEST			("Template in its most specific state"), // Only used for finding. Template will never actually be in this state. 
		UNKNOWN			("Unknown origin");
		
		private final String description;
		private TemplateState(String description){
			this.description = description;
		}

		public String getDescription() {
			return this.description;
		} 
		
		public boolean isStateProvided()
		{
		    return TemplateState.PROVIDED.equals(this);
		}
		public boolean isStateUserDefined()
		{
			return TemplateState.USER_DEFINED.equals(this);
		}
		public boolean isStateUserOverridden()
		{
			return TemplateState.USER_OVERRIDDEN.equals(this);
		}
		public boolean isStateUnknown()
		{
			return TemplateState.UNKNOWN.equals(this);
		}
		public boolean isStateAny()
		{
			return TemplateState.BEST.equals(this);
		}
	}

	public void setConfigFilePath(String configFilePath) {
		this.configFilePath = configFilePath;
		
	}

	public boolean removeXmlConfigTemplateFormat(String name) {
		synchronized (orderedTemplateCollection) {
			if (isRegisteredTemplate(name)) {
				xmlConfigTemplates.remove(name);
				rebuildOrderedListOfTemplates();
				
				Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Deleting XML template " 
						+ name);
				rebuildOrderedListOfTemplates();
				Loggers.SERVER.debug(this.getClass().getSimpleName() + TEMPLATES_LIST_IS + this.orderedTemplateCollection.size() + TEMPLATES_ARE_RANKED_IN_THE_FOLLOWING_ORDER);
				for (WebHookPayloadTemplate pl : this.orderedTemplateCollection){
					Loggers.SERVER.debug(this.getClass().getSimpleName() + TEMPLATE_NAME + pl.getTemplateId() + " Rank: " + pl.getRank());
				}			
				return true;
			}
			return false;
		}
	}

	public void registerAllXmlTemplates(WebHookTemplates templatesList) {
		synchronized (orderedTemplateCollection) {
			this.unregisterAllXmlConfigTemplates();
			for (WebHookTemplateEntity template : templatesList.getWebHookTemplateList()){
				this.registerTemplateFormatFromXmlEntityUnsyncd(template);
			}
			rebuildOrderedListOfTemplates();
			Loggers.SERVER.debug(this.getClass().getSimpleName() + TEMPLATES_LIST_IS + this.orderedTemplateCollection.size() + TEMPLATES_ARE_RANKED_IN_THE_FOLLOWING_ORDER);
			for (WebHookPayloadTemplate pl : this.orderedTemplateCollection){
				Loggers.SERVER.debug(this.getClass().getSimpleName() + TEMPLATE_NAME + pl.getTemplateId() + " Rank: " + pl.getRank());
			}
		}
	}
	
}
