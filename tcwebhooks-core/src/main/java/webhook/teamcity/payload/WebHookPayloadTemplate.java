package webhook.teamcity.payload;

import java.util.Set;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;

public interface WebHookPayloadTemplate {

	/** 
	 * Sets the TemplateManager so that register() can register this template with that webHookTemplateManager.
	 * 
	 * @param webhookTemplateManager
	 */
	void setTemplateManager (WebHookTemplateManager webhookTemplateManager);
	
	/**
	 * Registers with the TemplateManager
	 * 
	 */
	void register();
	
	/**
	 * Returns a Description of the template type. This is used for display on the config page
	 * when selecting the WebHook template. The output will be used with <c:out in the JSP so 
	 * any HTML tags will get escaped into &amp;lt; tag &amp;gt; etc.
	 *  
	 * @return	Text for display on the WebHook config page.
	 */
	String getTemplateDescription();

	/**
	 * Returns a sightly longer Description of the template type. This is used in the "title" 
	 * tags for providing a "tool tip" when a user hovers over the template description. 
	 * The output will be used with <c:out in the JSP so 
	 * any HTML tags will get escaped into &amp;lt; tag &amp;gt; etc.
	 *  
	 * @return	Tool Tip Text for display on the WebHook config page.
	 */
	String getTemplateToolTip();
	
	/**
	 * Returns a short name for the template type. This string is used in the HTML form when 
	 * referring to the template, and used in the plugin-settings.xml file for specifying the
	 * template of the payload.
	 * 
	 * @return	Text for referring to the template type. eg, JSON.
	 */
	String getTemplateShortName();
	
	/**
	 * Asks if this template can provide a set of templates for this format.
	 * 
	 * @return Whether this template is designed to return templates for this format;
	 */
    boolean supportsPayloadFormat(String payloadFormat);
    
    /**
     * Gets in Integer for order. The Higher the number, the more likely 
     * it is to appear higher in the list of options.
     * The highest number will be the default when showing the list of webhooks
     * in the web UI.
     *  
     * Suggestion : When registering your plugin with Spring, you could set with a bean property
     * in the spring XML file. That way it can be edited by the end user if required.
     * 
     * @return rank (higher numbers are ranked first)
     */
    int getRank();
    
    /**
     * Set in Integer for order. The Higher the number, the more likely 
     * it is to appear higher in the list of options.
     * The highest number will be the default when showing the list of webhooks
     * in the web UI.
     *  
     * Suggestion : When registering your plugin with Spring, you could set with a bean property
     * in the spring XML file. That way it can be edited by the end user if required.
     * 
     * @param rank (higher numbers are ranked first)
     */
    void setRank(Integer rank);	
    
    /**
     * Get the message template for the specified BuildState. This method is called 
     * for builds that are on VCS that are <b>NOT</b> branch aware.
     * @param buildState
     * @return The {@link WebHookTemplateContent} relevant to this build state.
     * It is up to the implementation to return a sensible template.
     * If the template is the same for all messages, this could simply return the
     * same WebHookTemplateContent every time.   
     */
	WebHookTemplateContent getTemplateForState(BuildStateEnum buildState);
	
	/**
     * Get the message template for the specified BuildState. This method is called 
     * for builds that are on VCS that <b>ARE</b> branch aware (eg, Git, Mecurial).
     * @param buildState
     * @return The {@link WebHookTemplateContent} relevant to this build state.
     * It is up to the implementation to return a sensible template.
     * If the template is the same for all messages, this could simply return the
     * same WebHookTemplateContent every time. 
	 */
	WebHookTemplateContent getBranchTemplateForState(BuildStateEnum buildState);
	
	/**
	 * Get the list of BuildStates for which we have {@link WebHookTemplateContent} content.<br>
	 * This method expected to be called to resolve templates for builds that are running
	 * from a VCS is that not branch aware.<br> 
	 * A "branch aware VCS" is one which TeamCity has knowledge about regarding branches. Eg. Git and Mecurial. 
	 * 
	 * @return A set of templates that don't contain branch information.
	 * 
	 */
	Set<BuildStateEnum> getSupportedBuildStates();
	
	/**
	 * Get the list of BuildStates for which we have {@link WebHookTemplateContent} content.<br>
	 * This method expected to be called to resolve templates for builds that are running
	 * from a VCS is that is branch aware.<br> 
	 * A "branch aware VCS" is one which TeamCity has knowledge about regarding branches. Eg. Git and Mecurial.
	 * 
	 * @return A set of templates that contain branch information.
	 * 
	 */
	Set<BuildStateEnum> getSupportedBranchBuildStates();
	
	/**
	 * Get the preferred date/time format for any date object that needs to be presented in the payload content. 
	 * 
	 * @return A string that can be passed into SimpleDateFormat<br>For example: "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" would format a date like as "2015-09-20T15:26:35.641+00:00"
	 * 
	 */
	String getPreferredDateTimeFormat();
	
	WebHookTemplateEntity getAsEntity();
	
	WebHookTemplateConfig getAsConfig();
}
