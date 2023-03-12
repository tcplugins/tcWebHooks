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
import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookPayloadDefaultTemplates;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.settings.converter.PayloadToTemplateConverter;
import webhook.teamcity.settings.converter.WebHookBuildStateConverter;
import webhook.teamcity.settings.project.WebHookParameter;
import webhook.teamcity.settings.project.WebHookParameterModel;

@Builder @AllArgsConstructor
public class WebHookConfig {
	private static final String ATTR_KEY = "key";
	private static final String ATTR_URL = "url";
	private static final String ATTR_ID = "id";
	private static final String ATTR_TEMPLATE_ENGINE = "template-engine";
	private static final String ATTR_INCLUDED_IN_LEGACY_PAYLOADS = "included-in-legacy-payloads";
	private static final String ATTR_FORCE_RESOLVE_TEAMCITY_VARIABLE = "force-resolve-teamcity-variable";
	private static final String ATTR_SECURE = "secure";
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
	private static final String ATTR_HIDE_SECURE = "hide-secure-values";
	private static final String EL_STATES = "states";
	private static final String EL_BUILD_TYPES = "build-types";
	private static final String ATTR_ENABLED_FOR_ALL = "enabled-for-all";
	private static final String ATTR_ENABLED_FOR_SUBPROJECTS = "enabled-for-subprojects";
	private static final String LOG_PREFIX_WEB_HOOK_CONFIG = "WebHookConfig :: ";
	private ExtraParameters extraParameters;
	@Builder.Default private Boolean enabled = true;
	@Builder.Default private String uniqueKey = generateRandomKey();
	private String url;
	@Builder.Default private String payloadFormat = null;
	@Builder.Default private String payloadTemplate = "none";
	@Builder.Default private BuildState states = new BuildState();
	@Builder.Default private SortedMap<String, CustomMessageTemplate> templates = new TreeMap<>();
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
	@Builder.Default private boolean hideSecureValues = true;

	@SuppressWarnings("unchecked")
	public WebHookConfig (Element e) {

		this.uniqueKey = e.getAttributeValue(ATTR_ID, generateRandomKey());
		this.extraParameters = new ExtraParameters();
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
		this.hideSecureValues = true;

		if (e.getAttribute(ATTR_URL) != null){
			this.setUrl(e.getAttributeValue(ATTR_URL));
		}

		if (e.getAttribute(ATTR_ENABLED) != null){
			this.setEnabled(Boolean.parseBoolean(e.getAttributeValue(ATTR_ENABLED)));
		}

		if (e.getAttribute(ATTR_STATEMASK) != null){
			this.setBuildStates(WebHookBuildStateConverter.convert(Integer.parseInt(e.getAttributeValue(ATTR_STATEMASK))));
		}

		if (e.getAttribute(ATTR_KEY) != null){
			this.setUniqueKey(e.getAttributeValue(ATTR_KEY));
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
		
		if (e.getAttribute(ATTR_HIDE_SECURE) != null){
			this.setHideSecureValues(Boolean.parseBoolean(e.getAttributeValue(ATTR_HIDE_SECURE)));
		}

		// Transform payload and template to template.
		this.setPayloadTemplate(PayloadToTemplateConverter.transformPayloadToTemplate(this.getPayloadFormat(), this.getPayloadTemplate()));
		this.setPayloadFormat(null);

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
			if (Boolean.FALSE.equals(isEnabledForAllBuildsInProject())){
				List<Element> typesList = eTypes.getChildren("build-type");
				if ( ! typesList.isEmpty()){
					for(Element eType : typesList)
					{
						if (eType.getAttributeValue(ATTR_ID)!= null){
							enabledBuildTypesSet.add(eType.getAttributeValue(ATTR_ID));
						}
					}
				}
			}
		}

		if(e.getChild(EL_PARAMETERS) != null){
			Element eParams = e.getChild(EL_PARAMETERS);
			List<Element> paramsList = eParams.getChildren(ATTR_PARAM);
			if ( ! paramsList.isEmpty()){
				List<WebHookParameter> webHookParameters = new ArrayList<>();
				int counter = 0;
				for(Element eParam : paramsList) {
					counter++;
					WebHookParameterModel param = new WebHookParameterModel(
							String.valueOf(counter),
							ExtraParameters.WEBHOOK, 
							eParam.getAttributeValue(ATTR_NAME),
							eParam.getAttributeValue(ATTR_VALUE),
							Boolean.valueOf(eParam.getAttributeValue(ATTR_SECURE, Boolean.toString(false))),
							Boolean.valueOf(eParam.getAttributeValue(ATTR_INCLUDED_IN_LEGACY_PAYLOADS, Boolean.toString(true))),
							Boolean.valueOf(eParam.getAttributeValue(ATTR_FORCE_RESOLVE_TEAMCITY_VARIABLE, Boolean.toString(false))),
							eParam.getAttributeValue(ATTR_TEMPLATE_ENGINE, PayloadTemplateEngineType.STANDARD.toString())
						);
					
					webHookParameters.add(param);
				}
				this.extraParameters.putAll(ExtraParameters.WEBHOOK, webHookParameters);
			}
		}

		if(e.getChild(EL_CUSTOM_TEMPLATES) != null){
			Element eParams = e.getChild(EL_CUSTOM_TEMPLATES);
			List<Element> templateList = eParams.getChildren(EL_CUSTOM_TEMPLATE);
			if ( ! templateList.isEmpty()){
				for(Element eParam : templateList)
				{
					this.templates.put(
							eParam.getAttributeValue(CustomMessageTemplate.XML_ATTR_TYPE),
							CustomMessageTemplate.create(
									eParam.getAttributeValue(CustomMessageTemplate.XML_ATTR_TYPE),
									eParam.getAttributeValue(CustomMessageTemplate.XML_ATTR_TEMPLATE),
									Boolean.parseBoolean(eParam.getAttributeValue(CustomMessageTemplate.XML_ATTR_ENABLED))
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
									eParam.getAttributeValue(WebHookFilterConfig.XML_ATTR_VALUE),
									eParam.getAttributeValue(WebHookFilterConfig.XML_ATTR_REGEX),
									Boolean.parseBoolean(eParam.getAttributeValue(WebHookFilterConfig.XML_ATTR_ENABLED))
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
									eParam.getAttributeValue(WebHookHeaderConfig.XML_ATTR_NAME),
									eParam.getAttributeValue(WebHookHeaderConfig.XML_ATTR_VALUE)
									)
							);
				}
			}
		}

	}

