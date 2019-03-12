package webhook.teamcity.executor;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.SBuild;
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
	 * Executor for other build events.
	 * @param sBuild
	 * @param state
	 */
	public void execute(
			@NotNull WebHook webHook,
			@NotNull WebHookConfig whc,
			@NotNull SBuild sBuild, 
			@NotNull BuildStateEnum state,
			String user, 
			String comment,
			boolean isTest
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

}
