package webhook.teamcity.payload.template;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateBranchText;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateFormat;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateItems;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateText;

public abstract class AbstractPropertiesBasedWebHookTemplate extends AbstractWebHookTemplate {
	
	Map<BuildStateEnum,WebHookTemplateContent> templateContent = new HashMap<>();
	Map<BuildStateEnum,WebHookTemplateContent> branchTemplateContent = new HashMap<>();
	private WebHookTemplateText defaultTemplateText;
	private WebHookTemplateBranchText defaultBranchTemplateText;

	public abstract String getLoggingName();
	public abstract String getPropertiesFileName();

	public AbstractPropertiesBasedWebHookTemplate(WebHookTemplateManager manager) {
		this.manager = manager;
	}
	
	@Override
	public void register() {
		templateContent.clear();
		branchTemplateContent.clear();
		loadTemplatesFromPropertiesFile();
		if (!templateContent.isEmpty() && !branchTemplateContent.isEmpty()){
			this.manager.registerTemplateFormatFromSpring(this);
		} else {
			if (templateContent.isEmpty()){
				Loggers.SERVER.error(getLoggingName() + " :: Failed to register template " + getTemplateShortName() + ". No regular template configurations were found.");
			}
			if (branchTemplateContent.isEmpty()){
				Loggers.SERVER.error(getLoggingName() + " :: Failed to register template " + getTemplateShortName() + ". No branch template configurations were found.");
			}
		}
	}

	private URL findPropertiesFileUrlInVariousClassloaders(String propertiesFile) {
		final ClassLoader[] classLoaders = {AbstractPropertiesBasedWebHookTemplate.class.getClassLoader(), ClassLoader.getSystemClassLoader()}; 
		URL url = null;
		for (ClassLoader cl : classLoaders){
			if (cl != null){
				url = cl.getResource(propertiesFile);
		        if (url != null){
		        	break;
		        }
			}
		}
		return url;
	}

