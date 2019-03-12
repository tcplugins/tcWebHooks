package webhook.teamcity.payload;

import java.util.Map;
import java.util.SortedMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import webhook.teamcity.executor.WebHookResponsibilityHolder;
import webhook.teamcity.payload.template.render.WebHookStringRenderer;

public interface WebHookPayload extends WebHookContentObjectSerialiser {
	
	public static final String BUILD_STATUS_FAILURE   = "failure";
	public static final String BUILD_STATUS_SUCCESS   = "success";
	public static final String BUILD_STATUS_RUNNING   = "running";
	public static final String BUILD_STATUS_NO_CHANGE = "unchanged";
	public static final String BUILD_STATUS_FIXED     = "fixed";
	public static final String BUILD_STATUS_BROKEN    = "broken";
	public static final String BUILD_STATUS_UNKNOWN   = "unknown";
	
	/** 
	 * Sets the PayloadManger so that register() can register this payload with that webHookTemplateManager.
	 * 
	 * @param webhookPayloadManager
	 */
	void setPayloadManager (WebHookPayloadManager webhookPayloadManager);
	
	/**
	 * Registers with the PayloadManager
	 * 
	 */
	void register();
	
	/**
	 * Returns a Description of the format type. This is used for display on the config page
	 * when selecting the WebHook format. The output will be used with <code>< c:out ></code> in the JSP so 
	 * any HTML tags will get escaped into &amp;lt; tag &amp;gt; etc.
	 *  
	 * @return	Text for display on the WebHook config page.
	 */
	String getFormatDescription();

	/**
	 * Returns a sightly longer Description of the format type. This is used in the "title" 
	 * tags for providing a "tool tip" when a user hovers over the format description. 
	 * The output will be used with <c:out in the JSP so 
	 * any HTML tags will get escaped into &amp;lt; tag &amp;gt; etc.
	 *  
	 * @return	Tool Tip Text for display on the WebHook config page.
	 */
	String getFormatToolTipText();
	
	/**
	 * Returns a short name for the format type. This string is used in the HTML form when 
	 * referring to the format, and used in the plugin-settings.xml file for specifying the
	 * format of the payload.
	 * 
	 * @return	Text for referring to the format type. eg, JSON.
	 */
	String getFormatShortName();

	/**
	 * Extracts the required information from the sQueuedBuild and extraParameters configured in the webhook
	 * or build parameters and returns a String of the WebHook payload.
	 *  
	 * @param sQueuedBuild
	 * @param extraParameters
	 * @return Formatted payload for the WebHook to send for the buildAddedToQueue event.
	 */
    String buildAddedToQueue(SQueuedBuild sQueuedBuild, SortedMap<String,String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate);
    
    /**
     * Extracts the required information from the sQueuedBuild and extraParameters configured in the webhook
     * or build parameters and returns a String of the WebHook payload.
     *  
     * @param sQueuedBuild
     * @param extraParameters
     * @return Formatted payload for the WebHook to send for the buildAddedToQueue event.
     */
    String buildRemovedFromQueue(SQueuedBuild sQueuedBuild, SortedMap<String,String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate, String user, String comment);
    
    /**
     * Extracts the required information from the sBuild and extraParameters configured in the webhook
     * or build parameters and returns a String of the WebHook payload.
     * 
     * @param sBuild
     * @param extraParameters
     * @param templates
     * @param webHookTemplate
     * @param username
     * @param comment
     * @return Formatted payload for the WebHook to send for the buildPinned event.
     */
	String buildPinned(SBuild sBuild, SortedMap<String, String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate, String username, String comment);

	/**
	 * Extracts the required information from the sBuild and extraParameters configured in the webhook
	 * or build parameters and returns a String of the WebHook payload.
	 * 
	 * @param sBuild
	 * @param extraParameters
	 * @param templates
	 * @param webHookTemplate
	 * @param username
	 * @param comment
	 * @return Formatted payload for the WebHook to send for the buildUnpinned event.
	 */
	String buildUnpinned(SBuild sBuild, SortedMap<String, String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate, String username, String comment);
	
	
    /**
     * Extracts the required information from the sRunningBuild and extraParameters configured in the webhook
     * or build parameters and returns a String of the WebHook payload.
     *  
     * @param sRunningBuild
     * @param extraParameters
     * @return Formatted payload for the WebHook to send for the buildStarted event.
     */
    String buildStarted(SBuild sRunningBuild, SFinishedBuild previousBuild, SortedMap<String,String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate);
    
