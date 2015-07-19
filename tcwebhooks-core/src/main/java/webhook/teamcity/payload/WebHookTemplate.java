package webhook.teamcity.payload;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.settings.CustomMessageTemplate;

public interface WebHookTemplate {

	/** 
	 * Sets the TemplateManager so that register() can register this template with that manager.
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
	String getTemplateToolTipText();
	
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
     * @param rank (higher numbers are ranked first)
     */
    void setRank(Integer rank);	
    
    /**
     * Get the message teamplate
     * @param buildState
     * @return The {@link CustomMessageTemplate} relevant to this build state.
     * It is up to the implementation to return a sensible template.
     * If the template is the same for all messages, this could simply return the
     * same CustomMessageTemplate everytime.   
     */
	WebHookTemplateContent getTemplateForState(BuildStateEnum buildState);
	
	/**
	 * 
	 */

}
