package webhook.teamcity.settings;

import static webhook.teamcity.BuildStateEnum.BEFORE_BUILD_FINISHED;
import static webhook.teamcity.BuildStateEnum.BUILD_BROKEN;
import static webhook.teamcity.BuildStateEnum.BUILD_FAILED;
import static webhook.teamcity.BuildStateEnum.BUILD_FINISHED;
import static webhook.teamcity.BuildStateEnum.BUILD_FIXED;
import static webhook.teamcity.BuildStateEnum.BUILD_INTERRUPTED;
import static webhook.teamcity.BuildStateEnum.CHANGES_LOADED;
import static webhook.teamcity.BuildStateEnum.BUILD_STARTED;
import static webhook.teamcity.BuildStateEnum.BUILD_SUCCESSFUL;
import static webhook.teamcity.BuildStateEnum.RESPONSIBILITY_CHANGED;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildType;
import lombok.AllArgsConstructor;
import lombok.Builder;

import org.jdom.DataConversionException;
import org.jdom.Element;

import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.auth.WebHookAuthConfig;
import webhook.teamcity.payload.WebHookPayloadDefaultTemplates;
import webhook.teamcity.settings.converter.WebHookBuildStateConverter;

@Builder @AllArgsConstructor
public class WebHookConfig {
	private static final String EL_TRIGGER_FILTERS = "trigger-filters";
	private static final String EL_HEADERS = "headers";
	private static final String ATTR_PREEMPTIVE = "preemptive";
	private static final String CHECKED = "checked ";
	private static final String EL_CUSTOM_TEMPLATE = "custom-template";
	private static final String EL_CUSTOM_TEMPLATES = "custom-templates";
	private static final String ATTR_VALUE = "value";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_PARAM = "param";
	private static final String EL_PARAMETERS = "parameters";
	private static final String ATTR_STATEMASK = "statemask";
	private static final String ATTR_TYPE = "type";
	private static final String EL_STATE = "state";
	private static final String ATTR_TEMPLATE = "template";
	private static final String ATTR_FORMAT = "format";
	private static final String ATTR_ENABLED = "enabled";
	private static final String EL_STATES = "states";
	private static final String EL_BUILD_TYPES = "build-types";
	private static final String ATTR_ENABLED_FOR_ALL = "enabled-for-all";
	private static final String ATTR_ENABLED_FOR_SUBPROJECTS = "enabled-for-subprojects";
	private static final String LOG_PREFIX_WEB_HOOK_CONFIG = "WebHookConfig :: ";
	private SortedMap<String,String> extraParameters;
	@Builder.Default private Boolean enabled = true;
	@Builder.Default private String uniqueKey = "";
	private String url;
	@Builder.Default private String payloadFormat = null;
	@Builder.Default private String payloadTemplate = "none";
	@Builder.Default private BuildState states = new BuildState();
	private SortedMap<String, CustomMessageTemplate> templates; // This defaults to null so that it's not in the XML if empty.
	@Builder.Default private Boolean allBuildTypesEnabled = true;
	@Builder.Default private Boolean subProjectsEnabled = true;
	@Builder.Default private Set<String> enabledBuildTypesSet = new HashSet<>();
	@Builder.Default private String authType = "";
	@Builder.Default private Boolean authEnabled = false;
	@Builder.Default private Map<String,String> authParameters = new LinkedHashMap<>();
	@Builder.Default private Boolean authPreemptive = true;
	private List<WebHookFilterConfig> filters;
	private List<WebHookHeaderConfig> headers;
	@Builder.Default private String projectInternalId = null;
	@Builder.Default private String projectExternalId = null;
	
