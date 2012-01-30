package webhook.teamcity.settings;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jdom.Element;

import webhook.teamcity.BuildState;


public class WebHookConfig {
	private SortedMap<String,String> extraParameters;
	private Boolean enabled = true;
	private Integer statemask = BuildState.ALL_ENABLED; // Enable all eight bits by default. 
	private String uniqueKey = "";
	private String url;
	private String payloadFormat = null;
	
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
			this.setStatemask(Integer.parseInt(e.getAttributeValue("statemask")));
			
			// upgrade from old bit mask to new bit mask.
			int oldState = statemask;
			if (BuildState.enabled(BuildState.BUILD_FINISHED, oldState)){
				// Remove the BUILD_FINISHED and BUILD_CHANGED_STATUS states by 
				// ANDing with ALL_ENABLED which has BUILD_FINISHED and 
				// BUILD_CHANGED_STATUS set to zero.
				this.statemask = (this.statemask & BuildState.ALL_ENABLED);
				// Now OR with SUCCESSFUL
				this.statemask = (this.statemask | BuildState.BUILD_SUCCESSFUL);
				// and OR with FAILED
				this.statemask = (this.statemask | BuildState.BUILD_FAILED);
			}
			
			// upgrade from old bit mask to new bit mask for BUILD_CHANGED_STATUS
			// Most times, we'll end up doing it twice, but it's better than none.
			if (BuildState.enabled(BuildState.BUILD_CHANGED_STATUS, oldState)){
				// Remove the BUILD_FINISHED and BUILD_CHANGED_STATUS states by 
				// ANDing with ALL_ENABLED which has BUILD_FINISHED and 
				// BUILD_CHANGED_STATUS set to zero.
				this.statemask = (this.statemask & BuildState.ALL_ENABLED);
			}
			
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
	public WebHookConfig (String url, Boolean enabled, Integer stateMask, String payloadFormat){
		int Min = 1000000, Max = 1000000000;
		Integer Rand = Min + (int)(Math.random() * ((Max - Min) + 1));
		this.uniqueKey = Rand.toString();
		this.extraParameters = new TreeMap<String,String>();
		this.setUrl(url);
		this.setEnabled(enabled);
		this.setStatemask(stateMask);
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
		el.setAttribute("statemask", String.valueOf(this.statemask));
		el.setAttribute("format", String.valueOf(this.payloadFormat).toLowerCase());
		
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

	public Integer getStatemask() {
		return statemask;
	}

	public void setStatemask(Integer statemask) {
		this.statemask = statemask;
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
		} else if (this.statemask.equals(BuildState.ALL_ENABLED)){
			return "All Builds";
		} else if (this.statemask.equals(0)) {
			return "None";
		} else {
			String enabledStates = "";
			if (BuildState.enabled(BuildState.BUILD_STARTED,this.statemask)){
				enabledStates += ", Build Started";
			}
//			if (BuildState.enabled(BuildState.BUILD_FINISHED,this.statemask)){
//				enabledStates += ", Build Completed";
//			}
//			if (BuildState.enabled(BuildState.BUILD_CHANGED_STATUS,this.statemask)){
//				enabledStates += ", Build Changed Status";
//			}
			if (BuildState.enabled(BuildState.BUILD_INTERRUPTED,this.statemask)){
				enabledStates += ", Build Interrupted";
			}
			if (BuildState.enabled(BuildState.BEFORE_BUILD_FINISHED,this.statemask)){
				enabledStates += ", Build Almost Completed";
			}
			if (BuildState.enabled(BuildState.RESPONSIBILITY_CHANGED,this.statemask)){
				enabledStates += ", Build Responsibility Changed";
			}
			if (BuildState.enabled(BuildState.BUILD_FAILED,this.statemask)){
				if (BuildState.enabled(BuildState.BUILD_BROKEN, this.statemask)){
					enabledStates += ", Build Broken";
				} else {
					enabledStates += ", Build Failed";
				}
			}
			if (BuildState.enabled(BuildState.BUILD_SUCCESSFUL,this.statemask)){
				if (BuildState.enabled(BuildState.BUILD_FIXED, this.statemask)){
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
		if (this.statemask.equals(BuildState.ALL_ENABLED)){
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
		if (BuildState.enabled(BuildState.BUILD_STARTED,this.statemask)){
			return "checked ";
		}
		return ""; 
	}
	
	public String getStateBuildFinishedAsChecked() {
		if (BuildState.enabled(BuildState.BUILD_FINISHED,this.statemask)){
			return "checked ";
		}
		return ""; 
	}

	public String getStateBuildChangedStatusAsChecked() {
		if (BuildState.enabled(BuildState.BUILD_CHANGED_STATUS,this.statemask)){
			return "checked ";
		}
		return ""; 
	}

	public String getStateBeforeFinishedAsChecked() {
		if (BuildState.enabled(BuildState.BEFORE_BUILD_FINISHED,this.statemask)){
			return "checked ";
		}
		return ""; 
	}

	public String getStateResponsibilityChangedAsChecked() {
		if (BuildState.enabled(BuildState.RESPONSIBILITY_CHANGED,this.statemask)){
			return "checked ";
		}
		return ""; 
	}

	public String getStateBuildInterruptedAsChecked() {
		if (BuildState.enabled(BuildState.BUILD_INTERRUPTED,this.statemask)){
			return "checked ";
		}
		return ""; 
	}
	
	public String getStateBuildSuccessfulAsChecked() {
		if (BuildState.enabled(BuildState.BUILD_SUCCESSFUL,this.statemask)){
			return "checked ";
		}
		return ""; 
	}
	
	public String getStateBuildFixedAsChecked() {
		if (BuildState.enabled(BuildState.BUILD_FIXED,this.statemask)){
			return "checked ";
		}
		return ""; 
	}
	
	public String getStateBuildFailedAsChecked() {
		if (BuildState.enabled(BuildState.BUILD_FAILED,this.statemask)){
			return "checked ";
		}
		return ""; 
	}

	public String getStateBuildBrokenAsChecked() {
		if (BuildState.enabled(BuildState.BUILD_BROKEN,this.statemask)){
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
