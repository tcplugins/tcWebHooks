package webhook.teamcity.executor;

import java.util.Collection;

import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.problems.BuildProblemInfo;
import jetbrains.buildServer.tests.TestName;
import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class WebHookResponsibilityHolder {

	ResponsibilityEntry.State state;
	ResponsibilityEntry responsibilityEntryOld;
	ResponsibilityEntry responsibilityEntryNew;
	Boolean isUserAction;
	SProject sProject;
	SBuildType sBuildType;
	TestNameResponsibilityEntry testNameResponsibilityEntry;
	Collection<TestName> testNames;
	Collection<BuildProblemInfo> buildProblems;
	
}