	public static String generateRandomKey() {
		int min = 1000000;
		int max = 1000000000;
		Integer rand = min + new Random().nextInt((max - min) + 1);
		return "id_" + rand.toString();
	}

	/**
	 * WebHooksConfig constructor. Unchecked version. Use with caution!!
	 * This constructor does not check if the template is valid.
	 * It will still allow you to add the template, but the webhook might not
	 * fire at runtime if the template configured is not available.
	 *
	 * @param projectInternalId
	 * @param projectExternalId
	 * @param url
	 * @param enabled
	 * @param states
	 * @param payloadTemplate
	 * @param buildTypeAllEnabled
	 * @param buildTypeSubProjects
	 * @param enabledBuildTypes
	 * @param webHookAuthConfig
	 */
	public WebHookConfig (String projectInternalId, String projectExternalId, String url, Boolean enabled, BuildState states, String payloadTemplate, boolean buildTypeAllEnabled, boolean buildTypeSubProjects, Set<String> enabledBuildTypes, WebHookAuthConfig webHookAuthConfig, ExtraParameters extraParameters, List<WebHookFilterConfig> filters, List<WebHookHeaderConfig> headers, boolean hideSecureValues){
		this.uniqueKey =  generateRandomKey();
		this.setProjectInternalId(projectInternalId);
		this.setProjectExternalId(projectExternalId);
		this.extraParameters = new ExtraParameters();
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
		this.setPayloadTemplate(payloadTemplate);
		this.subProjectsEnabled = buildTypeSubProjects;
		this.allBuildTypesEnabled = buildTypeAllEnabled;
		if (Boolean.FALSE.equals(this.allBuildTypesEnabled)){
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
		if (extraParameters != null && !extraParameters.isEmpty()) {
			this.extraParameters.putAll(ExtraParameters.WEBHOOK, extraParameters.asMap());
		}
		if (filters != null && !filters.isEmpty()) {
			this.filters.addAll(filters);
		}
		if (headers != null && !headers.isEmpty()) {
			this.headers.addAll(headers);
		}
		this.hideSecureValues = hideSecureValues;
	}
	

	private Element getParameterAsElement(WebHookParameter i) {
		Element e = new Element(ATTR_PARAM);
		e.setAttribute(ATTR_NAME, i.getName());
		e.setAttribute(ATTR_VALUE, i.getValue());
		e.setAttribute(ATTR_SECURE, String.valueOf(i.getSecure()));
		e.setAttribute(ATTR_INCLUDED_IN_LEGACY_PAYLOADS, String.valueOf(i.getIncludedInLegacyPayloads()));
		e.setAttribute(ATTR_FORCE_RESOLVE_TEAMCITY_VARIABLE, String.valueOf(i.getForceResolveTeamCityVariable()));
		e.setAttribute(ATTR_TEMPLATE_ENGINE, i.getTemplateEngine());
		return e;
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
		el.setAttribute(ATTR_ID, this.getUniqueKey());
		el.setAttribute(ATTR_URL, this.getUrl());
		el.setAttribute(ATTR_ENABLED, String.valueOf(this.enabled));
		el.setAttribute(ATTR_TEMPLATE, String.valueOf(this.payloadTemplate));
		el.setAttribute(ATTR_HIDE_SECURE, String.valueOf(this.hideSecureValues));

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
			e.setAttribute(ATTR_ID, i);
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

		if (!this.extraParameters.getWebHookParameters().isEmpty()){
			Element paramsEl = new Element(EL_PARAMETERS);
			for (WebHookParameter i : this.extraParameters.getAll()){
				paramsEl.addContent(this.getParameterAsElement(i));
			}
			el.addContent(paramsEl);
		}

		// Don't put into the XML if empty.
		if (this.templates != null && this.templates.size() > 0){
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
	public ExtraParameters getParams() {
		return extraParameters;
	}

	public void setExtraParameters(ExtraParameters extraParameters) {
		this.extraParameters = extraParameters;
	}

	/**
	 * If allBuildTypes enabled, return true, otherwise  return whether the build is in the list of enabled buildTypes.
	 * @param sBuildType
	 * @return whether webhook is enabled for this specific {@link SBuildType} 
	 */
	public boolean isEnabledForBuildType(SBuildType sBuildType){
		return isEnabledForAllBuildsInProject() || enabledBuildTypesSet.contains(TeamCityIdResolver.getInternalBuildId(sBuildType));
	}

	public boolean isSpecificBuildTypeEnabled(SBuildType sBuildType){
		// Just check if this build type is only enabled for a specific build.
		return enabledBuildTypesSet.contains(TeamCityIdResolver.getInternalBuildId(sBuildType));
	}

	public Set<String> getEnabledBuildTypesSet() {
		return enabledBuildTypesSet;
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
		if (this.uniqueKey == null) {
			this.uniqueKey = generateRandomKey();
		}
		
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}

	public String getEnabledListAsString(){
		if (Boolean.FALSE.equals(this.enabled)){
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
			if (states.enabled(BuildStateEnum.TESTS_MUTED)){
			    enabledStates += ", Tests Muted";
			}
			if (states.enabled(BuildStateEnum.TESTS_UNMUTED)){
			    enabledStates += ", Tests Unmuted";
			}
			if (states.enabled(BuildStateEnum.SERVICE_MESSAGE_RECEIVED)){
				enabledStates += ", Service Message Received";
			}
			if (enabledStates.length() > 0){
				return enabledStates.substring(1);
			} else {
				return "None";
			}
		}
	}

	public String getWebHookEnabledAsChecked() {
		return Boolean.TRUE.equals(this.enabled) ? CHECKED : "";
	}

	public String getStateAllAsChecked() {
		return states.allEnabled() ? CHECKED : "";
		}

	private String getAsChecked(BuildStateEnum buildState) {
		return states.enabled(buildState) ? CHECKED : "";
	}

	public String getStateBuildStartedAsChecked() {
		return getAsChecked(BUILD_STARTED);
	}

	public String getStateChangesLoadedAsChecked() {
		return getAsChecked(CHANGES_LOADED);
	}

	public String getStateBuildFinishedAsChecked() {
		return getAsChecked(BUILD_FINISHED);
	}

	public String getStateBeforeFinishedAsChecked() {
		return getAsChecked(BEFORE_BUILD_FINISHED);
	}

	public String getStateResponsibilityChangedAsChecked() {
		return getAsChecked(RESPONSIBILITY_CHANGED);
	}

	public String getStateBuildInterruptedAsChecked() {
		return getAsChecked(BUILD_INTERRUPTED);
	}

	public String getStateBuildSuccessfulAsChecked() {
		return getAsChecked(BUILD_SUCCESSFUL);
	}

	public String getStateBuildFixedAsChecked() {
		return getAsChecked(BUILD_FIXED);
	}

	public String getStateBuildFailedAsChecked() {
		return getAsChecked(BUILD_FAILED);
	}

	public String getStateBuildBrokenAsChecked() {
		return getAsChecked(BUILD_BROKEN);
	}

	private String getPayloadFormat() {
		return payloadFormat;
	}

	/** Get the PayloadTemplateId as a string.
	 *
	 * @return templateId
	 */
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
	
	public Map<String,String> getEnabledTemplatesExcludingDefaults() {
		Map<String,String> mT = new TreeMap<>();
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
		this.authParameters = new LinkedHashMap<>();
		this.authParameters.putAll(authParameters);
	}

	public void clearAuthParameters() {
		if (this.authParameters != null) {
			this.authParameters.clear();
		}
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public void setAuthPreemptive(Boolean authPreemptive) {
		this.authPreemptive = authPreemptive;
	}

	public WebHookAuthConfig getAuthenticationConfig() {
		if (Boolean.TRUE.equals(authEnabled) && !authType.equals("")){
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
	public void setTriggerFilters(List<WebHookFilterConfig> filters) {
		this.filters = filters;
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

	public boolean isHideSecureValues() {
		return hideSecureValues;
	}

	public void setHideSecureValues(boolean hideSecureValues) {
		this.hideSecureValues = hideSecureValues;
	}
}