	@SuppressWarnings("unchecked")
	public WebHookConfig (Element e) {
		
		this.uniqueKey = "id_" + getRandomKey();
		this.extraParameters = new TreeMap<>();
		this.states = new BuildState();
		this.templates = new TreeMap<>();
		this.allBuildTypesEnabled = true;
		this.subProjectsEnabled = true;
		this.enabledBuildTypesSet = new HashSet<>();
		this.authType = "";
		this.authEnabled = false;
		this.authParameters = new LinkedHashMap<>();
		this.authPreemptive = true;
		this.filters = new ArrayList<>();
		this.headers = new ArrayList<>();
		
		if (e.getAttribute("url") != null){
			this.setUrl(e.getAttributeValue("url"));
		}

		if (e.getAttribute(ATTR_ENABLED) != null){
			this.setEnabled(Boolean.parseBoolean(e.getAttributeValue(ATTR_ENABLED)));
		}

		if (e.getAttribute(ATTR_STATEMASK) != null){
			this.setBuildStates(WebHookBuildStateConverter.convert(Integer.parseInt(e.getAttributeValue(ATTR_STATEMASK))));
		}

		if (e.getAttribute("key") != null){
			this.setUniqueKey(e.getAttributeValue("key"));
		}

		if (e.getAttribute(ATTR_FORMAT) != null){
			this.setPayloadFormat(e.getAttributeValue(ATTR_FORMAT));
		} else {
			// Set to nvpairs by default for backward compatibility.
			this.setPayloadFormat("nvpairs");
		}
		
		if (e.getAttribute(ATTR_TEMPLATE) != null){
			this.setPayloadTemplate(e.getAttributeValue(ATTR_TEMPLATE));
		}
		
		if(e.getChild(EL_STATES) != null){
			Element eStates = e.getChild(EL_STATES);
			List<Element> statesList = eStates.getChildren(EL_STATE);
			if ( ! statesList.isEmpty()){
				for(Element eState : statesList)
				{
					try {
						states.setEnabled(BuildStateEnum.findBuildState(eState.getAttributeValue(ATTR_TYPE)), 
										  eState.getAttribute(ATTR_ENABLED).getBooleanValue());
					} catch (DataConversionException e1) {
						Loggers.SERVER.warn(LOG_PREFIX_WEB_HOOK_CONFIG + e1.getMessage());
					}
				}
			}
		}
		
		if(e.getChild(EL_BUILD_TYPES) != null){
			Element eTypes = e.getChild(EL_BUILD_TYPES);
			if (eTypes.getAttribute(ATTR_ENABLED_FOR_ALL) != null){
				try {
					this.enableForAllBuildsInProject(eTypes.getAttribute(ATTR_ENABLED_FOR_ALL).getBooleanValue());
				} catch (DataConversionException e1) {
					Loggers.SERVER.warn(LOG_PREFIX_WEB_HOOK_CONFIG + e1.getMessage());
				}
			}
			if (eTypes.getAttribute(ATTR_ENABLED_FOR_SUBPROJECTS) != null){
				try {
					this.enableForSubProjects(eTypes.getAttribute(ATTR_ENABLED_FOR_SUBPROJECTS).getBooleanValue());
				} catch (DataConversionException e1) {
					Loggers.SERVER.warn(LOG_PREFIX_WEB_HOOK_CONFIG + e1.getMessage());
				}
			}
			if (!isEnabledForAllBuildsInProject()){
				List<Element> typesList = eTypes.getChildren("build-type");
				if ( ! typesList.isEmpty()){
					for(Element eType : typesList)
					{
						if (eType.getAttributeValue("id")!= null){
							enabledBuildTypesSet.add(eType.getAttributeValue("id"));
						}
					}
				}
			}
		}
		
		if(e.getChild(EL_PARAMETERS) != null){
			Element eParams = e.getChild(EL_PARAMETERS);
			List<Element> paramsList = eParams.getChildren(ATTR_PARAM);
			if ( ! paramsList.isEmpty()){
				for(Element eParam : paramsList)
				{
					this.extraParameters.put(
							eParam.getAttributeValue(ATTR_NAME), 
							eParam.getAttributeValue(ATTR_VALUE)
							);
				}
			}
		}
		
		if(e.getChild(EL_CUSTOM_TEMPLATES) != null){
			Element eParams = e.getChild(EL_CUSTOM_TEMPLATES);
			List<Element> templateList = eParams.getChildren(EL_CUSTOM_TEMPLATE);
			if ( ! templateList.isEmpty()){
				for(Element eParam : templateList)
				{
					this.templates.put(
							eParam.getAttributeValue(CustomMessageTemplate.TYPE),
							CustomMessageTemplate.create(
									eParam.getAttributeValue(CustomMessageTemplate.TYPE),
									eParam.getAttributeValue(CustomMessageTemplate.TEMPLATE),
									Boolean.parseBoolean(eParam.getAttributeValue(CustomMessageTemplate.ENABLED))
									)
							);
				}
			}
		}
		
		if(e.getChild("auth") != null){
			Element eAuth = e.getChild("auth");
			if (eAuth.getAttribute(ATTR_TYPE) != null){
				// We have an "auth" element
				// Try to get the enabled flag
				authType = eAuth.getAttribute(ATTR_TYPE).getValue();
				try {
					authEnabled = eAuth.getAttribute(ATTR_ENABLED).getBooleanValue();
				} catch (DataConversionException e1){
					// And if it can't be read as boolean default it 
					// to true anyway (since we have the auth type).
					authEnabled = true;
				}
				try {
					if (eAuth.getAttribute(ATTR_PREEMPTIVE) != null){
						authPreemptive = eAuth.getAttribute(ATTR_PREEMPTIVE).getBooleanValue();
					}
				} catch (DataConversionException e1){
					// And if it can't be read as boolean default it 
					// to true (which means creds are always sent).
					authPreemptive = true;
				}
				Element eParams = eAuth.getChild("auth-parameters");
				if (eParams != null){
					List<Element> paramsList = eParams.getChildren(ATTR_PARAM);
					if (!paramsList.isEmpty()){
						for(Element eParam : paramsList)
						{
							this.authParameters.put(
									eParam.getAttributeValue(ATTR_NAME), 
									eParam.getAttributeValue(ATTR_VALUE)
									);
						}
					}
				}
			}

		}
		
		/*
		    <trigger-filters>
	  			<filter value="${branchDisplayName}" regex="^master$" />
	  		</trigger-filters>
		 */
		if(e.getChild(EL_TRIGGER_FILTERS) != null){
			Element eParams = e.getChild(EL_TRIGGER_FILTERS);
			List<Element> filterList = eParams.getChildren("filter");
			if (! filterList.isEmpty()){
				for(Element eParam : filterList)
				{
					this.filters.add(
							
							WebHookFilterConfig.create(
									eParam.getAttributeValue(WebHookFilterConfig.VALUE),
									eParam.getAttributeValue(WebHookFilterConfig.REGEX),
									Boolean.parseBoolean(eParam.getAttributeValue(WebHookFilterConfig.ENABLED))
									)
							);
				}
			}
		}
		
		/*
		    <headers>
	  			<header name="${someThing}" value="${branchDisplayName}" />
	  		</headers>
		 */
		if(e.getChild(EL_HEADERS) != null){
			Element eParams = e.getChild(EL_HEADERS);
			List<Element> headerList = eParams.getChildren(WebHookHeaderConfig.XML_ELEMENT_NAME);
			if (! headerList.isEmpty()){
				for(Element eParam : headerList)
				{
					this.headers.add(
							
							WebHookHeaderConfig.create(
									eParam.getAttributeValue(WebHookHeaderConfig.NAME),
									eParam.getAttributeValue(WebHookHeaderConfig.VALUE)
									)
							);
				}
			}
		}
		
	}
	
