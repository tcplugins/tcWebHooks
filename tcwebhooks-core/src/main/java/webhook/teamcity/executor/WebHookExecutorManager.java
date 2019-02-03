package webhook.teamcity.executor;

import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookMainSettings;

public class WebHookExecutorManager implements WebHookExecutor {
	
	boolean useThreadedExecutor = true;
	private WebHookMainSettings myWebHookMainSettings;
	private WebHookSerialExecutor myWebHookSerialExecutor;
	private WebHookThreadingExecutor myWebHookThreadingExecutor;
	
	public WebHookExecutorManager(
			WebHookMainSettings webHookMainSettings,
			WebHookSerialExecutor webHookSerialExecutor,
			WebHookThreadingExecutor webHookThreadingExecutor) {
		myWebHookMainSettings = webHookMainSettings;
		myWebHookSerialExecutor = webHookSerialExecutor;
		myWebHookThreadingExecutor = webHookThreadingExecutor;
	}

	@Override
	public void execute(WebHook webHook, WebHookConfig whc, SQueuedBuild sBuild, BuildStateEnum state, String user,
			String comment, boolean isTest) {
		if (myWebHookMainSettings.useThreadedExecutor()) {
			myWebHookThreadingExecutor.execute(webHook, whc, sBuild, state, user, comment, isTest);
		} else {
			myWebHookSerialExecutor.execute(webHook, whc, sBuild, state, user, comment, isTest);
		}
	}

	@Override
	public void execute(WebHook webHook, WebHookConfig whc, SBuildType sBuildType,
			ResponsibilityEntry responsibilityEntryOld, ResponsibilityEntry responsibilityEntryNew, boolean isTest) {
		if (myWebHookMainSettings.useThreadedExecutor()) {
			myWebHookThreadingExecutor.execute(webHook, whc, sBuildType, responsibilityEntryOld, responsibilityEntryNew, isTest);
		} else {
			myWebHookSerialExecutor.execute(webHook, whc, sBuildType, responsibilityEntryOld, responsibilityEntryNew, isTest);
		}
	}

	@Override
	public void execute(WebHook webHook, WebHookConfig whc, SBuild sBuild, BuildStateEnum state, boolean isTest) {
		if (myWebHookMainSettings.useThreadedExecutor()) {
			myWebHookThreadingExecutor.execute(webHook, whc, sBuild, state, isTest);
		} else {
			myWebHookSerialExecutor.execute(webHook, whc, sBuild, state, isTest);
		}
	}

}
