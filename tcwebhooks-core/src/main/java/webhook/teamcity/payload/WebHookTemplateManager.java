package webhook.teamcity.payload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import webhook.teamcity.Loggers;
import webhook.teamcity.payload.template.WebHookTemplateFromXml;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.settings.entity.WebHookTemplates;

public class WebHookTemplateManager {
	
	HashMap<String, WebHookTemplate> springTemplates = new HashMap<String,WebHookTemplate>();
	HashMap<String, WebHookTemplateEntity> xmlConfigTemplates = new HashMap<String,WebHookTemplateEntity>();
	Comparator<WebHookTemplate> rankComparator = new WebHookTemplateRankingComparator();
	List<WebHookTemplate> orderedTemplateCollection = new ArrayList<WebHookTemplate>();
	WebHookPayloadManager webHookPayloadManager;
	private String configFilePath;
	
	public WebHookTemplateManager(WebHookPayloadManager webHookPayloadManager){
		this.webHookPayloadManager = webHookPayloadManager;
		Loggers.SERVER.info("WebHookTemplateManager :: Starting (" + toString() + ")");
	}
	
	public void registerTemplateFormatFromSpring(WebHookTemplate payloadTemplate){
		synchronized (orderedTemplateCollection) {
			Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Registering Spring template " 
					+ payloadTemplate.getTemplateDescription() + " (" + payloadTemplate.getTemplateShortName() + ")"
					+ " with rank of " + payloadTemplate.getRank());
			springTemplates.put(payloadTemplate.getTemplateShortName(),payloadTemplate);
			rebuildOrderedListOfTemplates();
			Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Templates list is " + this.orderedTemplateCollection.size() + " items long. Templates are ranked in the following order..");
			for (WebHookTemplate pl : this.orderedTemplateCollection){
				Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Template Name: " + pl.getTemplateDescription() + " (" + pl.getTemplateShortName() + ")" + " Rank: " + pl.getRank());
			}
		}
	}
	
	public void registerTemplateFormatFromXmlConfig(WebHookTemplateEntity payloadTemplate){
		synchronized (orderedTemplateCollection) {
			Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Registering XML template " 
					+ payloadTemplate.getName() 
					+ " with rank of " + payloadTemplate.getRank());
			xmlConfigTemplates.put(payloadTemplate.getName(),payloadTemplate);
			rebuildOrderedListOfTemplates();
			Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Templates list is " + this.orderedTemplateCollection.size() + " items long. Templates are ranked in the following order..");
			for (WebHookTemplate pl : this.orderedTemplateCollection){
				Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Template Name: " + pl.getTemplateShortName() + " Rank: " + pl.getRank());
			}
		}
	}
	
	public void unregisterAllXmlConfigTemplates(){
		synchronized (orderedTemplateCollection) {
			Loggers.SERVER.info(this.getClass().getSimpleName() + " :: un-registering all XML config templates.");
			xmlConfigTemplates.clear();
			rebuildOrderedListOfTemplates();
			Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: Templates list is " + this.orderedTemplateCollection.size() + " items long. Templates are ranked in the following order..");
			for (WebHookTemplate pl : this.orderedTemplateCollection){
				Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: Template Name: " + pl.getTemplateShortName() + " Rank: " + pl.getRank());
			}
		}
	}
	
	public boolean persistAllXmlConfigTemplates(){
		synchronized (orderedTemplateCollection) {
			WebHookTemplates templates = new WebHookTemplates();
			templates.addAllWebHookTemplates(xmlConfigTemplates.values());
			try {
				WebHookTemplateJaxHelper.write(templates, configFilePath);
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
		
		HashMap<String, WebHookTemplate> combinedTemplates = new HashMap<String,WebHookTemplate>();
		
		// Rebuild the list of configured templates.
		// Add all the spring ones.
		for (WebHookTemplate payloadTemplate : springTemplates.values()){
			combinedTemplates.put(payloadTemplate.getTemplateShortName(), payloadTemplate);
		}
		
		// Now add the XML ones. If any have the same name
		// as a spring one, it should overwrite it. 
		// If we've just cleared the XML ones, the list will be empty of course.
		for (WebHookTemplateEntity payloadTemplate : xmlConfigTemplates.values()){
			combinedTemplates.put(payloadTemplate.getName(), WebHookTemplateFromXml.build(payloadTemplate, this.webHookPayloadManager));
		}
		
		this.orderedTemplateCollection.addAll(combinedTemplates.values());
		Collections.sort(this.orderedTemplateCollection, rankComparator);
	}

	public WebHookTemplate getTemplate(String formatShortname){
		synchronized (orderedTemplateCollection) {
			if (xmlConfigTemplates.containsKey(formatShortname)){
				return WebHookTemplateFromXml.build(xmlConfigTemplates.get(formatShortname), this.webHookPayloadManager);
			}
			if (springTemplates.containsKey(formatShortname)){
				return springTemplates.get(formatShortname);
			}
			return null;
		}
	}
	
	public Boolean isRegisteredTemplate(String template){
		return xmlConfigTemplates.containsKey(template) || springTemplates.containsKey(template);
	}
	
	public List<WebHookTemplate> getRegisteredTemplates(){
		return orderedTemplateCollection;
	}

	public List<WebHookTemplate> findAllTemplatesForFormat(String formatShortName){
		List<WebHookTemplate> matchingTemplates = new ArrayList<WebHookTemplate>();
		for (WebHookTemplate template : orderedTemplateCollection){
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
	
}