	private String getRandomKey() {
		int min = 1000000;
		int max = 1000000000;
		Integer rand = min + new Random().nextInt((max - min) + 1);
		return rand.toString();
	}

	/**
	 * WebHooksConfig constructor. Unchecked version. Use with caution!!
	 * This constructor does not check if the payloadFormat is valid.
	 * It will still allow you to add the format, but the webhook might not
	 * fire at runtime if the payloadFormat configured is not available.
	 *  
	 * @param url
	 * @param enabled
	 * @param stateMask
	 * @param payloadFormat (unvalidated)
	 * @param webHookAuthConfig 
	 */
	public WebHookConfig (String projectInternalId, String projectExternalId, String url, Boolean enabled, BuildState states, String payloadFormat, String payloadTemplate, boolean buildTypeAllEnabled, boolean buildTypeSubProjects, Set<String> enabledBuildTypes, WebHookAuthConfig webHookAuthConfig){
		this.uniqueKey = "id_" + getRandomKey();
		this.setProjectInternalId(projectInternalId);
		this.setProjectExternalId(projectExternalId);
		this.extraParameters = new TreeMap<>();
		this.templates = new TreeMap<>();
		this.authType = "";
		this.authEnabled = false;
		this.authParameters = new LinkedHashMap<>();
		this.authPreemptive = true;
		this.filters = new ArrayList<>();
		this.headers = new ArrayList<>();
		this.setUrl(url);
		this.setEnabled(enabled);
		this.setBuildStates(states);
		this.setPayloadFormat(payloadFormat);
		this.setPayloadTemplate(payloadTemplate);
		this.subProjectsEnabled = buildTypeSubProjects;
		this.allBuildTypesEnabled = buildTypeAllEnabled;
		if (!this.allBuildTypesEnabled){
			this.enabledBuildTypesSet = enabledBuildTypes;
		} else {
			this.enabledBuildTypesSet = new HashSet<>();
		}
		if (webHookAuthConfig != null){
			this.authType = webHookAuthConfig.getType();
			this.authPreemptive = webHookAuthConfig.getPreemptive();
			this.authEnabled = true;
			this.authParameters.putAll(webHookAuthConfig.getParameters());
		}
	}