    /**
     * Extracts the required information from the sRunningBuild and extraParameters configured in the webhook
     * or build parameters and returns a String of the WebHook payload.
     *  
     * @param sRunningBuild
     * @param extraParameters
     * @return Formatted payload for the WebHook to send for the changesLoaded event.
     */
    String changesLoaded(SBuild sRunningBuild, SFinishedBuild previousBuild, SortedMap<String,String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate);

    /**
	 * Extracts the required information from the sRunningBuild and extraParameters configured in the webhook
	 * or build parameters and returns a String of the WebHook payload.
	 *  
	 * @param sRunningBuild
	 * @param extraParameters
	 * @return Formatted payload for the WebHook to send for the buildFinished event.
	 */
    String buildFinished(SBuild sRunningBuild, SFinishedBuild previousBuild, SortedMap<String,String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate);

    /**
	 * Extracts the required information from the sRunningBuild and extraParameters configured in the webhook
	 * or build parameters and returns a String of the WebHook payload.
	 *  
	 * @param sRunningBuild
	 * @param extraParameters
	 * @return Formatted payload for the WebHook to send for the buildInterrupted event.
	 */
    String buildInterrupted(SBuild sRunningBuild, SFinishedBuild previousBuild, SortedMap<String,String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate);

    /**
	 * Extracts the required information from the sRunningBuild and extraParameters configured in the webhook
	 * or build parameters and returns a String of the WebHook payload.
	 *  
	 * @param sRunningBuild
	 * @param extraParameters
	 * @return Formatted payload for the WebHook to send for the beforeBuildFinish event.
	 */
    String beforeBuildFinish(SBuild sRunningBuild, SFinishedBuild previousBuild, SortedMap<String,String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate);
    
	/**
	 * buildChangedStatus has been deprecated because it alluded to build history status, which was incorrect.
	 * It will no longer be called by the WebHookListener
	 */
	@Deprecated
    String buildChangedStatus(SBuild sRunningBuild, SFinishedBuild previousBuild, 
    		Status oldStatus, 
    		Status newStatus, 
    		SortedMap<String,String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate);
	
	/** 
	 * ResponsibilityChanged handler for all responsibility events.<p>
	 * @since tcWebHooks 1.2.0
	 * 
	 * Builds and assembles the webhook payload for all Responsibility Changed events.
	 * Returns the webhook body ready to send, which must be already formatted in the 
	 * correct format to match the "Content-Type" and "Character Set".<p>
	 * 
	 * This method supersedes the previous responsibility methods, and must be capable
	 * of handling the various payloads (which get assembled into a 
	 * {@link WebHookResponsibilityHolder} instance.
	 * 
	 * @param responsibilityHolder
	 * @param mergeParameters
	 * @param enabledTemplates
	 * @param templateForThisBuild
	 * @return the Payload as a string representation.
	 */
	String responsibilityChanged(WebHookResponsibilityHolder responsibilityHolder,
			SortedMap<String, String> mergeParameters, Map<String, String> enabledTemplates,
			WebHookTemplateContent templateForThisBuild); 

	/**
	 * Gets the content type of the format.
	 * Should return a string like "application/json"
	 * 
	 * @return contentType;
	 */
    String getContentType();
    
    /**
     * Gets in Integer for order. The Higher the number, the more likely 
     * it is to appear higher in the list of options.
     * The highest number will be the default when showing the list of webhooks
     * in the web UI.
     *  
     * Suggestion : When registering your plugin with Spring, you could set with a bean property
     * in the spring XML file. That way it can be edited by the end user if required.
     * 
     * @return rank (lower numbers are ranked first)
     */
    Integer getRank();
    
    /**
     * Set in Integer for order. The Higher the number, the more likely 
     * it is to appear higher in the list of options.
     * The highest number will be the default when showing the list of webhooks
     * in the web UI.
     *  
     * Suggestion : When registering your plugin with Spring, you could set with a bean property
     * in the spring XML file. That way it can be edited by the end user if required.
     * 
     * @param rank (lower numbers are ranked first)
     */
    void setRank(Integer rank);

    /**
     * Get the character set that the payload is in. This is probably UTF-8, but is up to the 
     * implementation. 
     * @return charset (string like "UTF-8")
     */
	String getCharset();

	/**
	 * Get the {@link WebHookStringRenderer}, which is responsible for rendering the 
	 * payload as an HTML string. Must convert the output to properly escape HTML and
	 * should do pretty printing. 
	 * 
	 * @return an instance of a {@link WebHookStringRenderer}
	 */
	public abstract WebHookStringRenderer getWebHookStringRenderer();
	
	/**
	 * Specifies which type of template engine this payload uses to convert 
	 * the templated payload into populated output. 
	 * 
	 * @return One of the {@link PayloadTemplateEngineType} enums
	 */
	public abstract PayloadTemplateEngineType getTemplateEngineType();

}
