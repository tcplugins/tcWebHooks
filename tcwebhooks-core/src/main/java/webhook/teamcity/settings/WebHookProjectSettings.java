package webhook.teamcity.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jdom.Element;

import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.settings.ProjectSettings;
import webhook.teamcity.BuildState;
import webhook.teamcity.Loggers;
import webhook.teamcity.auth.WebHookAuthConfig;


public class WebHookProjectSettings implements ProjectSettings {
	private static final String NAME = WebHookProjectSettings.class.getName();
	private Boolean webHooksEnabled = true;
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
        if(namedChildren.isEmpty())
        {
            this.webHooksConfigs = null;
        } else {
			for(Element e :  namedChildren)
	        {
				WebHookConfig whConfig = new WebHookConfig(e);
				Loggers.SERVER.debug(e.toString());
				configs.add(whConfig);
				Loggers.SERVER.debug(NAME + ":readFrom :: url " + whConfig.getUrl());
				Loggers.SERVER.debug(NAME + ":readFrom :: enabled " + whConfig.getEnabled());
				Loggers.SERVER.debug(NAME + ":readFrom :: payloadTemplate " + whConfig.getPayloadTemplate());
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
				Loggers.SERVER.debug(NAME + ":writeTo :: enabled " + whc.getEnabled());
				Loggers.SERVER.debug(NAME + ":writeTo :: payloadTemplate " + whc.getPayloadTemplate());
            }

        }
    }
    
    public List<WebHookConfig> getWebHooksAsList(){
    	if (this.webHooksConfigs == null) {
    		return new ArrayList<>();
    	}
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
    	StringBuilder tmpString = new StringBuilder();
    	for(WebHookConfig whConf : webHooksConfigs)
    	{
    		tmpString.append(whConf.getUrl()).append("<br/>");
    	}
    	return tmpString.toString();
    }

    public WebHookUpdateResult deleteWebHook(String webHookId, String projectId){
    	WebHookConfig configToDelete = null;
    	boolean updateSuccess = false;
    	
        if(this.webHooksConfigs != null)
        {
        	List<WebHookConfig> tempWebHookList = new ArrayList<>();
            for(WebHookConfig whc : webHooksConfigs)
            {
                if (whc.getUniqueKey().equals(webHookId)){
                	Loggers.SERVER.debug(NAME + ":deleteWebHook :: Deleting webhook from " + projectId + " with URL " + whc.getUrl());
                	tempWebHookList.add(whc);
                	configToDelete = whc;
                }
            }
            if (! tempWebHookList.isEmpty()){
            	updateSuccess = true;
            	this.webHooksConfigs.removeAll(tempWebHookList);	
            }
        }
        return new WebHookUpdateResult(updateSuccess, configToDelete);
    }

	public WebHookUpdateResult updateWebHook(String projectId, String webHookId, String url, Boolean enabled, BuildState buildState, String template, boolean buildTypeAll, boolean buildSubProjects, Set<String> buildTypesEnabled, WebHookAuthConfig webHookAuthConfig) {
		boolean updateSuccess = false;
		WebHookConfig configToUpdate = null;
        if(this.webHooksConfigs != null)
        {
            for(WebHookConfig whc : webHooksConfigs)
            {
                if (whc.getUniqueKey().equals(webHookId)){
                	whc.setEnabled(enabled);
                	whc.setUrl(url);
                	whc.setBuildStates(buildState);
                	whc.setPayloadTemplate(template);
                	whc.enableForSubProjects(buildSubProjects);
                	whc.enableForAllBuildsInProject(buildTypeAll);
                	if (!buildTypeAll){
                		whc.clearAllEnabledBuildsInProject();
                		for (String bt : buildTypesEnabled){
                			whc.enableBuildInProject(bt);
                		}
                	}
            		if (webHookAuthConfig != null){
            			whc.setAuthEnabled(true);
            			whc.setAuthType(webHookAuthConfig.getType());
            			whc.setAuthPreemptive(webHookAuthConfig.getPreemptive());
            			whc.setAuthParameters(webHookAuthConfig.getParameters());
            		} else {
            			whc.setAuthEnabled(false);
            			whc.setAuthType("");
            			whc.setAuthPreemptive(true);
            			whc.clearAuthParameters();
            		}
                	Loggers.SERVER.debug(NAME + ":updateWebHook :: Updating webhook from " + projectId + " with URL " + whc.getUrl());
                   	updateSuccess = true;
                   	configToUpdate = whc;
                }
            }
        }
        return new WebHookUpdateResult(updateSuccess, configToUpdate);
	}

	public void addNewWebHook(String projectInternalId, String projectExternalId, String url, Boolean enabled, BuildState buildState, String template, boolean buildTypeAll, boolean buildTypeSubProjects, Set<String> buildTypesEnabled) {
		addNewWebHook(projectInternalId, projectExternalId, url, enabled, buildState, template, buildTypeAll, buildTypeSubProjects, buildTypesEnabled, null);
	}
	
	public WebHookUpdateResult addNewWebHook(String projectInternalId, String projectExternalId, String url, Boolean enabled, BuildState buildState, String template, boolean buildTypeAll, boolean buildTypeSubProjects, Set<String> buildTypesEnabled, WebHookAuthConfig webHookAuthConfig) {
		WebHookConfig newWebHook = new WebHookConfig(projectInternalId, projectExternalId, url, enabled, buildState, template, buildTypeAll, buildTypeSubProjects, buildTypesEnabled, webHookAuthConfig); 
		this.webHooksConfigs.add(newWebHook);
		Loggers.SERVER.debug(NAME + ":addNewWebHook :: Adding webhook to " + projectExternalId + " with URL " + url);
		return new WebHookUpdateResult(true, newWebHook);
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
	
}
