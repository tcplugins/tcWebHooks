package webhook.teamcity.payload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import webhook.teamcity.Loggers;

import jetbrains.buildServer.serverSide.SBuildServer;

public class WebHookTemplateManager {
	
	HashMap<String, WebHookTemplate> springTemplates = new HashMap<String,WebHookTemplate>();
	HashMap<String, WebHookTemplate> xmlConfigTemplates = new HashMap<String,WebHookTemplate>();
	Comparator<WebHookTemplate> rankComparator = new WebHookTemplateRankingComparator();
	List<WebHookTemplate> orderedTemplateCollection = new ArrayList<WebHookTemplate>();
	SBuildServer server;
	
	public WebHookTemplateManager(SBuildServer server){
		this.server = server;
		Loggers.SERVER.info("WebHookTemplateManager :: Starting");
	}
	
	public void registerTemplateFormatFromSpring(WebHookTemplate payloadTemplate){
		synchronized (orderedTemplateCollection) {
			Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Registering template " 
					+ payloadTemplate.getTemplateShortName() 
					+ " with rank of " + payloadTemplate.getRank());
			springTemplates.put(payloadTemplate.getTemplateShortName(),payloadTemplate);
			this.orderedTemplateCollection.add(payloadTemplate);
			
			Collections.sort(this.orderedTemplateCollection, rankComparator);
			Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: Templates list is " + this.orderedTemplateCollection.size() + " items long. Templates are ranked in the following order..");
			for (WebHookTemplate pl : this.orderedTemplateCollection){
				Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: Template Name: " + pl.getTemplateShortName() + " Rank: " + pl.getRank());
			}
		}
	}
	
	public void registerTemplateFormatFromXmlConfig(WebHookTemplate payloadTemplate){
		synchronized (orderedTemplateCollection) {
			Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Registering template " 
					+ payloadTemplate.getTemplateShortName() 
					+ " with rank of " + payloadTemplate.getRank());
			xmlConfigTemplates.put(payloadTemplate.getTemplateShortName(),payloadTemplate);
			this.orderedTemplateCollection.add(payloadTemplate);
			
			Collections.sort(this.orderedTemplateCollection, rankComparator);
			Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: Templates list is " + this.orderedTemplateCollection.size() + " items long. Templates are ranked in the following order..");
			for (WebHookTemplate pl : this.orderedTemplateCollection){
				Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: Template Name: " + pl.getTemplateShortName() + " Rank: " + pl.getRank());
			}
		}
	}
	
	public void unregisterAllXmlConfigTemplates(){
		synchronized (orderedTemplateCollection) {
			Loggers.SERVER.info(this.getClass().getSimpleName() + " :: un-registering all XML config templates.");
			xmlConfigTemplates.clear();
			// Rebuild the list of configured templates from just the Spring registered ones.
			for (WebHookTemplate payloadTemplate : springTemplates.values()){
				this.orderedTemplateCollection.add(payloadTemplate);
			}
			
			Collections.sort(this.orderedTemplateCollection, rankComparator);
			Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: Templates list is " + this.orderedTemplateCollection.size() + " items long. Templates are ranked in the following order..");
			for (WebHookTemplate pl : this.orderedTemplateCollection){
				Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: Template Name: " + pl.getTemplateShortName() + " Rank: " + pl.getRank());
			}
		}
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
	
	public Set<String> getRegisteredTemplates(){
		return springTemplates.keySet();
	}
	
	public Collection<WebHookTemplate> getRegisteredTemplatesAsCollection(){
		return orderedTemplateCollection;
	}

	public SBuildServer getServer() {
		return server;
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