	private Element getKeyAndValueAsElement(Map<String,String> map, String key, String elementName){
		Element e = new Element(elementName);
		if (map.containsKey(key)){
			e.setAttribute(ATTR_NAME, key);
			e.setAttribute(ATTR_VALUE,map.get(key));
		}
		return e;
	}
	
	public Element getAsElement(){
		Element el = new Element("webhook");
		el.setAttribute("url", this.getUrl());
		el.setAttribute(ATTR_ENABLED, String.valueOf(this.enabled));
		el.setAttribute(ATTR_FORMAT, String.valueOf(this.payloadFormat).toLowerCase());
		el.setAttribute(ATTR_TEMPLATE, String.valueOf(this.payloadTemplate));
		
		Element statesEl = new Element(EL_STATES);
		for (BuildStateEnum state : states.getStateSet()){
			Element e = new Element(EL_STATE);
			e.setAttribute(ATTR_TYPE, state.getShortName());
			e.setAttribute(ATTR_ENABLED, Boolean.toString(states.enabled(state)));
			statesEl.addContent(e);
		}
		el.addContent(statesEl);
		
		Element buildsEl = new Element(EL_BUILD_TYPES);
		buildsEl.setAttribute(ATTR_ENABLED_FOR_ALL, Boolean.toString(isEnabledForAllBuildsInProject()));
		buildsEl.setAttribute(ATTR_ENABLED_FOR_SUBPROJECTS, Boolean.toString(isEnabledForSubProjects()));
		
		for (String i : enabledBuildTypesSet){
			Element e = new Element("build-type");
			e.setAttribute("id", i);
			buildsEl.addContent(e);
		}
		el.addContent(buildsEl);
		
		if (this.filters != null &&  ! this.filters.isEmpty()){
			Element filtersEl = new Element(EL_TRIGGER_FILTERS);
			for (WebHookFilterConfig f : this.filters){
				filtersEl.addContent(f.getAsElement());
			}
			el.addContent(filtersEl);
		}		
		
		if (this.extraParameters.size() > 0){
			Element paramsEl = new Element(EL_PARAMETERS);
			for (String i : this.extraParameters.keySet()){
				paramsEl.addContent(this.getKeyAndValueAsElement(this.extraParameters, i, ATTR_PARAM));
			}
			el.addContent(paramsEl);
		}
		
		if (this.templates.size() > 0){
			Element templatesEl = new Element(EL_CUSTOM_TEMPLATES);
			for (CustomMessageTemplate t : this.templates.values()){
				templatesEl.addContent(t.getAsElement());
			}
			el.addContent(templatesEl);
		}
		
		if (this.authType != null && ! this.authType.isEmpty()){
			Element authEl = new Element("auth");
			authEl.setAttribute(ATTR_ENABLED, this.authEnabled.toString());
			authEl.setAttribute(ATTR_TYPE, this.authType);
			authEl.setAttribute(ATTR_PREEMPTIVE, this.authPreemptive.toString() );
			if (this.authParameters != null && ! this.authParameters.isEmpty()){
				Element paramsEl = new Element("auth-parameters");
				for (String i : this.authParameters.keySet()){
					paramsEl.addContent(this.getKeyAndValueAsElement(this.authParameters, i, ATTR_PARAM));
				}
				authEl.addContent(paramsEl);
			}
			el.addContent(authEl);
		}
		
		if (this.headers != null &&  ! this.headers.isEmpty()){
			Element headersEl = new Element(EL_HEADERS);
			for (WebHookHeaderConfig h : this.headers){
				headersEl.addContent(h.getAsElement());
			}
			el.addContent(headersEl);
		}	
		
		return el;
	}
	
	// Getters and Setters..

	public SortedMap<String,String> getParams() {
		return extraParameters;
	}
	
	public boolean isEnabledForBuildType(SBuildType sBuildType){
		// If allBuildTypes enabled, return true, otherwise  return whether the build is in the list of enabled buildTypes. 
		return isEnabledForAllBuildsInProject() ? true : enabledBuildTypesSet.contains(TeamCityIdResolver.getInternalBuildId(sBuildType));
	}
	
