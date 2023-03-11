package webhook.teamcity.executor;

import java.util.Collection;
import java.util.Map;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.STest;
import jetbrains.buildServer.serverSide.mute.MuteInfo;
import jetbrains.buildServer.users.SUser;
import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.statistics.StatisticsReport;

public class WebHookExecutorManager implements WebHookExecutor, WebHookStatisticsExecutor {
	
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
	public void execute(WebHook webHook, WebHookConfig whc, SQueuedBuild sBuild, BuildStateEnum state, String username,
			String comment, boolean isTest) {
		if (myWebHookMainSettings.useThreadedExecutor()) {
			myWebHookThreadingExecutor.execute(webHook, whc, sBuild, state, username, comment, isTest);
		} else {
			myWebHookSerialExecutor.execute(webHook, whc, sBuild, state, username, comment, isTest);
		}
	}
	
	@Override
	public void execute(WebHook webHook, WebHookConfig whc, BuildStateEnum state,
			WebHookResponsibilityHolder responsibilityHolder, boolean isTest) {
		if (myWebHookMainSettings.useThreadedExecutor()) {
			myWebHookThreadingExecutor.execute(webHook, whc, state, responsibilityHolder, isTest);
		} else {
			myWebHookSerialExecutor.execute(webHook, whc, state, responsibilityHolder, isTest);
		}
	}

	@Override
	public void execute(WebHook webHook, WebHookConfig whc, SBuild sBuild, BuildStateEnum state, String username,
			String comment, boolean isTest, Map<String,String> extraAttributes) {
		if (myWebHookMainSettings.useThreadedExecutor()) {
			myWebHookThreadingExecutor.execute(webHook, whc, sBuild, state, username, comment, isTest, extraAttributes);
		} else {
			myWebHookSerialExecutor.execute(webHook, whc, sBuild, state, username, comment, isTest, extraAttributes);
		}
	}

	@Override
	public void execute(WebHook webHook, WebHookConfig whc, BuildStateEnum state, StatisticsReport report, SProject rootProject, boolean isTest) {
		myWebHookSerialExecutor.execute(webHook, whc, state, report, rootProject, isTest);

	}

    @Override
    public void execute(WebHook webHook, WebHookConfig whc, SProject sProject,
            Map<MuteInfo, Collection<STest>> mutedOrUnmutedGroups, BuildStateEnum state, SUser user, boolean isTest) {
        if (myWebHookMainSettings.useThreadedExecutor()) {
            myWebHookThreadingExecutor.execute(webHook, whc, sProject, mutedOrUnmutedGroups, state, user, isTest);
        } else {
            myWebHookSerialExecutor.execute(webHook, whc, sProject, mutedOrUnmutedGroups, state, user, isTest);
        }
    }

}
