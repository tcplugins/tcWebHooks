/**
 * 
 */
package webhook.teamcity.payload.format;

import java.util.Map;
import java.util.SortedMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.executor.WebHookResponsibilityHolder;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.variableresolver.VariableResolverFactory;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;

public abstract class WebHookPayloadGeneric implements WebHookPayload {
	
	WebHookPayloadManager myManager;
	WebHookVariableResolverManager webHookVariableResolverManager;
	VariableResolverFactory myVariableResolverFactory;
	
	public WebHookPayloadGeneric(WebHookPayloadManager manager, WebHookVariableResolverManager webHookVariableResolverManager){
		this.setPayloadManager(manager);
		this.webHookVariableResolverManager = webHookVariableResolverManager;
	}
	
	protected VariableResolverFactory getVariableResolverFactory() {
		if (myVariableResolverFactory == null) {
			this.myVariableResolverFactory = this.webHookVariableResolverManager.getVariableResolverFactory(getTemplateEngineType());
		}
		return this.myVariableResolverFactory;
	}

	@Override
	public void setPayloadManager(WebHookPayloadManager manager){
		myManager = manager;
	}

	@Override
	public String buildAddedToQueue(SQueuedBuild sQueuedBuild, SortedMap<String, String> extraParameters,
			Map<String, String> templates, WebHookTemplateContent webHookTemplate) {
		WebHookPayloadContent content = new WebHookPayloadContent(getVariableResolverFactory(), myManager.getServer(), sQueuedBuild, BuildStateEnum.BUILD_ADDED_TO_QUEUE, extraParameters, templates, null, null);
		return getStatusAsString(content, webHookTemplate);
	}

	@Override
	public String buildRemovedFromQueue(SQueuedBuild sQueuedBuild, SortedMap<String, String> extraParameters,
			Map<String, String> templates, WebHookTemplateContent webHookTemplate, String user, String comment) {
		WebHookPayloadContent content = new WebHookPayloadContent(getVariableResolverFactory(), myManager.getServer(), sQueuedBuild, BuildStateEnum.BUILD_REMOVED_FROM_QUEUE, extraParameters, templates, user, comment);
		return getStatusAsString(content, webHookTemplate);
	}
	
	@Override
	public String buildPinned(SBuild sBuild, SortedMap<String, String> extraParameters, Map<String, String> templates,
			WebHookTemplateContent webHookTemplate, String username, String comment) {
		WebHookPayloadContent content = new WebHookPayloadContent(getVariableResolverFactory(), myManager.getServer(), sBuild, BuildStateEnum.BUILD_PINNED, extraParameters, sBuild.getParametersProvider().getAll(), templates, username, comment);
		return getStatusAsString(content, webHookTemplate);
	}

	@Override
	public String buildUnpinned(SBuild sBuild, SortedMap<String, String> extraParameters, Map<String, String> templates,
			WebHookTemplateContent webHookTemplate, String username, String comment) {
		WebHookPayloadContent content = new WebHookPayloadContent(getVariableResolverFactory(), myManager.getServer(), sBuild, BuildStateEnum.BUILD_UNPINNED, extraParameters, sBuild.getParametersProvider().getAll(), templates, username, comment);
		return getStatusAsString(content, webHookTemplate);
	}
	
	@Override
	public String beforeBuildFinish(SBuild runningBuild, SFinishedBuild previousBuild,
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		WebHookPayloadContent content = new WebHookPayloadContent(getVariableResolverFactory(), myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
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
		WebHookPayloadContent content = new WebHookPayloadContent(getVariableResolverFactory(), myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.BUILD_FINISHED, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
		return getStatusAsString(content, webHookTemplate);
	}

	@Override
	public String buildInterrupted(SBuild runningBuild, SFinishedBuild previousBuild,
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		WebHookPayloadContent content = new WebHookPayloadContent(getVariableResolverFactory(), myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.BUILD_INTERRUPTED, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
		return getStatusAsString(content, webHookTemplate);
	}

	@Override
	public String changesLoaded(SBuild runningBuild, SFinishedBuild previousBuild, 
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		WebHookPayloadContent content = new WebHookPayloadContent(getVariableResolverFactory(), myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.CHANGES_LOADED, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
		return getStatusAsString(content, webHookTemplate);
	}
	
	@Override
	public String buildStarted(SBuild runningBuild, SFinishedBuild previousBuild, 
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		WebHookPayloadContent content = new WebHookPayloadContent(getVariableResolverFactory(), myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.BUILD_STARTED, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
		return getStatusAsString(content, webHookTemplate);
	}

	@Override
	public String responsibilityChanged(WebHookResponsibilityHolder responsibilityHolder,
			SortedMap<String, String> extraParameters, Map<String, String> templates,
			WebHookTemplateContent webHookTemplate) {
		WebHookPayloadContent content = new WebHookPayloadContent(getVariableResolverFactory(), myManager.getServer(), responsibilityHolder, BuildStateEnum.RESPONSIBILITY_CHANGED, extraParameters, templates);
		return getStatusAsString(content, webHookTemplate);

	}
	protected abstract String getStatusAsString(WebHookPayloadContent content, WebHookTemplateContent webHookTemplate);
	
	public Object serialiseObject(Object object) {
		return object;
	}

}
