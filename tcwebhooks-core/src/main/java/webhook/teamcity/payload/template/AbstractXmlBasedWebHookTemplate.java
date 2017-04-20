package webhook.teamcity.payload.template;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import javax.xml.bind.JAXBException;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.config.builder.WebHookTemplateConfigBuilder;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;

public abstract class AbstractXmlBasedWebHookTemplate implements WebHookPayloadTemplate {

	protected WebHookPayloadManager payloadManager;
	protected WebHookTemplateJaxHelper webHookTemplateJaxHelper;
	private WebHookTemplateFromXml template;
	protected WebHookTemplateManager templateManager;
	private Integer rank;

	public AbstractXmlBasedWebHookTemplate(WebHookTemplateManager templateManager, WebHookPayloadManager payloadManager, WebHookTemplateJaxHelper webHookTemplateJaxHelper) {
		this.templateManager = templateManager;
		this.payloadManager = payloadManager;
		this.webHookTemplateJaxHelper = webHookTemplateJaxHelper;
	}

	/**
	 * Registers the first template from the XML file with the template manager.
	 * The file format is the same as the webhook-templates.xml file.
	 */
	@Override
	public void register() {
		
		template = (WebHookTemplateFromXml) WebHookTemplateFromXml.build(loadFirstTemplateFromXmlFile(), payloadManager);
		
		// Is rank is set by spring initialisation then use that value
		// rather than the one in the XML file.
		
		if (this.rank != null){
			template.setRank(this.rank);
		} else {
			this.setRank(template.getRank());
		}
		
		
		
		if (!template.templateContent.isEmpty() && !template.branchTemplateContent.isEmpty()){
			this.templateManager.registerTemplateFormatFromSpring(template);
		} else {
			if (template.templateContent.isEmpty()){
				Loggers.SERVER.error(getLoggingName() + " :: Failed to register template " + getTemplateShortName() + ". No regular template configurations were found.");
			}
			if (template.branchTemplateContent.isEmpty()){
				Loggers.SERVER.error(getLoggingName() + " :: Failed to register template " + getTemplateShortName() + ". No branch template configurations were found.");
			}
		}
	}

	private URL findXmlFileUrlInVariousClassloaders(String xmlFile) {
		final ClassLoader[] classLoaders = {ElasticSearchXmlWebHookTemplate.class.getClassLoader(), ClassLoader.getSystemClassLoader()}; 
		URL url = null;
		for (ClassLoader cl : classLoaders){
			if (cl != null){
				url = cl.getResource(xmlFile);
		        if (url != null){
		        	break;
		        }
			}
		}
		return url;
	}

	/**
	 * Loads only the first template from an XML file. This means we can 
	 * use the webhook-templates.xml file format rather than declaring the 
	 * template strings in this class which would then require doing silly 
	 * string escaping in java.<br>
	 * Calls getXmlFileName() which must be implemented in subclass.
	 */
	private WebHookTemplateEntity loadFirstTemplateFromXmlFile() {
		WebHookTemplateEntity webhookEntity = null;
		URL url = findXmlFileUrlInVariousClassloaders(getXmlFileName());
	    if (url != null) {
	        try {
	            InputStream in = url.openStream();
	            webhookEntity = webHookTemplateJaxHelper.read(in).getWebHookTemplateList().get(0);
	        } catch (IOException | JAXBException e) {
	        	Loggers.SERVER.error(getLoggingName() + " :: An Error occurred trying to load the template properties file: " + getXmlFileName() + ".");
	        	Loggers.SERVER.debug(e);
	        	
	        } finally {
	           // close opened resources
	        }
	    } else {
	    	Loggers.SERVER.error(getLoggingName() + " :: An Error occurred trying to load the template properties file: " + getXmlFileName() + ". The file was not found in the classpath.");
	    }
	    webhookEntity.fixTemplateIds();
	    return webhookEntity;
	}

	public abstract  String getXmlFileName();

	public abstract String getLoggingName();

	@Override
	public void setTemplateManager(WebHookTemplateManager webhookTemplateManager) {
		this.templateManager = webhookTemplateManager;
		
	}

	@Override
	public String getTemplateDescription() {
		return template.getTemplateDescription();
	}

	@Override
	public String getTemplateToolTip() {
		return template.getTemplateToolTip();
	}

	@Override
	public String getTemplateShortName() {
		return template.getTemplateShortName();
	}

	@Override
	public boolean supportsPayloadFormat(String payloadFormat) {
		return template.supportsPayloadFormat(payloadFormat);
	}

	@Override
	public int getRank() {
		return rank;
	}

	@Override
	public void setRank(Integer rank) {
		this.rank = rank;
		if (template != null){
			template.setRank(rank);
		}
		
	}

	@Override
	public WebHookTemplateContent getTemplateForState(BuildStateEnum buildState) {
		return template.getTemplateForState(buildState);
	}

	@Override
	public WebHookTemplateContent getBranchTemplateForState(BuildStateEnum buildState) {
		return template.getBranchTemplateForState(buildState);
	}

	@Override
	public Set<BuildStateEnum> getSupportedBuildStates() {
		return template.getSupportedBranchBuildStates();
	}

	@Override
	public Set<BuildStateEnum> getSupportedBranchBuildStates() {
		return template.getSupportedBranchBuildStates();
	}

	@Override
	public String getPreferredDateTimeFormat() {
		return template.getPreferredDateTimeFormat();
	}

	@Override
	public WebHookTemplateEntity getAsEntity() {
		return template.getAsEntity();
	}
	
	@Override
	public WebHookTemplateConfig getAsConfig() {
		return WebHookTemplateConfigBuilder.buildConfig(template.getAsEntity());
	}

}