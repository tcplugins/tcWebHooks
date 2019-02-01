package webhook.teamcity.executor;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SQueuedBuild;
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
	 * Executor for Responsibility changed.
	 * @param webHook
	 * @param sBuildType
	 * @param responsibilityEntryOld
	 * @param responsibilityEntryNew
	 */
	public void execute(
			@NotNull WebHook webHook,
			@NotNull WebHookConfig whc,
			@NotNull SBuildType sBuildType,
            @NotNull ResponsibilityEntry responsibilityEntryOld,
            @NotNull ResponsibilityEntry responsibilityEntryNew,
            boolean isTest
		);
	
	/** 
	 * Executor for other build events.
	 * @param sBuild
	 * @param state
	 */
	public void execute(
			@NotNull WebHook webHook,
			@NotNull WebHookConfig whc,
			SBuild sBuild, 
			BuildStateEnum state,
			boolean isTest
		);

}
