package webhook.teamcity.payload.template;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.payload.WebHookTemplate;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.format.WebHookPayloadTailoredJson;

public class SlackComWebHookTemplate extends AbstractWebHookTemplate implements WebHookTemplate {
	
	Map<BuildStateEnum,WebHookTemplateContent> templateContent = new HashMap<BuildStateEnum, WebHookTemplateContent>();
	String CONF_PROPERTIES = "webhook/teamcity/payload/template/SlackComWebHookTemplate.properties";
	
	public SlackComWebHookTemplate(WebHookTemplateManager manager) {
		this.manager = manager;
	}

	@Override
	public void register() {
		templateContent.clear();
		loadTemplatesFromPropertiesFile();
		if (!templateContent.isEmpty()){
			super.register(this);
		} else {
			Loggers.SERVER.error("SlackComWebHookTemplate :: Failed to register template. No template configurations were found.");
		}
		
	}
	
	/**
	 * Load the template from a properties file, rather than doing silly string escaping in java.
	 */
	private void loadTemplatesFromPropertiesFile(){
		Properties props = null;
		ClassLoader cl = ClassLoader.getSystemClassLoader();
	    if (cl != null) {
	        URL url = cl.getResource(CONF_PROPERTIES);
	        if (url == null) {
	            url = cl.getResource("/" + CONF_PROPERTIES);
	        }
	        if (url != null) {
	            try {
	                InputStream in = url.openStream();
	                props = new Properties();
	                props.load(in);
	            } catch (IOException e) {
	            	Loggers.SERVER.error("SlackComWebHookTemplate :: An Error occurred trying to load the template properties file: " + CONF_PROPERTIES + ".");
	            	Loggers.SERVER.debug(e);
	            	
	            } finally {
	               // close opened resources
	            }
	        } else {
            	Loggers.SERVER.error("SlackComWebHookTemplate :: An Error occurred trying to load the template properties file: " + CONF_PROPERTIES + ". The file was not found in the classpath.");
	        }
	    }
	    if (props != null){
	    	String templatePropKey = "";
	    	for (BuildStateEnum state : BuildStateEnum.values()){
	    		templatePropKey = "template." + state.getShortName();
	    		if (props.containsKey(templatePropKey)){
	    			templateContent.put(state, WebHookTemplateContent.create(
	    																state.getShortName(), 
	    																props.getProperty(templatePropKey),
	    																true));
	    			Loggers.SERVER.info("SlackComWebHookTemplate :: Found and loaded template: " + templatePropKey);
	    			Loggers.SERVER.debug("SlackComWebHookTemplate :: Template content is: " + props.getProperty(templatePropKey));
	    		}
	    	}
	    } 
	}
	
	@Override
	public String getTemplateDescription() {
		return "Slack.com JSON templates";
	}

	@Override
	public String getTemplateToolTipText() {
		return "Supports the slack.com JSON webhooks endpoint";
	}

	@Override
	public String getTemplateShortName() {
		return "slack.com";
	}
	
	@Override
	public boolean supportsPayloadFormat(String payloadFormat) {
		return payloadFormat.equals(WebHookPayloadTailoredJson.FORMAT_SHORT_NAME);
	}
	
	@Override
	public WebHookTemplateContent getTemplateForState(BuildStateEnum buildState) {
		if (templateContent.containsKey(buildState)){
			return (templateContent.get(buildState)).copy(); 
		}
		return null;
	}

}
