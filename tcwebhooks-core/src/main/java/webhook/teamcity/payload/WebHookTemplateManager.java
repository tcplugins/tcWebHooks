package webhook.teamcity.payload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.JAXBException;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import webhook.Constants;
import webhook.teamcity.Loggers;
import webhook.teamcity.ProbableJaxbJarConflictErrorException;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.payload.template.WebHookTemplateFromXml;
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
	private final ProjectIdResolver projectIdResolver;
	private String configFilePath;
	
	public WebHookTemplateManager(
			WebHookPayloadManager webHookPayloadManager, 
			WebHookTemplateJaxHelper webHookTemplateJaxHelper,
			ProjectIdResolver projectIdResolver)
	{
		this.webHookPayloadManager = webHookPayloadManager;
		this.webHookTemplateJaxHelper = webHookTemplateJaxHelper;
		this.projectIdResolver = projectIdResolver;
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
		// Set template as belonging to _Root if no associated project id found for this template.
		if (Objects.isNull(payloadTemplate.getAssociatedProjectId()) || payloadTemplate.getAssociatedProjectId().isEmpty()) {
			payloadTemplate.setAssociatedProjectId(projectIdResolver.getInternalProjectId(Constants.ROOT_PROJECT_ID));
		}
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

	public WebHookPayloadTemplate getTemplate(String templateId){
		synchronized (orderedTemplateCollection) {
			if (xmlConfigTemplates.containsKey(templateId)){
				return xmlConfigTemplates.get(templateId);
			}
			if (springTemplates.containsKey(templateId)){
				return springTemplates.get(templateId);
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
	
	public boolean isRegisteredTemplate(String template){
		return xmlConfigTemplates.containsKey(template) || springTemplates.containsKey(template);
	}
	
	public List<WebHookPayloadTemplate> getRegisteredTemplates(){
		return orderedTemplateCollection;
	}
	
	public List<WebHookPayloadTemplate> getRegisteredPermissionedTemplates(){
		List<WebHookPayloadTemplate> orderedTemplates = new ArrayList<>();
		for (WebHookPayloadTemplate template : orderedTemplateCollection) {
			try {
				projectIdResolver.getExternalProjectId(template.getProjectId()); // Throws AccessDeniedException if user is not permissioned on Project
				orderedTemplates.add(template);
			} catch (AccessDeniedException ex) {
				// Don't add the template if user is not permissioned for the project.
			}
		}
		return orderedTemplates;
	}
	
	public List<WebHookPayloadTemplate> getRegisteredPermissionedTemplatesForProject(SProject project){
		List<WebHookPayloadTemplate> orderedTemplates = new ArrayList<>();
		for (WebHookPayloadTemplate template : orderedTemplateCollection) {
			if (project.getProjectId().equals(template.getProjectId())) {
				try {
					projectIdResolver.getExternalProjectId(template.getProjectId()); // Throws AccessDeniedException if user is not permissioned on Project
					orderedTemplates.add(template);
				} catch (AccessDeniedException ex) {
					// Don't add the template if user is not permissioned for the project.
				}
			}
		}
		return orderedTemplates;
	}
	
	public Map<String,List<WebHookPayloadTemplate>> getRegisteredTemplatesForProjects(List<String> projectExternalIds) {
		Map<String, List<WebHookPayloadTemplate>> projectTemplates = new LinkedHashMap<>();
		getRegisteredTemplates().forEach(template -> {
			if (projectExternalIds.contains(template.getProjectId())) {
				projectTemplates.putIfAbsent(template.getProjectId(), new ArrayList<WebHookPayloadTemplate>());
				projectTemplates.get(template.getProjectId()).add(template);
			}
		});
		return projectTemplates;
	}
	
	public List<WebHookTemplateConfig> getRegisteredPermissionedTemplateConfigs(){
		List<WebHookTemplateConfig> orderedTemplateConfigs = new ArrayList<>();
		for (WebHookPayloadTemplate xmlConfig : getRegisteredPermissionedTemplates()){
			if (xmlConfig.getAsEntity()!=null){
				orderedTemplateConfigs.add(WebHookTemplateConfigBuilder.buildConfig(xmlConfig.getAsEntity()));
			}
		}
		return orderedTemplateConfigs;
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
	
	public enum TemplateState {
		PROVIDED 		("Template bundled with tcWebhooks"), 
		USER_DEFINED 	("User defined template"), 
		USER_OVERRIDDEN ("Overridden by user defined template"), 
		PROJECT_DEFINED ("Template associated with project"),
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
