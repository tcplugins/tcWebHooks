package webhook;

import java.util.SortedMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.ResponsibilityInfo;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;

import org.jetbrains.annotations.NotNull;

public interface WebHookPayload {
	
	/**
	 * Returns a Description of the format type. This is used for display on the config page
	 * when selecting the WebHook format. The output will be used with <c:out in the JSP so 
	 * any HTML tags will get escaped into &amp;lt; tag &amp;gt; etc.
	 *  
	 * @return	Text for display on the WebHook config page.
	 */
	String getFormatDescription();
	
	/**
	 * Returns a short name for the format type. This string is used in the HTML form when 
	 * referring to the format, and used in the plugin-settings.xml file for specifying the
	 * format of the payload.
	 * 
	 * @return	Text for referring to the format type. eg, JSON.
	 */
	String getFormatShortName();
	
	/**
	 * Extracts the required information from the sRunningBuild and extraParameters configured in the webhook
	 * or build parameters and returns a String of the WebHook payload.
	 *  
	 * @param sRunningBuild
	 * @param extraParameters
	 * @return Formatted payload for the WebHook to send for the buildStarted event.
	 */
    String buildStarted(SRunningBuild sRunningBuild, SortedMap<String,String> extraParameters);

    /**
	 * Extracts the required information from the sRunningBuild and extraParameters configured in the webhook
	 * or build parameters and returns a String of the WebHook payload.
	 *  
	 * @param sRunningBuild
	 * @param extraParameters
	 * @return Formatted payload for the WebHook to send for the buildFinished event.
	 */
    String buildFinished(SRunningBuild sRunningBuild, SortedMap<String,String> extraParameters);

    /**
	 * Extracts the required information from the sRunningBuild and extraParameters configured in the webhook
	 * or build parameters and returns a String of the WebHook payload.
	 *  
	 * @param sRunningBuild
	 * @param extraParameters
	 * @return Formatted payload for the WebHook to send for the buildInterrupted event.
	 */
    String buildInterrupted(SRunningBuild sRunningBuild, SortedMap<String,String> extraParameters);

    /**
	 * Extracts the required information from the sRunningBuild and extraParameters configured in the webhook
	 * or build parameters and returns a String of the WebHook payload.
	 *  
	 * @param sRunningBuild
	 * @param extraParameters
	 * @return Formatted payload for the WebHook to send for the beforeBuildFinish event.
	 */
    String beforeBuildFinish(SRunningBuild sRunningBuild, SortedMap<String,String> extraParameters);
    
    /**
	 * Extracts the required information from the sRunningBuild, oldStatus, newStatus and extraParameters 
	 * configured in the webhook or build parameters and returns a String of the WebHook payload.
	 * 
	 * @param sRunningBuild
     * @param oldStatus
     * @param newStatus
     * @param extraParameters
     * @return Formatted payload for the WebHook to send for the buildChangedStatus event.
     */
    String buildChangedStatus(SRunningBuild sRunningBuild, 
    		Status oldStatus, 
    		Status newStatus, 
    		SortedMap<String,String> extraParameters);
	

    /**
     * 
     * @param sBuildType
     * @param responsibilityInfoOld
     * @param responsibilityInfoNew
     * @param isUserAction
     * @param extraParameters
     * @return Formatted payload for the WebHook to send for the responsibleChanged event.
     */
    String responsibleChanged(@NotNull SBuildType sBuildType, 
    		@NotNull ResponsibilityInfo responsibilityInfoOld, 
    		@NotNull ResponsibilityInfo responsibilityInfoNew, 
    		boolean isUserAction, SortedMap<String,String> extraParameters);
}
