package webhook.teamcity;

import java.util.Collection;

import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.ResponsibilityInfo;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.tests.TestName;

public class TestListener extends BuildServerAdapter {
	
	private SBuildServer myBuildServer;

	public TestListener(SBuildServer sBuildServer, ProjectSettingsManager settings) {
		myBuildServer = sBuildServer;
		logit("TestListener :: Starting");
	}

	public void register(){
		myBuildServer.addListener(this);
		logit("TestListener :: Registering");
	}

	@Override
	public void beforeBuildFinish(SRunningBuild runningBuild,
			boolean buildFailed) {
		logit("beforeBuildFinish(SRunningBuild runningBuild,boolean buildFailed)");
	}

	@Override
	public void beforeBuildFinish(SRunningBuild runningBuild) {
		logit("beforeBuildFinish(SRunningBuild runningBuild)");
	}

	@Override
	public void buildFinished(SRunningBuild build) {
		logit("buildFinished(SRunningBuild build)");
	}

	@Override
	public void buildInterrupted(SRunningBuild build) {
		logit("buildInterrupted(SRunningBuild build)");
	}

	@Override
	public void buildStarted(SRunningBuild build) {
		logit("buildStarted(SRunningBuild build)");
	}

	@Override
	public void responsibleChanged(SBuildType bt, ResponsibilityInfo oldValue,
			ResponsibilityInfo newValue, boolean isUserAction) {
		logit("responsibleChanged(SBuildType bt, ResponsibilityInfo oldValue,	ResponsibilityInfo newValue, boolean isUserAction)");
	}

	@Override
	public void responsibleChanged(SProject project,
			Collection<TestName> testNames, ResponsibilityEntry entry,
			boolean isUserAction) {
		logit("responsibleChanged(SProject project, Collection<TestName> testNames, ResponsibilityEntry entry, boolean isUserAction)");
	}

	@Override
	public void responsibleChanged(SProject project,
			TestNameResponsibilityEntry oldValue,
			TestNameResponsibilityEntry newValue, boolean isUserAction) {
		logit("responsibleChanged(SProject project, TestNameResponsibilityEntry oldValue, TestNameResponsibilityEntry newValue, boolean isUserAction)");
	}

	private void logit(String s){
		Loggers.SERVER.info("#####################################################################");
		Loggers.SERVER.info("# " + s);
		Loggers.SERVER.info("#####################################################################");
	}
	
	
}
