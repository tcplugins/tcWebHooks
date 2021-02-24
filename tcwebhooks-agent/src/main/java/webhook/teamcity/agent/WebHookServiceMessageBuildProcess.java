package webhook.teamcity.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildProgressLogger;

public class WebHookServiceMessageBuildProcess implements BuildProcess {
	
	private boolean isFinished = false;
	private boolean isInterrupted = false;
	private BuildProgressLogger myBuildLogger;

	public WebHookServiceMessageBuildProcess(BuildProgressLogger buildLogger) {
		myBuildLogger = buildLogger;
	}

	@Override
	public void start() throws RunBuildException {
		myBuildLogger.message("##teamcity[sendWebhook]");
		isFinished = true;
	}

	@Override
	public boolean isInterrupted() {
		return isFinished && isInterrupted;
	}

	@Override
	public boolean isFinished() {
		return isFinished;
	}

	@Override
	public void interrupt() {
		isInterrupted = true;
	}

	@Override
	public BuildFinishedStatus waitFor() throws RunBuildException {
		return BuildFinishedStatus.FINISHED_SUCCESS;
	}

}
