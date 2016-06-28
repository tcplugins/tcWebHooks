package webhook.teamcity.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.settings.ProjectSettings;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;

import org.jdom.Element;

import webhook.teamcity.BuildState;
import webhook.teamcity.Loggers;


public class WebHookProjectSettings implements ProjectSettings {
	private static final String NAME = WebHookProjectSettings.class.getName();
	ProjectSettingsManager psm;
	ProjectSettings ps;
	private Boolean webHooksEnabled = true;
	private Boolean updateSuccess = false;
	private String updateMessage = "";
	private CopyOnWriteArrayList<WebHookConfig> webHooksConfigs;
	
	public WebHookProjectSettings(){
		webHooksConfigs = new CopyOnWriteArrayList<>();
	}

    @SuppressWarnings("unchecked")
	public void readFrom(Element rootElement)
    /* Is passed an Element by TC, and is expected to load it into the in memory settings object.
     * Old settings should be overwritten.
     */
    {
    	Loggers.SERVER.debug("readFrom :: " + rootElement.toString());
    	CopyOnWriteArrayList<WebHookConfig> configs = new CopyOnWriteArrayList<>();
    	
    	if (rootElement.getAttribute("enabled") != null){
    		this.webHooksEnabled = Boolean.parseBoolean(rootElement.getAttributeValue("enabled"));
    	}
    	
		List<Element> namedChildren = rootElement.getChildren("webhook");
        if(namedChildren.size() == 0)
        {
            this.webHooksConfigs = null;
        } else {
			for(Element e :  namedChildren)
	        {
				WebHookConfig whConfig = new WebHookConfig(e);
				Loggers.SERVER.debug(e.toString());
				configs.add(whConfig);
				Loggers.SERVER.debug(NAME + ":readFrom :: url " + whConfig.getUrl());
				Loggers.SERVER.debug(NAME + ":readFrom :: enabled " + String.valueOf(whConfig.getEnabled()));
				Loggers.SERVER.debug(NAME + ":readFrom :: payloadFormat " + String.valueOf(whConfig.getPayloadFormat()));
	        }
			this.webHooksConfigs = configs;
    	}
    }

    public void writeTo(Element parentElement)
    /* Is passed an (probably empty) Element by TC, which is expected to be populated from the settings
     * in memory. 
     */
    {
    	Loggers.SERVER.debug(NAME + ":writeTo :: " + parentElement.toString());
    	parentElement.setAttribute("enabled", String.valueOf(this.webHooksEnabled));
        if(webHooksConfigs != null)
        {
            for(WebHookConfig whc : webHooksConfigs){
            	Element el = whc.getAsElement();
            	Loggers.SERVER.debug(el.toString());
                parentElement.addContent(el);
				Loggers.SERVER.debug(NAME + ":writeTo :: url " + whc.getUrl());
				Loggers.SERVER.debug(NAME + ":writeTo :: enabled " + String.valueOf(whc.getEnabled()));
				Loggers.SERVER.debug(NAME + ":writeTo :: payloadFormat " + String.valueOf(whc.getPayloadFormat()));
            }

        }
    }
    
    public List<WebHookConfig> getWebHooksAsList(){
    	return this.webHooksConfigs;
    }    
    
    public List<WebHookConfig> getProjectWebHooksAsList(){
    	List<WebHookConfig> projHooks = new ArrayList<>();
    	for (WebHookConfig config : getWebHooksAsList()){
    		if (config.isEnabledForAllBuildsInProject()){
    			projHooks.add(config);
    		}
    	}
    	return projHooks;
    }    
    
    public List<WebHookConfig> getBuildWebHooksAsList(SBuildType buildType){
    	List<WebHookConfig> buildHooks = new ArrayList<>();
    	for (WebHookConfig config : getWebHooksAsList()){
    		if (config.isSpecificBuildTypeEnabled(buildType)){
    			buildHooks.add(config);
    		}
    	}
    	return buildHooks;
    }    
        
	
    public String getWebHooksAsString(){
    	String tmpString = "";
    	for(WebHookConfig whConf : webHooksConfigs)
    	{
    		tmpString += whConf.getUrl() + "<br/>";
    	}
    	return tmpString;
    }

    public void deleteWebHook(String webHookId, String ProjectId){
        if(this.webHooksConfigs != null)
        {
        	updateSuccess = false;
        	updateMessage = "";
        	List<WebHookConfig> tempWebHookList = new ArrayList<>();
            for(WebHookConfig whc : webHooksConfigs)
            {
                if (whc.getUniqueKey().equals(webHookId)){
                	Loggers.SERVER.debug(NAME + ":deleteWebHook :: Deleting webhook from " + ProjectId + " with URL " + whc.getUrl());
                	tempWebHookList.add(whc);
                }
            }
            if (tempWebHookList.size() > 0){
            	this.updateSuccess = true;
            	this.webHooksConfigs.removeAll(tempWebHookList);	
            }
        }    	
    }

	public void updateWebHook(String ProjectId, String webHookId, String URL, Boolean enabled, BuildState buildState, String format, String template, boolean buildTypeAll, boolean buildSubProjects, Set<String> buildTypesEnabled) {
        if(this.webHooksConfigs != null)
        {
        	updateSuccess = false;
        	updateMessage = "";
            for(WebHookConfig whc : webHooksConfigs)
            {
                if (whc.getUniqueKey().equals(webHookId)){
                	whc.setEnabled(enabled);
                	whc.setUrl(URL);
                	whc.setBuildStates(buildState);
                	whc.setPayloadFormat(format);
                	whc.setPayloadTemplate(template);
                	whc.enableForSubProjects(buildSubProjects);
                	whc.enableForAllBuildsInProject(buildTypeAll);
                	if (!buildTypeAll){
                		whc.clearAllEnabledBuildsInProject();
                		for (String bt : buildTypesEnabled){
                			whc.enableBuildInProject(bt);
                		}
                	}
                	Loggers.SERVER.debug(NAME + ":updateWebHook :: Updating webhook from " + ProjectId + " with URL " + whc.getUrl());
                   	this.updateSuccess = true;
                }
            }
        }    			
	}

	public void addNewWebHook(String ProjectId, String URL, Boolean enabled, BuildState buildState, String format, String template, boolean buildTypeAll, boolean buildTypeSubProjects, Set<String> buildTypesEnabled) {
		this.webHooksConfigs.add(new WebHookConfig(URL, enabled, buildState, format, template, buildTypeAll, buildTypeSubProjects, buildTypesEnabled));
		Loggers.SERVER.debug(NAME + ":addNewWebHook :: Adding webhook to " + ProjectId + " with URL " + URL);
		this.updateSuccess = true;
	}

    public Boolean updateSuccessful(){
    	return this.updateSuccess;
    }
    
	public void dispose() {
		Loggers.SERVER.debug(NAME + ":dispose() called");
	}

	public Integer getWebHooksCount(){
		return this.webHooksConfigs.size();
	}
	
	public boolean isEnabled() {
		return webHooksEnabled;
	}

	public String isEnabledAsChecked() {
		if (this.webHooksEnabled){
			return "checked ";
		}
		return "";
	}
	
	public List<WebHookConfig> getWebHooksConfigs() {
		return webHooksConfigs;
	}

	public String getUpdateMessage() {
		return updateMessage;
	}

}
