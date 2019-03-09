package webhook.teamcity.payload;

import java.util.Collection;

import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.problems.BuildProblemInfo;
import jetbrains.buildServer.tests.TestName;
import lombok.Builder;
import webhook.teamcity.executor.WebHookResponsibilityHolder;
import webhook.teamcity.payload.content.WebHookResponsibilityEntry;

/**
 * A serialisable version of WebHookResponsibilityHolder
 * so that it can be added to the payload.
 *
 */
@Builder
public class WebHookResponsibility {
	
	ResponsibilityEntry.State state;
	WebHookResponsibilityEntry responsibilityEntryOld;
	WebHookResponsibilityEntry responsibilityEntryNew;
	Boolean isUserAction;
	String projectId;
	String buildTypeId;
	TestNameResponsibilityEntry testNameResponsibilityEntry;
	Collection<TestName> testNames;
	Collection<WebHookBuildProblemInfo> buildProblems;
	
	public static WebHookResponsibility build(WebHookResponsibilityHolder wrh) {
		return
				builder()
				.state(wrh.getState())
				.responsibilityEntryOld(WebHookResponsibilityEntry.build(wrh.getResponsibilityEntryOld()))
				.responsibilityEntryNew(WebHookResponsibilityEntry.build(wrh.getResponsibilityEntryNew()))
				.isUserAction(wrh.getIsUserAction())
				.projectId(wrh.getSProject() != null ? wrh.getSProject().getExternalId() : null)
				.buildTypeId(wrh.getSBuildType() != null ? wrh.getSBuildType().getExternalId() : null)
				.testNameResponsibilityEntry(wrh.getTestNameResponsibilityEntry())
				.testNames(wrh.getTestNames())
				.buildProblems(WebHookBuildProblemInfo.buildCollection(wrh.getBuildProblems()))
				.build();
	}

}
