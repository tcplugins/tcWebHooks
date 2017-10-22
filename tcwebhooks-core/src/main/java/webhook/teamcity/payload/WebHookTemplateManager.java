package webhook.teamcity.payload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;

import webhook.teamcity.Loggers;
import webhook.teamcity.payload.template.WebHookTemplateFromXml;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.config.builder.WebHookTemplateConfigBuilder;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.settings.entity.WebHookTemplates;

public class WebHookTemplateManager {
	
	HashMap<String, WebHookPayloadTemplate> springTemplates = new HashMap<>();
	HashMap<String, WebHookPayloadTemplate> xmlConfigTemplates = new HashMap<>();
	Comparator<WebHookPayloadTemplate> rankComparator = new WebHookTemplateRankingComparator();
	List<WebHookPayloadTemplate> orderedTemplateCollection = new ArrayList<>();
	WebHookPayloadManager webHookPayloadManager;
	WebHookTemplateJaxHelper webHookTemplateJaxHelper;
	private String configFilePath;
	
	public WebHookTemplateManager(WebHookPayloadManager webHookPayloadManager, WebHookTemplateJaxHelper webHookTemplateJaxHelper){
		this.webHookPayloadManager = webHookPayloadManager;
		this.webHookTemplateJaxHelper = webHookTemplateJaxHelper;
		Loggers.SERVER.info("WebHookTemplateManager :: Starting (" + toString() + ")");
	}
	
	public void registerTemplateFormatFromSpring(WebHookPayloadTemplate payloadTemplate){
		synchronized (orderedTemplateCollection) {
			Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Registering Spring template " 
					+ payloadTemplate.getTemplateDescription() + " (" + payloadTemplate.getTemplateShortName() + ")"
					+ " with rank of " + payloadTemplate.getRank());
			springTemplates.put(payloadTemplate.getTemplateShortName(),payloadTemplate);
			rebuildOrderedListOfTemplates();
			Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Templates list is " + this.orderedTemplateCollection.size() + " items long. Templates are ranked in the following order..");
			for (WebHookPayloadTemplate pl : this.orderedTemplateCollection){
				Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Template Name: " + pl.getTemplateDescription() + " (" + pl.getTemplateShortName() + ")" + " Rank: " + pl.getRank());
			}
		}
	}
	
	public void registerTemplateFormatFromXmlEntity(WebHookTemplateEntity payloadTemplate){
		synchronized (orderedTemplateCollection) {
			Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Registering XML template " 
					+ payloadTemplate.getName() 
					+ " with rank of " + payloadTemplate.getRank());
			payloadTemplate.fixTemplateIds();
			xmlConfigTemplates.put(payloadTemplate.getName(),WebHookTemplateFromXml.build(payloadTemplate, webHookPayloadManager));
			rebuildOrderedListOfTemplates();
			Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Templates list is " + this.orderedTemplateCollection.size() + " items long. Templates are ranked in the following order..");
			for (WebHookPayloadTemplate pl : this.orderedTemplateCollection){
				Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Template Name: " + pl.getTemplateShortName() + " Rank: " + pl.getRank());
			}
		}
	}
	
	public void registerTemplateFormatFromXmlConfig(WebHookTemplateConfig payloadTemplateConfig){
		WebHookTemplateEntity payloadTemplate = WebHookTemplateConfigBuilder.buildEntity(payloadTemplateConfig);
		registerTemplateFormatFromXmlEntity(payloadTemplate);	
	}
	
	public void unregisterAllXmlConfigTemplates(){
		synchronized (orderedTemplateCollection) {
			Loggers.SERVER.info(this.getClass().getSimpleName() + " :: un-registering all XML config templates.");
			xmlConfigTemplates.clear();
			rebuildOrderedListOfTemplates();
			Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: Templates list is " + this.orderedTemplateCollection.size() + " items long. Templates are ranked in the following order..");
			for (WebHookPayloadTemplate pl : this.orderedTemplateCollection){
				Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: Template Name: " + pl.getTemplateShortName() + " Rank: " + pl.getRank());
			}
		}
	}
	
	public boolean persistAllXmlConfigTemplates(){
		synchronized (orderedTemplateCollection) {
			WebHookTemplates templates = new WebHookTemplates();
			for (WebHookPayloadTemplate xmlConfig : xmlConfigTemplates.values()){
				templates.addWebHookTemplate(xmlConfig.getAsEntity());
			}
			try {
				webHookTemplateJaxHelper.write(templates, configFilePath);
				return true;
			} catch (JAXBException jaxbException){
				Loggers.SERVER.debug(jaxbException);
				return false;
			} finally {
				templates = null;
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
			combinedTemplates.put(payloadTemplate.getTemplateShortName(), payloadTemplate);
		}
		
		// Now add the XML ones. If any have the same name
		// as a spring one, it should overwrite it. 
		// If we've just cleared the XML ones, the list will be empty of course.
		for (WebHookPayloadTemplate payloadTemplate : xmlConfigTemplates.values()){
			combinedTemplates.put(payloadTemplate.getTemplateShortName(), payloadTemplate);
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
	
	public WebHookTemplateConfig getTemplateConfig(String formatShortname){
		synchronized (orderedTemplateCollection) {
			if (xmlConfigTemplates.containsKey(formatShortname)){
				return WebHookTemplateConfigBuilder.buildConfig(xmlConfigTemplates.get(formatShortname).getAsEntity());
			}
			if (springTemplates.containsKey(formatShortname)){
				return WebHookTemplateConfigBuilder.buildConfig(springTemplates.get(formatShortname).getAsEntity());
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

	public List<WebHookPayloadTemplate> findAllTemplatesForFormat(String formatShortName){
		List<WebHookPayloadTemplate> matchingTemplates = new ArrayList<>();
		for (WebHookPayloadTemplate template : orderedTemplateCollection){
			if (template.supportsPayloadFormat(formatShortName)){
				matchingTemplates.add(template);
			}
		}
		return matchingTemplates;
	}
	
	public TemplateState getTemplateState(String template){
		if (springTemplates.containsKey(template) && xmlConfigTemplates.containsKey(template)){
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
	}

	public void setConfigFilePath(String configFilePath) {
		this.configFilePath = configFilePath;
		
	}

	public boolean removeXmlConfigTemplateFormat(String name) {
		if (isRegisteredTemplate(name)) {
			xmlConfigTemplates.remove(name);
			rebuildOrderedListOfTemplates();
			
			Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Deleting XML template " 
					+ name);
			rebuildOrderedListOfTemplates();
			Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Templates list is " + this.orderedTemplateCollection.size() + " items long. Templates are ranked in the following order..");
			for (WebHookPayloadTemplate pl : this.orderedTemplateCollection){
				Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Template Name: " + pl.getTemplateShortName() + " Rank: " + pl.getRank());
			}			
			return true;
		}
		return false;
	}
	
}
