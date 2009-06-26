package webhook.teamcity.settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;
import org.jdom.Element;

import webhook.teamcity.BuildState;


public class WebHookConfig {
	private List<NameValuePair> params;
	private Boolean enabled = true;
	private Integer statemask = 255; // Enable all eight bits by default. 
	private String uniqueKey = "";
	private String url;
	
	@SuppressWarnings("unchecked")
	public WebHookConfig (Element e){
		
		int Min = 1000000, Max = 1000000000;
		Integer Rand = Min + (int)(Math.random() * ((Max - Min) + 1));
		this.uniqueKey = Rand.toString();
		this.params = new ArrayList<NameValuePair>();
		
		if (e.getAttribute("url") != null){
			this.setUrl(e.getAttributeValue("url"));
		}
		
		if (e.getAttribute("enabled") != null){
			this.setEnabled(Boolean.parseBoolean(e.getAttributeValue("enabled")));
		}

		if (e.getAttribute("statemask") != null){
			this.setStatemask(Integer.parseInt(e.getAttributeValue("statemask")));
		}

		if (e.getAttribute("key") != null){
			this.setUniqueKey(e.getAttributeValue("key"));
		}
		
		if(e.getChild("parameters") != null){
			Element eParams = e.getChild("parameters");
			List<Element> paramsList = eParams.getChildren("param");
			if (paramsList.size() > 0){
				for(Iterator<Element> param = paramsList.iterator(); param.hasNext();)
				{
					Element eParam = param.next();
					this.params.add(new NameValuePair(
							eParam.getAttributeValue("name"), 
							eParam.getAttributeValue("value")
							));
				}
			}
		}
	}
	
	public WebHookConfig (String url, Boolean enabled, Integer stateMask){
		int Min = 1000000, Max = 1000000000;
		Integer Rand = Min + (int)(Math.random() * ((Max - Min) + 1));
		this.uniqueKey = Rand.toString();
		this.params = new ArrayList<NameValuePair>();
		this.setUrl(url);
		this.setEnabled(enabled);
		this.setStatemask(stateMask);
	}

	public WebHookConfig (String key, String url, Boolean enabled, Integer stateMask){
		this.params = new ArrayList<NameValuePair>();
		this.setUrl(url);
		this.setEnabled(enabled);
		this.setStatemask(stateMask);
		this.setUniqueKey(key);
	}
	
	private Element getNameValueAsElement(NameValuePair nv, String elementName){
		Element e = new Element(elementName);
		e.setAttribute("name", nv.getName());
		e.setAttribute("value",nv.getValue());
		return e;
	}
	
	public Element getAsElement(){
		Element el = new Element("webhook");
		el.setAttribute("url", this.getUrl());
		el.setAttribute("enabled", String.valueOf(this.enabled));
		el.setAttribute("statemask", String.valueOf(this.statemask));
		
		if (this.params.size() > 0){
			Element paramsEl = new Element("parameters");
			for (Iterator<NameValuePair> i = this.params.iterator(); i.hasNext();){
				paramsEl.addContent(this.getNameValueAsElement(i.next(), "param"));
			}
			el.addContent(paramsEl);
		}
		return el;
	}
	
	// Getters and Setters..

	public List<NameValuePair> getParams() {
		return params;
	}

	public void setParams(List<NameValuePair> params) {
		this.params = params;
	}

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
		if (this.statemask == BuildState.ALL_ENABLED){
			return "All";
		} else if (this.statemask == 0) {
			return "None";
		} else {
			String enabledStates = "";
			if (BuildState.enabled(BuildState.BUILD_STARTED,this.statemask)){
				enabledStates += ", Build Started";
			}
			if (BuildState.enabled(BuildState.BUILD_FINISHED,this.statemask)){
				enabledStates += ", Build Completed";
			}
			if (BuildState.enabled(BuildState.BUILD_CHANGED_STATUS,this.statemask)){
				enabledStates += ", Build Changed Status";
			}
			if (BuildState.enabled(BuildState.BUILD_INTERRUPTED,this.statemask)){
				enabledStates += ", Build Interrupted";
			}
			if (BuildState.enabled(BuildState.BEFORE_BUILD_FINISHED,this.statemask)){
				enabledStates += ", Build Almost Completed";
			}
			if (BuildState.enabled(BuildState.RESPONSIBILITY_CHANGED,this.statemask)){
				enabledStates += ", Build Responsibility Changed";
			}
			return enabledStates.substring(1);
		}
	}
	
	public String getWebHookEnabledAsChecked() {
		if (this.enabled){
			return "checked ";
		}
		return ""; 
	}
	
	public String getStateAllAsChecked() {
		if (this.statemask == 255){
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

	
}
