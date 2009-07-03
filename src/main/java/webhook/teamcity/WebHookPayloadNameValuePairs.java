/**
 * 
 */
package webhook.teamcity;

import java.util.SortedMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.ResponsibilityInfo;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;

import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jetbrains.annotations.NotNull;

import webhook.WebHookPayload;

public class WebHookPayloadNameValuePairs implements WebHookPayload {

	public StringRequestEntity getRequestStream() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFormatDescription() {
		return "Name Value Pairs";
	}

	public String getFormatShortName() {
		return "nvpairs";
	}

    public void buildStarted(SRunningBuild sRunningBuild){
    	processBuildEvent(sRunningBuild, "buildStarted", "Started", BuildState.BUILD_STARTED);
    }	
	
    public void buildFinished(SRunningBuild sRunningBuild){
    	processBuildEvent(sRunningBuild, "buildFinished", "Finished", BuildState.BUILD_FINISHED);
    }    

    public void buildInterrupted(SRunningBuild sRunningBuild) {
    	processBuildEvent(sRunningBuild, "buildInterrupted", "been Interrupted", BuildState.BUILD_INTERRUPTED);
    }      

    public void beforeBuildFinish(SRunningBuild sRunningBuild) {
    	processBuildEvent(sRunningBuild, "buildNearlyFinished", "nearly Finished", BuildState.BEFORE_BUILD_FINISHED);
	}
    
    public void buildChangedStatus(SRunningBuild sRunningBuild, Status oldStatus, Status newStatus) {
    	
    }
	
    public void responsibleChanged(@NotNull SBuildType sBuildType, 
    		@NotNull ResponsibilityInfo responsibilityInfoOld, @NotNull ResponsibilityInfo responsibilityInfoNew, boolean b) {
    	
    }

	public String beforeBuildFinish(SRunningBuild runningBuild,
			SortedMap<String, String> extraParameters) {
		// TODO Auto-generated method stub
		return null;
	}

	public String buildChangedStatus(SRunningBuild runningBuild,
			Status oldStatus, Status newStatus,
			SortedMap<String, String> extraParameters) {
		// TODO Auto-generated method stub
		return null;
	}

	public String buildFinished(SRunningBuild runningBuild,
			SortedMap<String, String> extraParameters) {
		// TODO Auto-generated method stub
		return null;
	}

	public String buildInterrupted(SRunningBuild runningBuild,
			SortedMap<String, String> extraParameters) {
		// TODO Auto-generated method stub
		return null;
	}

	public String buildStarted(SRunningBuild runningBuild,
			SortedMap<String, String> extraParameters) {
		// TODO Auto-generated method stub
		return null;
	}

	public String responsibleChanged(SBuildType buildType,
			ResponsibilityInfo responsibilityInfoOld,
			ResponsibilityInfo responsibilityInfoNew, boolean isUserAction,
			SortedMap<String, String> extraParameters) {
		// TODO Auto-generated method stub
		return null;
	}

}
