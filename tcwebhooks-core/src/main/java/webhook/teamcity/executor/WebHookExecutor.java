package webhook.teamcity.executor;

import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.STest;
import jetbrains.buildServer.serverSide.mute.MuteInfo;
import jetbrains.buildServer.users.SUser;
import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.settings.WebHookConfig;

public interface WebHookExecutor {
	
	/**
	 * Executor for Add/Remove from Queue events.
	 * @param webHook
	 * @param sBuild
	 * @param state
	 * @param user
	 * @param comment
	 */
	public void execute(
			@NotNull WebHook webHook,
			@NotNull WebHookConfig whc,
			@NotNull SQueuedBuild sBuild, 
			@NotNull BuildStateEnum state,
			String user, 
			String comment,
			boolean isTest
		);
	
	/** 
	 * Executor for other build events.
	 * @param sBuild
	 * @param state
	 * @param extraAttributes 
	 */
	public void execute(
			@NotNull WebHook webHook,
			@NotNull WebHookConfig whc,
			@NotNull SBuild sBuild, 
			@NotNull BuildStateEnum state,
			String user, 
			String comment,
			boolean isTest, Map<String, String> extraAttributes
		);

	/**
	 * Executor for responsibility events.
	 * @param webHook
	 * @param webHookConfig
	 * @param state
	 * @param responsibilityHolder
	 * @param isTest
	 */
	public void execute(
			@NotNull WebHook webHook, 
			@NotNull WebHookConfig whc, 
			@NotNull BuildStateEnum state,
			@NotNull WebHookResponsibilityHolder responsibilityHolder, 
			boolean isTest);

	/**
	 * Executor for Tests Muted/Unmuted events.
	 * @param wh
	 * @param whc
	 * @param sProject
	 * @param mutedOrUnmutedGroups
	 * @param state
	 * @param user
	 * @param comment
	 * @param isTest
	 */
    public void execute(
            WebHook webHook, 
            WebHookConfig whc, 
            SProject sProject,
            Map<MuteInfo, Collection<STest>> mutedOrUnmutedGroups,
            BuildStateEnum state, 
            SUser user, 
            boolean isTest);

}
