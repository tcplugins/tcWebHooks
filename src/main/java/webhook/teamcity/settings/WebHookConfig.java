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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jdom.Element;

import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.settings.convertor.WebHookBuildStateConvertor;


public class WebHookConfig {
	private SortedMap<String,String> extraParameters;
	private Boolean enabled = true;
	private String uniqueKey = "";
	private String url;
	private String payloadFormat = null;
	private BuildState states = new BuildState();
	
	@SuppressWarnings("unchecked")
	public WebHookConfig (Element e){
		
		int Min = 1000000, Max = 1000000000;
		Integer Rand = Min + (int)(Math.random() * ((Max - Min) + 1));
		this.uniqueKey = Rand.toString();
		this.extraParameters = new TreeMap<String,String>();
		
		if (e.getAttribute("url") != null){
			this.setUrl(e.getAttributeValue("url"));
		}
		
		if (e.getAttribute("enabled") != null){
			this.setEnabled(Boolean.parseBoolean(e.getAttributeValue("enabled")));
		}

		if (e.getAttribute("statemask") != null){
			this.setBuildStates(WebHookBuildStateConvertor.convert(Integer.parseInt(e.getAttributeValue("statemask"))));
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
				for(Iterator<Element> state = statesList.iterator(); state.hasNext();)
				{
					Element eState = state.next();
						states.setEnabled(
									BuildStateEnum.findBuildState(eState.getAttributeValue("type")), 
									Boolean.getBoolean(eState.getAttributeValue("enabled"))
								);
				}
			}
		}
		
		if(e.getChild("parameters") != null){
			Element eParams = e.getChild("parameters");
			List<Element> paramsList = eParams.getChildren("param");
			if (paramsList.size() > 0){
				for(Iterator<Element> param = paramsList.iterator(); param.hasNext();)
				{
					Element eParam = param.next();
					this.extraParameters.put(
							eParam.getAttributeValue("name"), 
							eParam.getAttributeValue("value")
							);
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
	public WebHookConfig (String url, Boolean enabled, BuildState states, String payloadFormat){
		int Min = 1000000, Max = 1000000000;
		Integer Rand = Min + (int)(Math.random() * ((Max - Min) + 1));
		this.uniqueKey = Rand.toString();
		this.extraParameters = new TreeMap<String,String>();
		this.setUrl(url);
		this.setEnabled(enabled);
		this.setBuildStates(states);
		this.setPayloadFormat(payloadFormat);
	}

	
/*	public WebHookConfig (String key, String url, Boolean enabled, Integer stateMask){
		this.params = new ArrayList<NameValuePair>();
		this.setUrl(url);
		this.setEnabled(enabled);
		this.setStatemask(stateMask);
		this.setUniqueKey(key);
	}
*/	
	private Element getKeyAndValueAsElement(String key, String elementName){
		Element e = new Element(elementName);
		if (this.extraParameters.containsKey(key)){
			e.setAttribute("name", key);
			e.setAttribute("value",this.extraParameters.get(key));
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
		
		if (this.extraParameters.size() > 0){
			Element paramsEl = new Element("parameters");
			for (Iterator<String> i = this.extraParameters.values().iterator(); i.hasNext();){
				paramsEl.addContent(this.getKeyAndValueAsElement(i.next(), "param"));
			}
			el.addContent(paramsEl);
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
			return "All Builds";
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

	/*
	    public static final Integer BUILD_STARTED  			= Integer.parseInt("00000001",2);
		public static final Integer BUILD_FINISHED 			= Integer.parseInt("00000010",2);
	    public static final Integer BUILD_CHANGED_STATUS 	= Integer.parseInt("00000100",2);
	    public static final Integer BEFORE_BUILD_FINISHED 	= Integer.parseInt("00001000",2);
	    public static final Integer RESPONSIBILITY_CHANGED 	= Integer.parseInt("00010000",2);
	    public static final Integer BUILD_INTERRUPTED 		= Integer.parseInt("00100000",2);
	 */
	
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
	 * It does NOT check that the payload format has a valid implimentation loaded.
	 * 
	 * @param payloadFormat
	 */
	public void setPayloadFormat(String payloadFormat) {
		this.payloadFormat = payloadFormat;
	}	
	/**
	 * Sets the payload format, but only if it is in the set.
	 *  
	 * @param payloadFormat
	 * @param availableFormats
	 */
	public Boolean setPayloadFormat(String payloadFormat, Set<String> availableFormats) {
		if (availableFormats.contains(payloadFormat)){
			this.payloadFormat = payloadFormat;
			return true;
		}
		return false;
	}
	
}