	/**
	 * Load the template from a properties file, rather than doing silly string escaping in java.
	 */
	private void loadTemplatesFromPropertiesFile() {
		Properties props = null;
		URL url = findPropertiesFileUrlInVariousClassloaders(getPropertiesFileName());
	    if (url != null) {
	        try {
	            InputStream in = url.openStream();
	            props = new Properties();
	            props.load(in);
	        } catch (IOException e) {
	        	Loggers.SERVER.error(getLoggingName() + " :: An Error occurred trying to load the template properties file: " + getPropertiesFileName() + ".");
	        	Loggers.SERVER.debug(e);
	        	
	        } finally {
	           // close opened resources
	        }
	    } else {
	    	Loggers.SERVER.error(getLoggingName() + " :: An Error occurred trying to load the template properties file: " + getPropertiesFileName() + ". The file was not found in the classpath.");
	    }
	    if (props != null){
	    	String templatePropKey = "";
	    	
	    	// If the default template is set, initialise the list for all states first.
	    	templatePropKey = "template.default";
    		if (props.containsKey(templatePropKey)){
    			this.defaultTemplateText = new WebHookTemplateText(props.getProperty(templatePropKey));
    			
    			for (BuildStateEnum state : BuildStateEnum.getNotifyStates()){
	    			templateContent.put(state, WebHookTemplateContent.create(
	    																state.getShortName(), 
	    																props.getProperty(templatePropKey),
	    																true,
	    																this.getPreferredDateTimeFormat()));
	    			Loggers.SERVER.info(getLoggingName() + " :: Found and loaded default template as: " + state.getShortName());
	    			Loggers.SERVER.debug(getLoggingName() + " :: Template content is: " + props.getProperty(templatePropKey));
    			}
    		}
    		
	    	// If the default branch template is set, initialise the branch list for all states first.
    		templatePropKey = "template.default.branch";
    		if (props.containsKey(templatePropKey)){
    			this.defaultBranchTemplateText = new WebHookTemplateBranchText(props.getProperty(templatePropKey));
    			
    			for (BuildStateEnum state : BuildStateEnum.getNotifyStates()){
    				branchTemplateContent.put(state, WebHookTemplateContent.create(
    						state.getShortName(), 
    						props.getProperty(templatePropKey),
    						true,
    						this.getPreferredDateTimeFormat()));
    				Loggers.SERVER.info(getLoggingName() + " :: Found and loaded default branch template as: " + state.getShortName());
    				Loggers.SERVER.debug(getLoggingName() + " :: Template content is: " + props.getProperty(templatePropKey));
    			}
    		}
    		
    		// Now check if there is a generic template for all (non-branch) finished build states (successful, failed, fixed, broken)
    		BuildStateEnum[] finishedBuildStates = {BuildStateEnum.BUILD_SUCCESSFUL, BuildStateEnum.BUILD_FAILED, BuildStateEnum.BUILD_FIXED, BuildStateEnum.BUILD_BROKEN};
    		templatePropKey = "template.buildFinished";
    		if (props.containsKey(templatePropKey)){
	    		for (BuildStateEnum state : finishedBuildStates){
					templateContent.put(state, WebHookTemplateContent.create(
							state.getShortName(), 
							props.getProperty(templatePropKey),
							true,
							this.getPreferredDateTimeFormat()));
					Loggers.SERVER.info(getLoggingName() + " :: Found and loaded generic finished template as: " + state.getShortName());
					Loggers.SERVER.debug(getLoggingName() + " :: Template content is: " + props.getProperty(templatePropKey));
	    		}
    		}
    		
    		// Now check if there is a generic branch template for all finished build states (successful, failed, fixed, broken)
    		templatePropKey = "template.buildFinished.branch";
    		if (props.containsKey(templatePropKey)){
	    		for (BuildStateEnum state : finishedBuildStates){
					branchTemplateContent.put(state, WebHookTemplateContent.create(
							state.getShortName(), 
							props.getProperty(templatePropKey),
							true,
							this.getPreferredDateTimeFormat()));
					Loggers.SERVER.info(getLoggingName() + " :: Found and loaded generic finished branch template as: " + state.getShortName());
					Loggers.SERVER.debug(getLoggingName() + " :: Template content is: " + props.getProperty(templatePropKey));
	    		}
    		}
    		
    		// Then load the state specific templates (if any) for non-branch builds.
	    	for (BuildStateEnum state : BuildStateEnum.getNotifyStates()){
	    		templatePropKey = "template." + state.getShortName();
	    		if (props.containsKey(templatePropKey)){
	    			templateContent.put(state, WebHookTemplateContent.create(
	    																state.getShortName(), 
	    																props.getProperty(templatePropKey),
	    																true,
	    																this.getPreferredDateTimeFormat()));
	    			Loggers.SERVER.info(getLoggingName() + " :: Found and loaded template: " + templatePropKey);
	    			Loggers.SERVER.debug(getLoggingName() + " :: Template content is: " + props.getProperty(templatePropKey));
	    		}
	    	}
	    	
	    	// Then load the state specific templates (if any) for branch aware builds.
	    	for (BuildStateEnum state : BuildStateEnum.getNotifyStates()){
	    		templatePropKey = "template." + state.getShortName() + ".branch";
	    		if (props.containsKey(templatePropKey)){
	    			branchTemplateContent.put(state, WebHookTemplateContent.create(
	    					state.getShortName(), 
	    					props.getProperty(templatePropKey),
	    					true,
	    					this.getPreferredDateTimeFormat()));
	    			Loggers.SERVER.info(getLoggingName() + " :: Found and loaded template: " + templatePropKey);
	    			Loggers.SERVER.debug(getLoggingName() + " :: Template content is: " + props.getProperty(templatePropKey));
	    		}
	    	}
	    } 
	}

	@Override
	public WebHookTemplateContent getTemplateForState(BuildStateEnum buildState) {
		if (templateContent.containsKey(buildState)){
			return (templateContent.get(buildState)).copy(); 
		}
		return null;
	}

	@Override
	public WebHookTemplateContent getBranchTemplateForState(BuildStateEnum buildState) {
		if (branchTemplateContent.containsKey(buildState)){
			return (branchTemplateContent.get(buildState)).copy(); 
		}
		return null;
	}

	@Override
	public Set<BuildStateEnum> getSupportedBuildStates() {
		return templateContent.keySet();
	}

	@Override
	public Set<BuildStateEnum> getSupportedBranchBuildStates() {
		return branchTemplateContent.keySet();
	}

	@Override
	public WebHookTemplateEntity getAsEntity() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public WebHookTemplateConfig getAsConfig() {
		// TODO Auto-generated method stub
		return null;
	}

}
