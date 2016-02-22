package webhook.teamcity.payload;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.serverSide.ServerPaths;
import webhook.teamcity.Loggers;

public class WebHookTemplateManager {
	
	HashMap<String, WebHookTemplate> springTemplates = new HashMap<String,WebHookTemplate>();
	HashMap<String, WebHookTemplate> xmlConfigTemplates = new HashMap<String,WebHookTemplate>();
	Comparator<WebHookTemplate> rankComparator = new WebHookTemplateRankingComparator();
	List<WebHookTemplate> orderedTemplateCollection = new ArrayList<WebHookTemplate>();
	ServerPaths serverPaths;
	WebHookPayloadManager webHookPayloadManager;
	
	public WebHookTemplateManager(ServerPaths serverPaths, WebHookPayloadManager webHookPayloadManager){
		this.serverPaths = serverPaths;
		this.webHookPayloadManager = webHookPayloadManager;
		Loggers.SERVER.info("WebHookTemplateManager :: Starting");
	}
	
	public void register(){
		File configFile = new File(this.serverPaths.getConfigDir() + File.separator + "webhook-templates.xml");
		
		FileWatcher fw = new FileWatcher(configFile);
		WebHookTemplateFileChangeHandler changeListener = new WebHookTemplateFileChangeHandler(this, configFile, webHookPayloadManager);
		
		changeListener.handleConfigFileChange();
		
		fw.registerListener(changeListener);
		fw.start();
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
	
	public void registerTemplateFormatFromXmlConfig(WebHookTemplate payloadTemplate){
		synchronized (orderedTemplateCollection) {
			Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Registering XML template " 
					+ payloadTemplate.getTemplateShortName() 
					+ " with rank of " + payloadTemplate.getRank());
			xmlConfigTemplates.put(payloadTemplate.getTemplateShortName(),payloadTemplate);
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
		for (WebHookTemplate payloadTemplate : xmlConfigTemplates.values()){
			combinedTemplates.put(payloadTemplate.getTemplateShortName(), payloadTemplate);
		}
		
		this.orderedTemplateCollection.addAll(combinedTemplates.values());
		Collections.sort(this.orderedTemplateCollection, rankComparator);
	}

	public WebHookTemplate getTemplate(String formatShortname){
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
	
	
}