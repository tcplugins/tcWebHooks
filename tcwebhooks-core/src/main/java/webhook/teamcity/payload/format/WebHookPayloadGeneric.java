/**
 * 
 */
package webhook.teamcity.payload.format;

import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.ResponsibilityInfo;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.tests.TestName;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.content.WebHookPayloadContent;

public abstract class WebHookPayloadGeneric implements WebHookPayload {
	
	WebHookPayloadManager myManager;
	
	public WebHookPayloadGeneric(WebHookPayloadManager manager){
		this.setPayloadManager(manager);
	}

	@Override
	public void setPayloadManager(WebHookPayloadManager manager){
		myManager = manager;
	}
	
	@Override
	public String beforeBuildFinish(SBuild runningBuild, SFinishedBuild previousBuild,
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
		return getStatusAsString(content, webHookTemplate);
	}

	/**
	 * buildChangedStatus has been deprecated because it alluded to build history status, which was incorrect. 
	 * It will no longer be called by the WebHookListener
	 */
	@Deprecated
	@Override
	public String buildChangedStatus(SBuild runningBuild, SFinishedBuild previousBuild,
			Status oldStatus, Status newStatus,
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		return "";
	}

	@Override
	public String buildFinished(SBuild runningBuild, SFinishedBuild previousBuild,
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.BUILD_FINISHED, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
		return getStatusAsString(content, webHookTemplate);
	}

	@Override
	public String buildInterrupted(SBuild runningBuild, SFinishedBuild previousBuild,
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.BUILD_INTERRUPTED, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
		return getStatusAsString(content, webHookTemplate);
	}

	@Override
	public String changesLoaded(SBuild runningBuild, SFinishedBuild previousBuild, 
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.CHANGES_LOADED, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
		return getStatusAsString(content, webHookTemplate);
	}
	
	@Override
	public String buildStarted(SBuild runningBuild, SFinishedBuild previousBuild, 
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.BUILD_STARTED, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
		return getStatusAsString(content, webHookTemplate);
	}

	/** Used by versions of TeamCity less than 7.0
	 * @
	 */
	@Override
	public String responsibleChanged(SBuildType buildType,
			ResponsibilityInfo responsibilityInfoOld,
			ResponsibilityInfo responsibilityInfoNew, boolean isUserAction,
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		
		WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), buildType, BuildStateEnum.RESPONSIBILITY_CHANGED, extraParameters, templates);
		String oldUser = "Nobody";
		String newUser = "Nobody";
		try {
			oldUser = responsibilityInfoOld.getResponsibleUser().getDescriptiveName();
		} catch (Exception e) {}
		try {
			newUser = responsibilityInfoNew.getResponsibleUser().getDescriptiveName();
		} catch (Exception e) {}
		content.setMessage("Build " + buildType.getFullName()
				+ " has changed responsibility from " 
				+ oldUser
				+ " to "
				+ newUser
				+ " with comment '" 
				+ responsibilityInfoNew.getComment().trim()
				+ "'"
			);
		content.setText(buildType.getFullName()
				+ " changed responsibility from " 
				+ oldUser
				+ " to "
				+ newUser
				+ " with comment '" 
				+ responsibilityInfoNew.getComment().trim()
				+ "'"
			);
		
		content.setComment(responsibilityInfoNew.getComment());
		return getStatusAsString(content, webHookTemplate);
	}

	/** Used by versions of TeamCity 7.0 and above
	 * @
	 */
	@Override
	public String responsibleChanged(SBuildType buildType,
			ResponsibilityEntry responsibilityEntryOld,
			ResponsibilityEntry responsibilityEntryNew,
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		
		WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), buildType, BuildStateEnum.RESPONSIBILITY_CHANGED, extraParameters, templates);
		String oldUser = "Nobody";
		String newUser = "Nobody";
		if (responsibilityEntryOld.getState() != ResponsibilityEntry.State.NONE) {
			  oldUser = responsibilityEntryOld.getResponsibleUser().getDescriptiveName();
		}
		if (responsibilityEntryNew.getState() != ResponsibilityEntry.State.NONE) {
			newUser = responsibilityEntryNew.getResponsibleUser().getDescriptiveName();
		}
		content.setMessage("Build " + buildType.getFullName()
				+ " has changed responsibility from " 
				+ oldUser
				+ " to "
				+ newUser
				+ " with comment '" 
				+ responsibilityEntryNew.getComment()
				+ "'"
			);
		content.setText(buildType.getFullName().trim()
				+ " changed responsibility from " 
				+ oldUser
				+ " to "
				+ newUser
				+ " with comment '" 
				+ responsibilityEntryNew.getComment().trim()
				+ "'"
			);
		content.setResponsibilityUserOld(oldUser);
		content.setResponsibilityUserNew(newUser);
		content.setComment(responsibilityEntryNew.getComment());
		return getStatusAsString(content, webHookTemplate);
	}
	
	@Override
	public String responsibleChanged(SProject project,
			TestNameResponsibilityEntry oldTestNameResponsibilityEntry,
			TestNameResponsibilityEntry newTestNameResponsibilityEntry,
			boolean isUserAction, SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String responsibleChanged(SProject project,
			Collection<TestName> testNames, ResponsibilityEntry entry,
			boolean isUserAction, SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected abstract String getStatusAsString(WebHookPayloadContent content, WebHookTemplateContent webHookTemplate);
	
	public Object serialiseObject(Object object) {
		return object;
	}

}
