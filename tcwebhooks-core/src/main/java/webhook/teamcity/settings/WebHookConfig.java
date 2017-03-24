package webhook.teamcity.settings;

import static webhook.teamcity.BuildStateEnum.BEFORE_BUILD_FINISHED;
import static webhook.teamcity.BuildStateEnum.BUILD_BROKEN;
import static webhook.teamcity.BuildStateEnum.BUILD_FAILED;
import static webhook.teamcity.BuildStateEnum.BUILD_FINISHED;
import static webhook.teamcity.BuildStateEnum.BUILD_FIXED;
import static webhook.teamcity.BuildStateEnum.BUILD_INTERRUPTED;
import static webhook.teamcity.BuildStateEnum.BUILD_STARTED;
import static webhook.teamcity.BuildStateEnum.BUILD_SUCCESSFUL;
import static webhook.teamcity.BuildStateEnum.RESPONSIBILITY_CHANGED;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import jetbrains.buildServer.serverSide.SBuildType;

import org.jdom.DataConversionException;
import org.jdom.Element;

import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.auth.WebHookAuthConfig;
import webhook.teamcity.payload.WebHookPayloadDefaultTemplates;
import webhook.teamcity.settings.converter.WebHookBuildStateConverter;


public class WebHookConfig {
	private SortedMap<String,String> extraParameters;
	private Boolean enabled = true;
	private String uniqueKey = "";
	private String url;
	private String payloadFormat = null;
	private BuildState states = new BuildState();
	private SortedMap<String, CustomMessageTemplate> templates; 
	private Boolean allBuildTypesEnabled = true;
	private Boolean subProjectsEnabled = true;
	private Set<String> enabledBuildTypesSet = new HashSet<String>();
	private String authType = "";
	private Boolean authEnabled = false;
	private SortedMap<String,String> authParameters;
	private Boolean authPreemptive = true;
	
