package webhook.teamcity.payload.template;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import javax.xml.bind.JAXBException;

import com.intellij.openapi.diagnostic.Logger;
import webhook.Constants;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.DeferrableService;
import webhook.teamcity.DeferrableServiceManager;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.config.builder.WebHookTemplateConfigBuilder;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;

/**
 * Abstract class to handle loading a template from an XML file.
 * This is intended to be used by Bundled templates that want
 * to store their template configuration as an XML Entity
 * in the same format as is represented by the 
 * <code>webhook-templates.xml</code> file. 
 * <p>
 * Note: This is NOT for templates that are instantiated from
 * the <code>webhook-templates.xml</code> file. 
 */
public abstract class AbstractXmlBasedWebHookTemplate implements WebHookPayloadTemplate, DeferrableService {
	private static final Logger LOG = Logger.getInstance(AbstractXmlBasedWebHookTemplate.class.getName());

	protected WebHookPayloadManager payloadManager;
	protected WebHookTemplateJaxHelper webHookTemplateJaxHelper;
	private WebHookTemplateFromXml template;
	protected WebHookTemplateManager templateManager;
	private Integer rank;
	private ProjectIdResolver projectIdResolver;
	private DeferrableServiceManager deferrableServiceManager;

	protected AbstractXmlBasedWebHookTemplate(
			WebHookTemplateManager templateManager, WebHookPayloadManager payloadManager, 
			WebHookTemplateJaxHelper webHookTemplateJaxHelper, ProjectIdResolver projectIdResolver, 
			DeferrableServiceManager deferrableServiceManager) {
		this.templateManager = templateManager;
		this.payloadManager = payloadManager;
		this.webHookTemplateJaxHelper = webHookTemplateJaxHelper;
		this.projectIdResolver = projectIdResolver;
		this.deferrableServiceManager = deferrableServiceManager;
	}
	
	@Override
	public void requestDeferredRegistration() {
		this.deferrableServiceManager.registerService(this);
	}

	/**
	 * Registers the first template from the XML file with the template manager.
	 * The file format is the same as the webhook-templates.xml file.
	 */
	@Override
	public void register() {
		
		template = (WebHookTemplateFromXml) WebHookTemplateFromXml.build(loadTemplateFromXmlFile(), payloadManager);
		
		// If rank is set by spring initialisation then use that value
		// rather than the one in the XML file.
		
		if (this.rank != null){
			template.setRank(this.rank);
		} else {
			this.setRank(template.getRank());
		}
		
		template.setProjectId(this.projectIdResolver.getInternalProjectId(Constants.ROOT_PROJECT_ID));
		
		if (!template.templateContent.isEmpty() && !template.branchTemplateContent.isEmpty()){
			this.templateManager.registerTemplateFormatFromSpring(template);
		} else {
			if (template.templateContent.isEmpty()){
				LOG.error(getLoggingName() + " :: Failed to register template " + getTemplateId() + ". No regular template configurations were found.");
			}
			if (template.branchTemplateContent.isEmpty()){
				LOG.error(getLoggingName() + " :: Failed to register template " + getTemplateId() + ". No branch template configurations were found.");
			}
		}
	}
	
	@Override
	public void unregister() {
		// Nothing required for shutdown
	}

	private URL findXmlFileUrlInVariousClassloaders(String xmlFile) {
		final ClassLoader[] classLoaders = {AbstractXmlBasedWebHookTemplate.class.getClassLoader(), ClassLoader.getSystemClassLoader()}; 
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
	 * Loads the template from an XML file. This means we can 
	 * use the <webhook-template> entity from the file format rather than declaring the 
	 * template strings in this class which would then require doing silly 
	 * string escaping in java.<br>
	 * This file must be in the format returned by the .../rawConfig REST API method. 
	 * Calls getXmlFileName() which must be implemented in subclass.
	 */
	private WebHookTemplateEntity loadTemplateFromXmlFile() {
		WebHookTemplateEntity webhookEntity = null;
		URL url = findXmlFileUrlInVariousClassloaders(getXmlFileName());
	    if (url != null) {
	        try {
	            InputStream in = url.openStream();
	            webhookEntity = webHookTemplateJaxHelper.readTemplate(in);
	            webhookEntity.fixTemplateIds();
	        } catch (IOException | JAXBException e) {
	        	LOG.error(getLoggingName() + " :: An Error occurred trying to load the template properties file: " + getXmlFileName() + ".");
	        	LOG.debug(e);
	        	
	        } finally {
	           // close opened resources
	        }
	    } else {
	    	LOG.error(getLoggingName() + " :: An Error occurred trying to load the template properties file: " + getXmlFileName() + ". The file was not found in the classpath.");
	    }
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
	public String getTemplateId() {
		return template.getTemplateId();
	}
	
	@Override
	public String getProjectId() {
		return Constants.ROOT_PROJECT_ID;
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