	public boolean isSpecificBuildTypeEnabled(SBuildType sBuildType){
		// Just check if this build type is only enabled for a specific build. 
		return enabledBuildTypesSet.contains(TeamCityIdResolver.getInternalBuildId(sBuildType));
	}
	
	public String getBuildTypeCountAsFriendlyString(){
		if (this.allBuildTypesEnabled  && this.subProjectsEnabled){
			return "All builds & Sub-Projects";
		} else if (this.allBuildTypesEnabled){ // this.subProjectsEnabled is false
			return "All builds";
		} else {
			String subProjectsString = "";
			if (this.subProjectsEnabled){
				subProjectsString = " & All Sub-Project builds";
			}
			int enabledBuildTypeCount = this.enabledBuildTypesSet.size();
			if (enabledBuildTypeCount == 1){
				return enabledBuildTypeCount + " build" + subProjectsString;
			}
			return enabledBuildTypeCount + " builds" + subProjectsString; 
		}
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public BuildState getBuildStates() {
		return states;
	}

	public void setBuildStates(BuildState states) {
		this.states = states;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}
	
	public String getEnabledListAsString(){
		if (!this.enabled){
			return "Disabled";
		} else if (states.allEnabled()){
			return "All Build Events";
		} else if (states.noneEnabled()) {
			return "None";
		} else {
			String enabledStates = "";
			if (states.enabled(BuildStateEnum.BUILD_ADDED_TO_QUEUE)){
				enabledStates += ", Build Added to Queue";
			}
			if (states.enabled(BuildStateEnum.BUILD_REMOVED_FROM_QUEUE)){
				enabledStates += ", Build Removed from Queue by User";
			}
			if (states.enabled(BuildStateEnum.BUILD_STARTED)){
				enabledStates += ", Build Started";
			}
			if (states.enabled(BuildStateEnum.CHANGES_LOADED)){
				enabledStates += ", Changes Loaded";
			}
			if (states.enabled(BuildStateEnum.BUILD_INTERRUPTED)){
				enabledStates += ", Build Interrupted";
			}
			if (states.enabled(BuildStateEnum.BEFORE_BUILD_FINISHED)){
				enabledStates += ", Build Almost Completed";
			}
			if (states.enabled(BuildStateEnum.RESPONSIBILITY_CHANGED)){
				enabledStates += ", Build Responsibility Changed";
			}
			if (states.enabled(BuildStateEnum.BUILD_FAILED)){
				if (states.enabled(BuildStateEnum.BUILD_BROKEN)){
					enabledStates += ", Build Broken";
				} else {
					enabledStates += ", Build Failed";
				}
			}
			if (states.enabled(BuildStateEnum.BUILD_SUCCESSFUL)){
				if (states.enabled(BuildStateEnum.BUILD_FIXED)){
					enabledStates += ", Build Fixed";
				} else {
					enabledStates += ", Build Successful";
				}
			}
			if (states.enabled(BuildStateEnum.BUILD_PINNED)){
				enabledStates += ", Build Pinned";
			}
			if (states.enabled(BuildStateEnum.BUILD_UNPINNED)){
				enabledStates += ", Build Unpinned";
			}
			if (enabledStates.length() > 0){
				return enabledStates.substring(1);
			} else {
				return "None";
			}
		}
	}
	
	public String getWebHookEnabledAsChecked() {
		if (this.enabled){
			return CHECKED;
		}
		return ""; 
	}
	
	public String getStateAllAsChecked() {
		if (states.allEnabled()){
			return CHECKED;
		}		
		return ""; 
	}
	
	public String getStateBuildStartedAsChecked() {
		if (states.enabled(BUILD_STARTED)){
			return CHECKED;
		}
		return ""; 
	}
	
	public String getStateChangesLoadedAsChecked() {
		if (states.enabled(CHANGES_LOADED)){
			return CHECKED;
		}
		return ""; 
	}
	
	public String getStateBuildFinishedAsChecked() {
		if (states.enabled(BUILD_FINISHED)){
			return CHECKED;
		}
		return ""; 
	}

	public String getStateBeforeFinishedAsChecked() {
		if (states.enabled(BEFORE_BUILD_FINISHED)){
			return CHECKED;
		}
		return ""; 
	}

	public String getStateResponsibilityChangedAsChecked() {
		if (states.enabled(RESPONSIBILITY_CHANGED)){
			return CHECKED;
		}
		return ""; 
	}

	public String getStateBuildInterruptedAsChecked() {
		if (states.enabled(BUILD_INTERRUPTED)){
			return CHECKED;
		}
		return ""; 
	}
	
	public String getStateBuildSuccessfulAsChecked() {
		if (states.enabled(BUILD_SUCCESSFUL)){
			return CHECKED;
		}
		return ""; 
	}
	
	public String getStateBuildFixedAsChecked() {
		if (states.enabled(BUILD_FIXED)){
			return CHECKED;
		}
		return ""; 
	}
	
	public String getStateBuildFailedAsChecked() {
		if (states.enabled(BUILD_FAILED)){
			return CHECKED;
		}
		return ""; 
	}

	public String getStateBuildBrokenAsChecked() {
		if (states.enabled(BUILD_BROKEN)){
			return CHECKED;
		}
		return ""; 
	}
	
	public String getPayloadFormat() {
		return payloadFormat;
	}
	
	public String getPayloadTemplate() {
		return payloadTemplate;
	}

	/**
	 * Sets the payload format to whatever string is passed.
	 * It does NOT check that the payload format has a valid implementation loaded.
	 * 
	 * @param payloadFormat
	 */
	public void setPayloadFormat(String payloadFormat) {
		this.payloadFormat = payloadFormat;
	}
	
	
	public void setPayloadTemplate(String payloadTemplate) {
		this.payloadTemplate = payloadTemplate;
	}

	public Boolean isEnabledForAllBuildsInProject() {
		return allBuildTypesEnabled;
	}

	public void enableForAllBuildsInProject(Boolean allBuildTypesEnabled) {
		this.allBuildTypesEnabled = allBuildTypesEnabled;
	}
	
	public Boolean isEnabledForSubProjects() {
		return subProjectsEnabled;
	}
	
	public void enableForSubProjects(Boolean subProjectsEnabled) {
		this.subProjectsEnabled = subProjectsEnabled;
	}
	
	public void clearAllEnabledBuildsInProject(){
		this.enabledBuildTypesSet.clear();
	}
	
	public void enableBuildInProject(String buildTypeId) {
		this.enabledBuildTypesSet.add(buildTypeId);
	}

	public Map<String,String> getEnabledTemplates() {
		Map<String,String> mT = WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates();
		for (CustomMessageTemplate t : templates.values()){
			if (t.enabled){
				mT.put(t.templateType, t.templateText);
			}
		}
		return mT;
	}
	
	public Boolean getAuthEnabled() {
		return authEnabled;
	}
	
	public void setAuthEnabled(Boolean authEnabled) {
		this.authEnabled = authEnabled;
	}
	
	public void setAuthParameters(Map<String, String> authParameters) {
		this.authParameters.putAll(authParameters);
	}
	
	public void clearAuthParameters() {
		this.authParameters.clear();
	}
	
	public void setAuthType(String authType) {
		this.authType = authType;
	}
	
	public void setAuthPreemptive(Boolean authPreemptive) {
		this.authPreemptive = authPreemptive;
	}
	
	public WebHookAuthConfig getAuthenticationConfig() {
		if (authEnabled && !authType.equals("")){
			WebHookAuthConfig webhookAuthConfig= new WebHookAuthConfig();
			webhookAuthConfig.setType(authType);
			webhookAuthConfig.setPreemptive(authPreemptive);
			webhookAuthConfig.getParameters().putAll(authParameters);
			return webhookAuthConfig;
		}
		return null;
	}	
	
	public List<WebHookFilterConfig> getTriggerFilters() {
		return this.filters;
	}
	
	public List<WebHookHeaderConfig> getHeaders() {
		return headers;
	}
	
	public void setHeaders(List<WebHookHeaderConfig> headers) {
		this.headers = headers;
	}

	public WebHookConfig copy() {
		WebHookConfig configCopy = new WebHookConfig(this.getAsElement()) ;
		configCopy.setUniqueKey(this.getUniqueKey());
		configCopy.setProjectInternalId(this.getProjectInternalId());
		configCopy.setProjectExternalId(this.getProjectExternalId());
		return configCopy;
	}

	public String getProjectInternalId() {
		return projectInternalId;
	}
	
	public void setProjectInternalId(String projectInternalId) {
		this.projectInternalId = projectInternalId;
	}

	public String getProjectExternalId() {
		return projectExternalId;
	}

	public void setProjectExternalId(String projectExternalId) {
		this.projectExternalId = projectExternalId;
	}
}