	@SuppressWarnings("unchecked")
	public WebHookConfig (Element e) {
		
		int Min = 1000000, Max = 1000000000;
		Integer Rand = Min + (int)(Math.random() * ((Max - Min) + 1));
		this.uniqueKey = Rand.toString();
		this.extraParameters = new TreeMap<String,String>();
		this.authParameters = new TreeMap<String,String>();
		this.templates = new TreeMap<String,CustomMessageTemplate>();
		
		if (e.getAttribute("url") != null){
			this.setUrl(e.getAttributeValue("url"));
		}
		
		if (e.getAttribute("enabled") != null){
			this.setEnabled(Boolean.parseBoolean(e.getAttributeValue("enabled")));
		}

		if (e.getAttribute("statemask") != null){
			this.setBuildStates(WebHookBuildStateConverter.convert(Integer.parseInt(e.getAttributeValue("statemask"))));
		}

		if (e.getAttribute("key") != null){
			this.setUniqueKey(e.getAttributeValue("key"));
		}

		if (e.getAttribute("format") != null){
			this.setPayloadFormat(e.getAttributeValue("format"));
		} else {
			// Set to nvpairs by default for backward compatibility.
			this.setPayloadFormat("nvpairs");
		}
		
		if(e.getChild("states") != null){
			Element eStates = e.getChild("states");
			List<Element> statesList = eStates.getChildren("state");
			if (statesList.size() > 0){
				for(Element eState : statesList)
				{
					try {
						states.setEnabled(BuildStateEnum.findBuildState(eState.getAttributeValue("type")), 
										  eState.getAttribute("enabled").getBooleanValue());
					} catch (DataConversionException e1) {e1.printStackTrace();}
				}
			}
		}
		
		if(e.getChild("build-types") != null){
			Element eTypes = e.getChild("build-types");
			if (eTypes.getAttribute("enabled-for-all") != null){
				try {
					this.enableForAllBuildsInProject(eTypes.getAttribute("enabled-for-all").getBooleanValue());
				} catch (DataConversionException e1) {e1.printStackTrace();}
			}
			if (eTypes.getAttribute("enabled-for-subprojects") != null){
				try {
					this.enableForSubProjects(eTypes.getAttribute("enabled-for-subprojects").getBooleanValue());
				} catch (DataConversionException e1) {e1.printStackTrace();}
			}
			if (!isEnabledForAllBuildsInProject()){
				List<Element> typesList = eTypes.getChildren("build-type");
				if (typesList.size() > 0){
					for(Element eType : typesList)
					{
						if (eType.getAttributeValue("id")!= null){
							enabledBuildTypesSet.add(eType.getAttributeValue("id"));
						}
					}
				}
			}
		}
		
		if(e.getChild("parameters") != null){
			Element eParams = e.getChild("parameters");
			List<Element> paramsList = eParams.getChildren("param");
			if (paramsList.size() > 0){
				for(Element eParam : paramsList)
				{
					this.extraParameters.put(
							eParam.getAttributeValue("name"), 
							eParam.getAttributeValue("value")
							);
				}
			}
		}
		
		if(e.getChild("custom-templates") != null){
			Element eParams = e.getChild("custom-templates");
			List<Element> templateList = eParams.getChildren("custom-template");
			if (templateList.size() > 0){
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
			if (eAuth.getAttribute("type") != null){
				// We have an "auth" element
				// Try to get the enabled flag
				authType = eAuth.getAttribute("type").getValue();
				try {
					authEnabled = eAuth.getAttribute("enabled").getBooleanValue();
				} catch (DataConversionException e1){
					// And if it can't be read as boolean default it 
					// to true anyway (since we have the auth type).
					authEnabled = true;
				}
				try {
					if (eAuth.getAttribute("preemptive") != null){
						authPreemptive = eAuth.getAttribute("preemptive").getBooleanValue();
					}
				} catch (DataConversionException e1){
					// And if it can't be read as boolean default it 
					// to true (which means creds are always sent).
					authPreemptive = true;
				}
				Element eParams = eAuth.getChild("auth-parameters");
				if (eParams != null){
					List<Element> paramsList = eParams.getChildren("param");
					if (paramsList.size() > 0){
						for(Element eParam : paramsList)
						{
							this.authParameters.put(
									eParam.getAttributeValue("name"), 
									eParam.getAttributeValue("value")
									);
						}
					}
				}
			}

		}
		
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
	 */
	public WebHookConfig (String url, Boolean enabled, BuildState states, String payloadFormat, boolean buildTypeAllEnabled, boolean buildTypeSubProjects, Set<String> enabledBuildTypes){
		int Min = 1000000, Max = 1000000000;
		Integer Rand = Min + (int)(Math.random() * ((Max - Min) + 1));
		this.uniqueKey = Rand.toString();
		this.extraParameters = new TreeMap<String,String>();
		this.templates = new TreeMap<String,CustomMessageTemplate>();
		this.setUrl(url);
		this.setEnabled(enabled);
		this.setBuildStates(states);
		this.setPayloadFormat(payloadFormat);
		this.subProjectsEnabled = buildTypeSubProjects;
		this.allBuildTypesEnabled = buildTypeAllEnabled;
		if (!this.allBuildTypesEnabled){
			this.enabledBuildTypesSet = enabledBuildTypes;
		}
	}

	private Element getKeyAndValueAsElement(Map<String,String> map, String key, String elementName){
		Element e = new Element(elementName);
		if (map.containsKey(key)){
			e.setAttribute("name", key);
			e.setAttribute("value",map.get(key));
		}
		return e;
	}
	
	public Element getAsElement(){
		Element el = new Element("webhook");
		el.setAttribute("url", this.getUrl());
		el.setAttribute("enabled", String.valueOf(this.enabled));
		el.setAttribute("format", String.valueOf(this.payloadFormat).toLowerCase());
		
		Element statesEl = new Element("states");
		for (BuildStateEnum state : states.getStateSet()){
			Element e = new Element("state");
			e.setAttribute("type", state.getShortName());
			e.setAttribute("enabled", Boolean.toString(states.enabled(state)));
			statesEl.addContent(e);
		}
		el.addContent(statesEl);
		
		Element buildsEl = new Element("build-types");
		buildsEl.setAttribute("enabled-for-all", Boolean.toString(isEnabledForAllBuildsInProject()));
		buildsEl.setAttribute("enabled-for-subprojects", Boolean.toString(isEnabledForSubProjects()));
		
		if (this.enabledBuildTypesSet.size() > 0){
			for (String i : enabledBuildTypesSet){
				Element e = new Element("build-type");
				e.setAttribute("id", i);
				buildsEl.addContent(e);
			}
		}
		el.addContent(buildsEl);
		
		if (this.extraParameters.size() > 0){
			Element paramsEl = new Element("parameters");
			for (String i : this.extraParameters.keySet()){
				paramsEl.addContent(this.getKeyAndValueAsElement(this.extraParameters, i, "param"));
			}
			el.addContent(paramsEl);
		}
		
		if (this.templates.size() > 0){
			Element templatesEl = new Element("custom-templates");
			for (CustomMessageTemplate t : this.templates.values()){
				templatesEl.addContent(t.getAsElement());
			}
			el.addContent(templatesEl);
		}
		
		if (this.authType != ""){
			Element authEl = new Element("auth");
			authEl.setAttribute("enabled", this.authEnabled.toString());
			authEl.setAttribute("type", this.authType);
			authEl.setAttribute("preemptive", this.authPreemptive.toString() );
			if (this.authParameters.size() > 0){
				Element paramsEl = new Element("auth-parameters");
				for (String i : this.authParameters.keySet()){
					paramsEl.addContent(this.getKeyAndValueAsElement(this.authParameters, i, "param"));
				}
				authEl.addContent(paramsEl);
			}
			el.addContent(authEl);
		}
		
		return el;
	}
	
	// Getters and Setters..

	public SortedMap<String,String> getParams() {
		return extraParameters;
	}
//
//	public void setParams(List<NameValuePair> params) {
//		this.params = params;
//	}
	
	public boolean isEnabledForBuildType(SBuildType sBuildType){
		// If allBuildTypes enabled, return true, otherwise  return whether the build is in the list of enabled buildTypes. 
		return isEnabledForAllBuildsInProject() ? true : enabledBuildTypesSet.contains(TeamCityIdResolver.getInternalBuildId(sBuildType));
	}
	
	public boolean isSpecificBuildTypeEnabled(SBuildType sBuildType){
		// Just check if this build type is only enabled for a specific build. 
		return enabledBuildTypesSet.contains(TeamCityIdResolver.getInternalBuildId(sBuildType));
	}
	
	public String getBuildTypeCountAsFriendlyString(){
		if (this.allBuildTypesEnabled  && !this.subProjectsEnabled){
			return "All builds";
		} else if (this.allBuildTypesEnabled  && this.subProjectsEnabled){
				return "All builds & Sub-Projects";
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
			if (states.enabled(BuildStateEnum.BUILD_STARTED)){
				enabledStates += ", Build Started";
			}
//			if (BuildState.enabled(BuildState.BUILD_FINISHED,this.statemask)){
//				enabledStates += ", Build Completed";
//			}
//			if (BuildState.enabled(BuildState.BUILD_CHANGED_STATUS,this.statemask)){
//				enabledStates += ", Build Changed Status";
//			}
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
			if (enabledStates.length() > 0){
				return enabledStates.substring(1);
			} else {
				return "None";
			}
		}
	}
	
	public String getWebHookEnabledAsChecked() {
		if (this.enabled){
			return "checked ";
		}
		return ""; 
	}
	
	public String getStateAllAsChecked() {
		if (states.allEnabled()){
			return "checked ";
		}		
		return ""; 
	}
	
	public String getStateBuildStartedAsChecked() {
		if (states.enabled(BUILD_STARTED)){
			return "checked ";
		}
		return ""; 
	}
	
	public String getStateBuildFinishedAsChecked() {
		if (states.enabled(BUILD_FINISHED)){
			return "checked ";
		}
		return ""; 
	}

	public String getStateBeforeFinishedAsChecked() {
		if (states.enabled(BEFORE_BUILD_FINISHED)){
			return "checked ";
		}
		return ""; 
	}

	public String getStateResponsibilityChangedAsChecked() {
		if (states.enabled(RESPONSIBILITY_CHANGED)){
			return "checked ";
		}
		return ""; 
	}

	public String getStateBuildInterruptedAsChecked() {
		if (states.enabled(BUILD_INTERRUPTED)){
			return "checked ";
		}
		return ""; 
	}
	
	public String getStateBuildSuccessfulAsChecked() {
		if (states.enabled(BUILD_SUCCESSFUL)){
			return "checked ";
		}
		return ""; 
	}
	
	public String getStateBuildFixedAsChecked() {
		if (states.enabled(BUILD_FIXED)){
			return "checked ";
		}
		return ""; 
	}
	
	public String getStateBuildFailedAsChecked() {
		if (states.enabled(BUILD_FAILED)){
			return "checked ";
		}
		return ""; 
	}

	public String getStateBuildBrokenAsChecked() {
		if (states.enabled(BUILD_BROKEN)){
			return "checked ";
		}
		return ""; 
	}
	
	public String getPayloadFormat() {
		return payloadFormat;
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

	public WebHookAuthConfig getAuthenticationConfig() {
		if (authEnabled && !authType.equals("")){
			WebHookAuthConfig webhookAuthConfig= new WebHookAuthConfig();
			webhookAuthConfig.type = authType;
			webhookAuthConfig.preemptive = authPreemptive;
			webhookAuthConfig.parameters.putAll(authParameters);
			return webhookAuthConfig;
		}
		return null;
	}	
	
}
